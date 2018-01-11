/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.model.read;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import com.infoclinika.mssharing.platform.model.PagedItem;

import com.infoclinika.mssharing.platform.model.PagedItemInfo;

/**
 * Provides information that could be read by admin.
 *
 * @author Stanislav Kurilin
 */
@Transactional(readOnly = true)
public interface AdministrationToolsReader {

    ImmutableSortedSet<NewsLine> readNewsItems(long actor);

    PagedItem<FileTranslationShortItem> readFileTranslationStatuses(long actor, PagedItemInfo pagedItem);

    /**
     *
     * @return value from 0..100
     */

    class NewsLine {
        public final long id;
        public final String title;
        public final String creatorEmail;
        public final Date dateCreated;

        public NewsLine(long id, String title, String creatorEmail, Date dateCreated) {
            this.id = id;
            this.title = title;
            this.creatorEmail = creatorEmail;
            this.dateCreated = dateCreated;
        }
    }

    class  FileTranslationShortItem  {
        public final long id;
        public final String name;
        public final String labName;
        public final String owner;
        public final String instrumentName;
        public final boolean translationSubmitted;
        public final String translationError;
        public final boolean translationResultsAvailable;
        public final boolean metadataAvailable;
        public final boolean usedInExperiment;

        public FileTranslationShortItem(long id, String name, String labName, String owner, String instrumentName,
                                        boolean translationSubmitted, String translationError,
                                        boolean translationResultsAvailable, boolean metadataAvailable,
                                        boolean usedInExperiment) {
            this.id = id;
            this.name = name;
            this.labName = labName;
            this.owner = owner;
            this.instrumentName = instrumentName;
            this.translationSubmitted = translationSubmitted;
            this.translationError = translationError;
            this.translationResultsAvailable = translationResultsAvailable;
            this.metadataAvailable = metadataAvailable;
            this.usedInExperiment = usedInExperiment;
        }
    }
}
