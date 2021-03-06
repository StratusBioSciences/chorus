/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.model.internal;

import com.google.common.base.Functions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.infoclinika.mssharing.model.api.MSFunctionType;
import com.infoclinika.mssharing.model.features.ApplicationFeature;
import com.infoclinika.mssharing.model.helper.BillingFeaturesHelper;
import com.infoclinika.mssharing.model.helper.FeaturesHelper;
import com.infoclinika.mssharing.model.internal.entity.AnnotationAttachment;
import com.infoclinika.mssharing.model.internal.entity.Instrument;
import com.infoclinika.mssharing.model.internal.entity.Lab;
import com.infoclinika.mssharing.model.internal.entity.MSFunctionItem;
import com.infoclinika.mssharing.model.internal.entity.ProteinDatabase;
import com.infoclinika.mssharing.model.internal.entity.RawFile;
import com.infoclinika.mssharing.model.internal.entity.UploadAppConfiguration;
import com.infoclinika.mssharing.model.internal.entity.User;
import com.infoclinika.mssharing.model.internal.entity.UserLabFileTranslationData;
import com.infoclinika.mssharing.model.internal.entity.Util;
import com.infoclinika.mssharing.model.internal.entity.restorable.AbstractFileMetaData;
import com.infoclinika.mssharing.model.internal.entity.restorable.AbstractProject;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveExperiment;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveFileMetaData;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveProject;
import com.infoclinika.mssharing.model.internal.entity.restorable.DeletedExperiment;
import com.infoclinika.mssharing.model.internal.entity.restorable.DeletedFileMetaData;
import com.infoclinika.mssharing.model.internal.entity.restorable.DeletedProject;
import com.infoclinika.mssharing.model.internal.entity.restorable.StorageData;
import com.infoclinika.mssharing.model.internal.repository.AnnotationAttachmentRepository;
import com.infoclinika.mssharing.model.internal.repository.DeletedExperimentRepository;
import com.infoclinika.mssharing.model.internal.repository.DeletedFileMetaDataRepository;
import com.infoclinika.mssharing.model.internal.repository.ExperimentRepository;
import com.infoclinika.mssharing.model.internal.repository.FeaturesRepository;
import com.infoclinika.mssharing.model.internal.repository.FileMetaDataRepository;
import com.infoclinika.mssharing.model.internal.repository.InstrumentRepository;
import com.infoclinika.mssharing.model.internal.repository.LabRepository;
import com.infoclinika.mssharing.model.internal.repository.ProjectRepository;
import com.infoclinika.mssharing.model.internal.repository.ProteinDatabaseRepository;
import com.infoclinika.mssharing.model.internal.repository.UserRepository;
import com.infoclinika.mssharing.platform.entity.EntityUtil;
import com.infoclinika.mssharing.platform.entity.ExperimentFileTemplate;
import com.infoclinika.mssharing.platform.entity.Sharing;
import com.infoclinika.mssharing.platform.entity.UserTemplate;
import com.infoclinika.mssharing.platform.model.impl.DefaultRuleValidator;
import com.infoclinika.mssharing.platform.model.impl.ValidatorPredicates;
import com.infoclinika.mssharing.platform.model.read.AccessLevel;
import com.infoclinika.mssharing.platform.repository.FileProjectUsage;
import com.infoclinika.mssharing.platform.repository.UserLabMembershipRepositoryTemplate;
import com.infoclinika.mssharing.services.billing.rest.api.model.BillingFeature;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.and;
import static com.google.common.base.Predicates.not;
import static com.google.common.base.Predicates.or;
import static com.google.common.collect.Iterables.any;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;
import static com.infoclinika.mssharing.model.features.ApplicationFeature.BILLING;
import static com.infoclinika.mssharing.model.features.ApplicationFeature.MICROARRAYS;
import static com.infoclinika.mssharing.model.features.ApplicationFeature.PROTEIN_ID_SEARCH;
import static com.infoclinika.mssharing.model.features.ApplicationFeature.PROTEIN_ID_SEARCH_RESULTS;
import static com.infoclinika.mssharing.model.features.ApplicationFeature.TRANSLATION;
import static com.infoclinika.mssharing.model.internal.entity.restorable.StorageData.Status.ARCHIVED;
import static com.infoclinika.mssharing.model.internal.entity.restorable.StorageData.Status.ARCHIVING_REQUESTED;
import static com.infoclinika.mssharing.model.internal.entity.restorable.StorageData.Status.UNARCHIVED;
import static com.infoclinika.mssharing.model.internal.entity.restorable.StorageData.Status.UNARCHIVING_REQUESTED;
import static com.infoclinika.mssharing.model.internal.read.Transformers.MS_FUNCTIONS_FROM_USER_TRANSLATION_DATA;
import static com.infoclinika.mssharing.model.internal.read.Transformers.RAW_META_DATA_TRANSFORMER;
import static com.infoclinika.mssharing.platform.model.impl.ValidatorPreconditions.checkPresence;

/**
 * @author Stanislav Kurilin
 */
@Component("validator")
@Transactional(readOnly = true)
public class RuleValidatorImpl extends DefaultRuleValidator<ActiveExperiment, ActiveFileMetaData, ActiveProject,
        Instrument, Lab> implements RuleValidator {

    public static final java.util.function.Predicate<ActiveFileMetaData> IS_UPLOAD_COMPLETE = input -> input.getContentId() != null;
    private static final java.util.function.Predicate<ActiveFileMetaData> IS_ON_GLACIER = input -> input.getArchiveId() != null;
    private static final Function<ExperimentFileTemplate, AbstractFileMetaData> META_DATA_FROM_RAW_FILE = input -> (AbstractFileMetaData) getMetaData(input);
    private static final Function<AbstractFileMetaData, Iterable<UserLabFileTranslationData>> USER_FUNCTIONS_FROM_META_DATA = AbstractFileMetaData::getUsersFunctions;
    @Inject
    private ProjectRepository projectRepository;
    @Inject
    private ExperimentRepository experimentRepository;
    @Inject
    private DeletedExperimentRepository deletedExperimentRepository;
    @Inject
    private FileMetaDataRepository fileMetaDataRepository;
    @Inject
    private DeletedFileMetaDataRepository deletedFileMetaDataRepository;
    @Inject
    private InstrumentRepository instrumentRepository;
    @Inject
    private UserRepository userRepository;
    @Inject
    private LabRepository labRepository;
    @Inject
    private ProteinDatabaseRepository proteinDatabaseRepository;
    @Inject
    private BillingFeaturesHelper billingFeaturesHelper;
    @Inject
    private AnnotationAttachmentRepository annotationAttachmentRepository;
    @Inject
    private UserLabMembershipRepositoryTemplate userLabMembershipRepository;
    @Inject
    private FeaturesHelper featuresHelper;
    @Inject
    private FeaturesRepository featuresRepository;


    /**
     * User can create experiment run in experiment only if this user is it's creator or labhead
     */
    @Override
    public boolean userHasPermissionToCreateSearch(long creator, long experiment) {
        final ActiveExperiment experimentEntity = checkPresence(experimentRepository.findOne(experiment));

        final User user = Util.USER_FROM_ID.apply(creator);
        return experimentEntity.getCreator().equals(user) || isExperimentLabHead(experimentEntity, creator);
    }

    @Override
    public Predicate<ActiveFileMetaData> userHasReadPermissionsOnFilePredicate(long userId) {
        return validatorPredicates.userHasReadPermissionsOnFile(userId);
    }

    /*Checks if files of experiment are suitable for translation*/
    @Override
    public boolean isAllFilesInExperimentTranslatedForSearch(long experiment) {
        final ActiveExperiment experimentEntity = checkPresence(experimentRepository.findOne(experiment));
        boolean translated = true;
        for (ExperimentFileTemplate file : experimentEntity.getRawFiles().getData()) {
            boolean ms = false;
            for (MSFunctionItem msItem : getMsFunctionItems(experimentEntity, file)) {
                if (msItem.getFunctionType() == MSFunctionType.MS2) {
                    ms = true;
                }
            }
            if (!(ms)) {
                translated = false;
                break;
            }
        }
        return translated;
    }

    private Set<MSFunctionItem> getMsFunctionItems(final ActiveExperiment experiment, ExperimentFileTemplate file) {

        return getMetaData(file).getUsersFunctions()
                .stream()
                .filter(input -> input.getLab().equals(getExperimentLab(experiment).orElse(null)))
                .findFirst()
                .map(MS_FUNCTIONS_FROM_USER_TRANSLATION_DATA::apply)
                .orElse(Collections.emptySet());
    }

    private static AbstractFileMetaData getMetaData(ExperimentFileTemplate file) {
        return (AbstractFileMetaData) file.getFileMetaData();
    }

    @Override
    public AccessLevel getAccessLevel(AbstractFileMetaData input) {

        final List<FileProjectUsage> projects = fileMetaDataRepository.findFileProjectUsage(input.getId());

        if (includedToProjectsOfType(projects, Sharing.Type.PUBLIC)) {
            return AccessLevel.PUBLIC;
        }
        if (includedToProjectsOfType(projects, Sharing.Type.SHARED)) {
            return AccessLevel.SHARED;
        }
        return AccessLevel.PRIVATE;
    }

    private boolean includedToProjectsOfType(List<FileProjectUsage> projects, final Sharing.Type type) {
        return Iterables.tryFind(projects, input -> input.sharingType.equals(type)).isPresent();
    }

    @Override
    public Predicate<ActiveFileMetaData> filesFromMatchedProjectsPredicate(Predicate<AbstractProject> projectPredicate) {
        return validatorPredicates.filesFromMatchedProjects(projectPredicate);
    }


    @Override
    public boolean canAccessExistingInstrument(long actor, String serialNumber) {
        final Lab ownerLab = instrumentRepository.labOfInstrument(serialNumber);
        final Set<Lab> actorLabs = super.findLabs(actor);
        return actorLabs.contains(ownerLab);
    }

    @Override
    public boolean canReadLabBilling(long actor, long lab) {
        final List<Lab> actorLabs = labRepository.findForUser(actor);
        final boolean userBelongsToLab = actorLabs.stream().filter(l -> l.getId().equals(lab)).findFirst().isPresent();
        return isAdmin(actor) || userBelongsToLab; /*|| isLabHead(actor, lab)*/
    }

    @Override
    public boolean isLabHead(long actor, long lab) {
        final Lab labEntity = labRepository.findOne(lab);
        return labEntity.getHead().getId().equals(actor);
    }

    @Override
    public boolean canUserManageLabAccount(long actor, long lab) {
        return isAdmin(actor) || isLabHead(actor, lab);
    }

    @Override
    public boolean isProjectOwner(long actor, long projectId) {
        return ValidatorPredicates.isOwnerInProject(factories.userFromId.apply(actor)).apply(projectRepository.findOne(projectId));
    }

    @Override
    public boolean canReadUsersInLab(long labHead, long labId) {
        final Lab lab = checkPresence(labRepository.findOne(labId));
        return lab.getHead().getId().equals(labHead);
    }


    @Override
    public boolean canRestoreProject(long actor, DeletedProject project) {
        final UserTemplate user = factories.userFromId.apply(actor);
        return (project.getCreator().getId().equals(actor) || ValidatorPredicates.isProjectLabHead(user).apply(project))
                && !projectHasDuplicateNames(actor, project);
    }

    @Override
    public boolean projectHasDuplicateNames(long owner, DeletedProject project) {
        if (projectRepository.findByName(owner, project.getName()).size() > 0) {
            return true;
        } else {
            for (DeletedExperiment e : deletedExperimentRepository.findByProject(project.getId())) {
                if (experimentHasDuplicateNames(e.getCreator().getId(), e)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean canRestoreExperiment(long actor, DeletedExperiment experiment) {
        return (experiment.getCreator().getId().equals(actor) || isExperimentLabHead(experiment, actor))
                && !experimentHasDuplicateNames(actor, experiment);
    }

    @Override
    public boolean experimentHasDuplicateNames(long owner, DeletedExperiment experiment) {
        if (experimentRepository.findByName(owner, experiment.getName()).size() > 0) {
            return true;
        } else {
            for (ExperimentFileTemplate rawFile : experiment.getRawFiles().getData()) {
                DeletedFileMetaData deletedFile = deletedFileMetaDataRepository.findOne(getMetaData(rawFile).getId());
                if (deletedFile != null && fileHasDuplicateName(deletedFile)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean canRemoveProteinSearch(long actor, long experimentRunId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canRestoreFile(long actor, DeletedFileMetaData file) {
        return (file.getOwner().getId().equals(actor) || file.getInstrument().getLab().getHead().getId().equals(actor))
                && !fileHasDuplicateName(file);
    }

    @Override
    public boolean fileHasDuplicateName(DeletedFileMetaData file) {
        return fileMetaDataRepository.findByInstrumentWithName(file.getInstrument(), file.getName()).size() > 0;
    }

    @Override
    public Predicate<ActiveExperiment> isUserCanReadExperimentPredicate(long actor) {
        return validatorPredicates.isUserCanReadExperiment(actor);
    }

    @Override
    public boolean isUserCanReadExperiment(long actor, long experiment) {
        return super.isUserCanReadExperiment(actor, experiment);
    }

    @Override
    public Predicate<Instrument> isUserCanReadInstrumentPredicate(long actor) {
        return validatorPredicates.isUserCanReadInstrument(actor);
    }

    @Override
    public boolean isUserCanReadProteinSearches(long actor, long experimentId) {

        final ActiveExperiment experiment = checkPresence(experimentRepository.findOne(experimentId));
        final Optional<Lab> labOpt = getExperimentLab(experiment);

        if(!labOpt.isPresent()) {
            return false;
        }

        boolean enabledForLab = isFeatureEnabledForLab(PROTEIN_ID_SEARCH, labOpt.get().getId());
        return enabledForLab && (validatorPredicates.isUserCanReadExperiment(actor).apply(experiment) || isAdmin(actor));
    }


    @Override
    public boolean canUserCreateProteinSearchWithTitle(long actor, long experiment, String runName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean userHasPermissionToRetranslateAllExperiments(long actor) {
        final User user = checkNotNull(userRepository.findOne(actor));
        return user.isAdmin();
    }

    @Override
    public boolean userCanReadExperimentTranslationStatuses(long actor) {
        final User user = checkNotNull(userRepository.findOne(actor));
        return user.isAdmin();
    }

    @Override
    public boolean hasAdminRights(long actor) {
        return isAdmin(actor);
    }

    @Override
    public boolean userHasPermissionToRunProteinIDSearches(long actor, List<Long> experimentSearches) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean userCanReadProteinSearchStatuses(long actor) {
        return isAdmin(actor);
    }

    @Override
    public boolean canManageNews(long actor) {
        return isAdmin(actor);
    }


    @Override
    public boolean isUploadAppConfigurationOwner(long actor, UploadAppConfiguration configuration) {
        final User user = checkNotNull(configuration).getUser();
        return user != null && user.getId() == actor;
    }

    @Override
    public boolean isExperimentReadyToDownload(ActiveExperiment activeExperiment) {
        return activeExperiment.getRawFiles().getData()
                .stream()
                .map(RAW_META_DATA_TRANSFORMER::apply)
                .allMatch(rawFileHasStorageStatus(UNARCHIVED)::apply);
    }


    @Override
    public boolean canTranslateFile(long actor, long lab, long fileId) {
        if (!isFeatureEnabledForLab(TRANSLATION, lab) || !isBillingFeatureEnabledForLab(BillingFeature.TRANSLATION, lab)) {
            return false;
        }
        final boolean userHasLab = (userLabMembershipRepository.findByLabAndUser(lab, actor) != null);
        return userHasLab && userHasReadPermissionsOnFile(actor, fileId);
    }

    @Override
    public boolean canReTranslateFiles(long actor, List<Long> files) {
        final List<ActiveFileMetaData> dataList = fileMetaDataRepository.findAllByIds(files);
        final User user = userRepository.findOne(actor);
        final java.util.function.Predicate<ActiveFileMetaData> canTranslateCompoundPredicate =
                ((java.util.function.Predicate<ActiveFileMetaData>) input -> input.getInstrument().isOperator(user))
                        .and(input -> isBillingFeatureEnabledForLab(BillingFeature.TRANSLATION, input.getInstrument().getLab().getId()))
                        .and(input -> isFeatureEnabledForLab(TRANSLATION, input.getInstrument().getLab().getId()));
        return dataList.stream().allMatch(canTranslateCompoundPredicate);
    }

    @Override
    public boolean canModifyProteinDatabase(long actor, long proteinDatabase) {
        return isProteinDatabaseOwner(actor, proteinDatabase);
    }

    @Override
    public boolean canReadProteinDatabase(long actor, long proteinDatabase) {
        final ProteinDatabase database = proteinDatabaseRepository.findOne(proteinDatabase);
        return database.isbPublic() || database.getUser().getId().equals(actor);
    }

    private boolean isProteinDatabaseOwner(long actor, long proteinDatabase) {
        User user = proteinDatabaseRepository.findOne(proteinDatabase).getUser();
        return user.getId().equals(actor);
    }

    @Override
    public boolean canTranslateExperimentFiles(final long actor, @Nullable Long chargedLab, ActiveExperiment experiment) {

        if (chargedLab == null ||
                chargedLab == 0 ||
                !isFeatureEnabledForLab(TRANSLATION, chargedLab) ||
                !isBillingFeatureEnabledForLab(BillingFeature.TRANSLATION, chargedLab)) {
            return false;
        }

        boolean bCanTranslateFile = true;

        for (RawFile rawFile : experiment.getRawFiles().getData()) {
            if (!canTranslateFile(actor, chargedLab, rawFile.getFileMetaData().getId())) {
                bCanTranslateFile = false;
                break;
            }
        }

        return bCanTranslateFile;
    }

    private Optional<Lab> getExperimentLab(ActiveExperiment experiment) {
        return Optional.ofNullable(experiment.getLab() != null ? experiment.getLab() : experiment.getBillLaboratory());
    }

    private Predicate<AbstractFileMetaData> fileHasStorageStatus(final StorageData.Status status) {
        return input -> input.getStorageData().getStorageStatus().equals(status);
    }

    private Predicate<AbstractFileMetaData> isLabHeadOfFileInstrument(final long actor) {
        return userIsLabHeadOfFile(actor);
    }

    @Override
    public boolean canArchiveFile(long actor, long file) {

        final ActiveFileMetaData fileMetaData = checkNotNull(fileMetaDataRepository.findOne(file));

        final Long lab = fileMetaData.getInstrument().getLab().getId();
        return isBillingFeatureEnabledForLab(BillingFeature.ANALYSE_STORAGE, lab)
                && isBillingFeatureEnabledForLab(BillingFeature.ARCHIVE_STORAGE, lab)
                && not(fileHasStorageStatus(ARCHIVED)).apply(fileMetaData)
                && (validatorPredicates.userIsOwnerOfFile(actor).apply(fileMetaData) || isLabHeadOfFileInstrument(actor).apply(fileMetaData));
    }

    @Override
    public boolean canUnarchiveFile(long actor, long file) {
        final ActiveFileMetaData fileMetaData = checkNotNull(fileMetaDataRepository.findOne(file));
        final Long lab = fileMetaData.getInstrument().getLab().getId();
        return isBillingFeatureEnabledForLab(BillingFeature.ANALYSE_STORAGE, lab)
                && not(fileHasStorageStatus(UNARCHIVED)).apply(fileMetaData)
                && (validatorPredicates.userIsOwnerOfFile(actor).apply(fileMetaData) || isLabHeadOfFileInstrument(actor).apply(fileMetaData));
    }

    @Override
    public boolean canArchiveExperiment(final long actor, ActiveExperiment experiment) {

        final Optional<Lab> lab = getExperimentLab(experiment);

        if (!lab.isPresent()) {
            return false;
        }

        final Long labId = lab.get().getId();
        final Iterable<AbstractFileMetaData> rawFiles = transform(experiment.getRawFiles().getData(), RAW_META_DATA_TRANSFORMER);

        return (isExperimentCreator(experiment, actor) || isExperimentLabHead(experiment, actor))
                && isBillingFeatureEnabledForLab(BillingFeature.ARCHIVE_STORAGE, labId)
                && anyFilesHaveStatus(rawFiles, UNARCHIVED)
                && any(rawFiles, or(validatorPredicates.userIsOwnerOfFile(actor), isLabHeadOfFileInstrument(actor)));
    }


    @Override
    public boolean canUnarchiveExperiment(long actor, ActiveExperiment experiment) {

        final Optional<Lab> lab = getExperimentLab(experiment);

        if (!lab.isPresent()) {
            return false;
        }

        final Long labId = lab.get().getId();
        final Iterable<AbstractFileMetaData> rawFiles = transform(experiment.getRawFiles().getData(), RAW_META_DATA_TRANSFORMER);

        return (isExperimentCreator(experiment, actor) || isExperimentLabHead(experiment, actor))
                && isBillingFeatureEnabledForLab(BillingFeature.ANALYSE_STORAGE, labId)
                && anyFilesHaveStatus(rawFiles, ARCHIVED, UNARCHIVING_REQUESTED, ARCHIVING_REQUESTED)
                && any(rawFiles, or(validatorPredicates.userIsOwnerOfFile(actor), isLabHeadOfFileInstrument(actor)));
    }

    @Override
    public boolean isBillingEnabledForLab(long lab) {
        return isFeatureEnabledForLab(BILLING, lab);
    }

    @Override
    public boolean isSearchResultsFeatureEnabledForLab(long lab) {
        return isFeatureEnabledForLab(PROTEIN_ID_SEARCH_RESULTS, lab);
    }

    @Override
    public boolean canUserAccessSearchResults(long actor, long run) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isTranslationEnabledForLab(long lab) {
        return isFeatureEnabledForLab(TRANSLATION, lab) && isBillingFeatureEnabledForLab(BillingFeature.TRANSLATION, lab);
    }

    @Override
    public boolean canLabUseProteinIdSearch(long lab) {
        return isFeatureEnabledForLab(PROTEIN_ID_SEARCH, lab)
                && isBillingFeatureEnabledForLab(BillingFeature.PROCESSING, lab)
                && isBillingFeatureEnabledForLab(BillingFeature.TRANSLATION, lab);
    }

    @Override
    public boolean canRemoveFile(long actor, long file) {

        final boolean removeFile = super.canRemoveFile(actor, file);

        if (!removeFile) {
            return false;
        }

        final ActiveFileMetaData entity = fileMetaDataRepository.findOne(file);

        if (!RuleValidatorImpl.IS_UPLOAD_COMPLETE.test(entity) && !RuleValidatorImpl.IS_ON_GLACIER.test(entity)) {
            return false;
        }

        return true;
    }

    private boolean anyFilesHaveStatus(Iterable<AbstractFileMetaData> files, final StorageData.Status... status) {
        return any(files, or(statusPredicateTransformer(status)));
    }

    private Iterable<Predicate<AbstractFileMetaData>> statusPredicateTransformer(StorageData.Status... status) {
        return newArrayList(status).stream().map(this::rawFileHasStorageStatus).collect(Collectors.toList());
    }

    private Predicate<AbstractFileMetaData> rawFileHasStorageStatus(final StorageData.Status status) {
        return input -> input.getStorageData().getStorageStatus().equals(status);
    }

    private boolean isExperimentCreator(ActiveExperiment experiment, long actor) {
        return experiment.getCreator().getId().equals(actor);
    }

    @Override
    public boolean canCreateWorkflow(long actor) {
        return findUser(actor).isAdmin();
    }

    @Override
    public boolean isUserCanReadWorkflow(long actor) {
        return true;
    }

    @Override
    public boolean canRemoveTranslationData(final long actor, long file, final long lab) {

        if (!isTranslationEnabledForLab(lab)) {
            return false;
        }

        final ActiveFileMetaData fileMetaData = fileMetaDataRepository.findOne(file);

        return any(fileMetaData.getUsersFunctions(), hasPermissionOnTranslationData(actor, lab));
    }

    private Predicate<UserLabFileTranslationData> hasPermissionOnTranslationData(long actor, long lab) {
        return and(hasTranslationDataForLab(lab), hasTranslationDataForUser(actor));
    }

    @Override
    public boolean canRemoveAnyExperimentTranslationData(long actor, long experiment) {
        final ActiveExperiment activeExperiment = experimentRepository.findOne(experiment);
        final Optional<Lab> lab = getExperimentLab(activeExperiment);

        if (!lab.isPresent()) {
            return false;
        }

        final Long labId = lab.get().getId();

        if (!isTranslationEnabledForLab(labId)) {
            return false;
        }

        final List<? extends ExperimentFileTemplate<?, ?, ?>> data = activeExperiment.getRawFiles().getData();
        final FluentIterable<UserLabFileTranslationData> experimentFilesTranslations = FluentIterable.from(data).transformAndConcat(Functions.compose(USER_FUNCTIONS_FROM_META_DATA::apply, META_DATA_FROM_RAW_FILE::apply));
        return (isExperimentLabHead(activeExperiment, actor) || activeExperiment.getCreator().getId().equals(actor))
                && any(experimentFilesTranslations, hasPermissionOnTranslationData(actor, labId));
    }

    @Override
    public boolean canModifyAnnotationAttachment(long actor, long annotationAttachment) {
        final AnnotationAttachment attachment = annotationAttachmentRepository.findOne(annotationAttachment);
        return attachment.getOwner().getId().equals(actor);
    }

    private Predicate<UserLabFileTranslationData> hasTranslationDataForUser(final long actor) {
        return input -> input.getUser().getId().equals(actor) || input.getLab().getHead().getId().equals(actor);
    }

    private Predicate<UserLabFileTranslationData> hasTranslationDataForLab(final long lab) {
        return input -> input.getLab().getId().equals(lab);
    }

    private Predicate<AbstractFileMetaData> userIsLabHeadOfFile(final long actor) {
        return input -> input.getInstrument().getLab().getHead().getId().equals(actor);
    }

    @Override
    public Set<Long> getProjectsWithReadAccess(final long actor) {
        return userRepository.findOne(actor).getProjectsWithReadAccess().stream().map(EntityUtil.ENTITY_TO_ID::apply).collect(Collectors.toSet());
    }

    @Override
    public boolean hasRightsToPersistProteins(long actor, long db) {
        final long owner = proteinDatabaseRepository.findOne(db).getUser().getId();
        return actor == owner || isAdmin(actor);
    }

    @Override
    public boolean canModifyAnalysis(long actor, long analysis) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canModifyAnalysisTemplate(long actor, long analysisTemplateId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canCreatePostProcessingPipeline(long actor) {
        return isAdmin(actor);
    }

    @Override
    public boolean canImportMicroArrays(long actor, long lab) {
        return isFeatureEnabledForLab(MICROARRAYS, lab);
    }

    @Override
    public boolean shouldSearchResultsBePersistedInBlibFile(long run) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canUserReadProteinSearchAttachment(long actor, long attachmentId) {
        return isAdminOrProteinSearchAttachmentOwner(actor, attachmentId);
    }

    @Override
    public boolean canUserManageProteinSearchAttachment(long actor, long attachmentId) {
        return isAdminOrProteinSearchAttachmentOwner(actor, attachmentId);
    }

    @Override
    public boolean canUserManageProteinSearchAttachments(long actor) {
        return isAdmin(actor) ||
                userRepository
                        .findOne(actor)
                        .getLabs()
                        .stream()
                        .anyMatch(l -> canLabUseProteinIdSearch(l.getId()));
    }

    @Override
    public boolean isProteinSearchOwner(long actor, long proteinSearchId) {
        throw new UnsupportedOperationException();
    }

    private boolean isAdminOrProteinSearchAttachmentOwner(long actor, long attachmentId) {
        throw new UnsupportedOperationException();
    }

    private boolean isFeatureEnabledForLab(ApplicationFeature feature, long labId) {
        return featuresHelper.isEnabledForLab(feature, labId);
    }

    private boolean isBillingFeatureEnabledForLab(BillingFeature feature, long labId) {
        return billingFeaturesHelper.isFeatureEnabled(labId, feature);
    }

}
