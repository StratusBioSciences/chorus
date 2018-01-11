/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.model.internal.read;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.infoclinika.mssharing.model.PaginationItems;
import com.infoclinika.mssharing.model.internal.RuleValidator;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveExperiment;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveFileMetaData;
import com.infoclinika.mssharing.model.internal.repository.ExperimentRepository;
import com.infoclinika.mssharing.model.internal.repository.FileMetaDataRepository;
import com.infoclinika.mssharing.model.internal.repository.NewsRepository;
import com.infoclinika.mssharing.model.read.AdministrationToolsReader;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.model.PagedItem;
import com.infoclinika.mssharing.platform.model.PagedItemInfo;
import org.apache.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static com.google.common.collect.FluentIterable.from;
import static com.infoclinika.mssharing.model.internal.read.Transformers.NEWS_BY_DATE;
import static com.infoclinika.mssharing.model.internal.read.Transformers.PagedItemsTransformer;
import static com.infoclinika.mssharing.model.internal.read.Transformers.PagedItemsTransformer.toFilterQuery;
import static com.infoclinika.mssharing.model.internal.read.Transformers.TO_NEWS_LINE;

/**
 * @author Stanislav Kurilin
 */
@Service
public class AdministrationToolsReaderImpl implements AdministrationToolsReader {

    private static final Logger LOG = Logger.getLogger(AdministrationToolsReaderImpl.class);

    @Inject
    private NewsRepository newsRepository;
    @Inject
    private RuleValidator ruleValidator;
    @Inject
    private ExperimentRepository experimentRepository;
    @PersistenceContext(unitName = "mssharing")
    private EntityManager em;
    @Inject
    private FileMetaDataRepository fileMetaDataRepository;
    @Inject
    private Transformers transformers;
    @Inject
    private PagedItemsTransformer pagedItemsTransformer;

    @Override
    public ImmutableSortedSet<NewsLine> readNewsItems(long actor) {
        return from(newsRepository.findAll())
                .transform(TO_NEWS_LINE)
                .toSortedSet(NEWS_BY_DATE);
    }

    @Override
    public PagedItem<FileTranslationShortItem> readFileTranslationStatuses(long actor, PagedItemInfo pagedItem) {

        if (!ruleValidator.hasAdminRights(actor)) {
            throw new AccessDenied("User cannot read file translation statuses");
        }

        final PageRequest pageRequest = pagedItemsTransformer.toPageRequest(ActiveFileMetaData.class, pagedItem);
        final Page<ActiveFileMetaData> files = fileMetaDataRepository.findAllWithFilter(toFilterQuery((PaginationItems.PagedItemInfo) pagedItem), pageRequest);

        return new PagedItem<>(
                files.getTotalPages(),
                files.getTotalElements(),
                files.getNumber(),
                files.getNumberOfElements(), from(files).transform(transformers.perFileTranslationTransformer).toList());
    }

}
