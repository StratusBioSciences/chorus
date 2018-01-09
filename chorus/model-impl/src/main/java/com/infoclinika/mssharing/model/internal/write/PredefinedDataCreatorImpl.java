/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.model.internal.write;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.infoclinika.mssharing.model.PredefinedDataCreator;
import com.infoclinika.mssharing.model.api.MSFunctionType;
import com.infoclinika.mssharing.model.api.MSResolutionType;
import com.infoclinika.mssharing.model.helper.ColumnViewHelper;
import com.infoclinika.mssharing.model.internal.entity.*;
import com.infoclinika.mssharing.model.internal.entity.payment.BillingProperty;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveExperiment;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveFileMetaData;
import com.infoclinika.mssharing.model.internal.repository.*;
import com.infoclinika.mssharing.model.read.DetailsReader;
import com.infoclinika.mssharing.platform.entity.ExperimentFileTemplate;
import com.infoclinika.mssharing.platform.entity.Species;
import com.infoclinika.mssharing.platform.model.impl.DefaultPredefinedDataCreator;
import com.infoclinika.mssharing.platform.repository.SpeciesRepositoryTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.ImmutableSet.of;
import static com.google.common.collect.Lists.newArrayList;
import static com.infoclinika.mssharing.model.write.ExperimentCategory.MICROARRAY;
import static com.infoclinika.mssharing.model.write.ExperimentCategory.PROTEOMICS;

/**
 * @author Stanislav Kurilin
 */
@Component
public class PredefinedDataCreatorImpl extends DefaultPredefinedDataCreator implements PredefinedDataCreator {
    private static final Logger LOG = LoggerFactory.getLogger(PredefinedDataCreatorImpl.class);
    @Inject
    private SpeciesRepositoryTemplate<Species> speciesRepository;

    @Inject
    private ProteinDatabaseRepository proteinDatabaseRepository;
    @Inject
    private FileMetaDataRepository fileMetaDataRepository;

    @Inject
    ColumnDefinitionRepository columnDefinitionRepo;

    @Inject
    private ColumnViewRepository columnViewRepository;

    @Inject
    private ExperimentRepository experimentRepository;

    @Inject
    private MSFunctionItemRepository msFunctionItemRepository;

    @Inject
    private BillingPropertyRepository billingPropertyRepository;

    @Inject
    private ExperimentLabelManagement experimentLabelManagement;

    @Override
    public long proteinDatabase(long user, String dbName, String contentUrl, String specie) {
        final Species specieEntity = speciesRepository.findByName(specie);
        final ProteinDatabase db = new ProteinDatabase(dbName, specieEntity, new Date(), 1L, true, Util.USER_FROM_ID.apply(user), false, PROTEOMICS);
        db.setContentId(contentUrl);
        return proteinDatabaseRepository.save(db).getId();
    }

    @Override
    public long cdfDatabase(long user, String dbName, String contentUrl, String specie) {
        final ProteinDatabase cdfDatabase = proteinDatabaseRepository.findByName(dbName);
        if (cdfDatabase != null) {
            LOG.info("CDF database was created already, name: " + dbName);
            return cdfDatabase.getId();
        }

        final Species specieEntity = speciesRepository.findByName(specie);
        final ProteinDatabase db = new ProteinDatabase(dbName, specieEntity, new Date(), 1L, true, Util.USER_FROM_ID.apply(user), false, MICROARRAY);
        db.setContentId(contentUrl);
        return proteinDatabaseRepository.save(db).getId();
    }

    @Override
    public long translateFileForRun(long bob, long file, long experiment) {
        return translateFileForRun(bob, file, experiment, "FTMS + p NSI Full ms [375.00-2000.00]", "MS2_DDA-ITMS");
    }

    @Override
    public long translateFileForRun(final long bob, long file, long experiment, String ms1FunctionName, String ms2FunctionName) {
        final ActiveFileMetaData fmd = fileMetaDataRepository.findOne(file);
        final MSFunctionItem ms1 = new MSFunctionItem();
        ms1.setFunctionName(ms1FunctionName);
        ms1.setFunctionType(MSFunctionType.MS);
        ms1.setTranslatedPath(ms1FunctionName);
        ms1.setResolutionType(MSResolutionType.HIGH);

        final MSFunctionItem ms2 = new MSFunctionItem();
        ms2.setTranslatedPath(ms2FunctionName);
        ms2.setFunctionName(ms2FunctionName);
        ms2.setFunctionType(MSFunctionType.MS2);
        ms2.setResolutionType(MSResolutionType.HIGH);
        final ActiveExperiment one = experimentRepository.findOne(experiment);
        final Optional<UserLabFileTranslationData> userTranslationDataOld = Iterables.tryFind(fmd.getUsersFunctions(), new Predicate<UserLabFileTranslationData>() {
            @Override
            public boolean apply(UserLabFileTranslationData input) {
                return input.getUser().getId() == bob;
            }
        });
        if (userTranslationDataOld.isPresent()) {
            userTranslationDataOld.get().getFunctions().addAll(of(ms1, ms2));
        } else {
            final UserLabFileTranslationData translationData = new UserLabFileTranslationData(new User(bob), of(ms1, ms2), one.getLab());
            fmd.getUsersFunctions().add(translationData);
        }

        return fileMetaDataRepository.save(fmd).getId();
    }

    @Override
    @Transactional
    public long translateExperiment(long actor, long experiment) {

        final ActiveExperiment one = experimentRepository.findOne(experiment);

        for (ExperimentFileTemplate file : one.getRawFiles().getData()) {

            RawFile rawFile = (RawFile) file;
            final UserLabFileTranslationData translationData = from(rawFile.getFileMetaData().getUsersFunctions())
                    .firstMatch(new Predicate<UserLabFileTranslationData>() {
                        @Override
                        public boolean apply(UserLabFileTranslationData input) {
                            return input.getLab().equals(one.getLab());
                        }
                    })
                    .or(new UserLabFileTranslationData(new User(actor), rawFile.getFileMetaData().getUsersFunctions().iterator().next().getFunctions(), one.getLab()));

            rawFile.getFileMetaData().getUsersFunctions().add(translationData);
            fileMetaDataRepository.save((ActiveFileMetaData) rawFile.getFileMetaData());

        }

        return one.getId();
    }

    @Override
    public List<Long> createColumnsDefinitions(List<ColumnViewHelper.Column> info, ColumnViewHelper.ColumnViewType type) {
        List<Long> ids = newArrayList();
        final ColumnsView.Type columnViewType = asColumnViewType(type);
        for (ColumnViewHelper.Column i : info) {
            final Long id = columnDefinitionRepo.save(new ColumnDefinition(i.name, columnViewType, i.dataType, i.sortable, i.hideable, i.units)).getId();
            ids.add(id);
        }
        return ids;
    }


    @Override
    public long defaultColumnsView(Set<ColumnViewHelper.ColumnInfo> view, ColumnViewHelper.ColumnViewType type) {
        final ColumnsView.Type columnViewType = asColumnViewType(type);
        final ColumnsView columnsView = new ColumnsView("Default", columnViewType, null);
        columnsView.setDefault(true);
        columnsView.getColumns().addAll(from(view).transform(new Function<ColumnViewHelper.ColumnInfo, ViewColumn>() {
            @Override
            public ViewColumn apply(ColumnViewHelper.ColumnInfo input) {
                final ColumnDefinition columnDefinition = columnDefinitionRepo.findOne(input.originalColumn);
                return new ViewColumn(columnDefinition, input.order);
            }
        }).toSet());
        return columnViewRepository.save(columnsView).getId();
    }

    @Override
    public void msFunctionItem(String name, String translatedPath) {
        MSFunctionItem item = new MSFunctionItem();
        item.setFunctionName(name);
        item.setTranslatedPath(translatedPath);
        msFunctionItemRepository.save(item);
    }

    @Override
    public long createExperimentLabelType(String name, int maxSamples) {
        final ExperimentLabelManagement.ExperimentTypeInfo typeToCreate
                = new ExperimentLabelManagement.ExperimentTypeInfo(name, maxSamples);

        return experimentLabelManagement.createLabelType(typeToCreate);
    }

    @Override
    public long createExperimentLabel(long experimentLabelType, String aminoAcid, String name) {
        final ExperimentLabelManagement.ExperimentLabelInfo labelToCreate =
                new ExperimentLabelManagement.ExperimentLabelInfo(aminoAcid, name, experimentLabelType);

        return experimentLabelManagement.createLabel(labelToCreate);
    }

    private static ColumnsView.Type asColumnViewType(ColumnViewHelper.ColumnViewType type) {
        switch (type) {
            case EXPERIMENT:
                return ColumnsView.Type.EXPERIMENT_META;
            case PROJECT:
                return ColumnsView.Type.PROJECT_META;
            case FILE:
                return ColumnsView.Type.FILE_META;
            default:
                throw new IllegalArgumentException("Can't convert unknown type: " + type);
        }
    }

    @Override
    public void billingProperties() {
        billingPropertyRepository.save(new BillingProperty(BillingProperty.BillingPropertyName.ENTERPRISE_ACCOUNT_STORAGE_VOLUME_COST, Long.toString(4000L)));
        billingPropertyRepository.save(new BillingProperty(BillingProperty.BillingPropertyName.ENTERPRISE_ACCOUNT_STORAGE_VOLUME_SIZE, Long.toString(536870912000L)));
        billingPropertyRepository.save(new BillingProperty(BillingProperty.BillingPropertyName.FREE_ACCOUNT_STORAGE_LIMIT, Long.toString(107374182400L)));
        billingPropertyRepository.save(new BillingProperty(BillingProperty.BillingPropertyName.PROCESSING_FEATURE_COST, Long.toString(20000L)));
        billingPropertyRepository.save(new BillingProperty(BillingProperty.BillingPropertyName.FREE_ACCOUNT_ARCHIVE_STORAGE_LIMIT, Long.toString(0L)));
        billingPropertyRepository.save(new BillingProperty(BillingProperty.BillingPropertyName.ENTERPRISE_ACCOUNT_ARCHIVE_STORAGE_VOLUME_COST, Long.toString(4000L)));
        billingPropertyRepository.save(new BillingProperty(BillingProperty.BillingPropertyName.ENTERPRISE_ACCOUNT_ARCHIVE_STORAGE_VOLUME_SIZE, Long.toString(536870912000L)));
    }
}
