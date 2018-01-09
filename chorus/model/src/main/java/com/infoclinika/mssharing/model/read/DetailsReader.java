/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.model.read;

import com.infoclinika.msdata.image.GridType;
import com.infoclinika.mssharing.model.api.MSFunctionMassAnalyzerType;
import com.infoclinika.mssharing.model.api.MSFunctionType;
import com.infoclinika.mssharing.model.api.MSResolutionType;
import com.infoclinika.mssharing.model.helper.LockMzItem;
import com.infoclinika.mssharing.model.read.dto.details.*;
import com.infoclinika.mssharing.model.write.ExperimentCategory;
import com.infoclinika.mssharing.platform.model.read.AttachmentsReaderTemplate;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate.GroupItemTemplate;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate.LabItemTemplateDetailed;
import com.infoclinika.mssharing.platform.model.read.RequestsDetailsReaderTemplate;
import com.infoclinika.tasks.api.workflow.model.MSExperimentResolutionType;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * @author Stanislav Kurilin
 */
public interface DetailsReader extends DetailsReaderTemplate<FileItem, ExperimentItem, ProjectItem, InstrumentItem, LabItemTemplateDetailed,
        GroupItemTemplate> {

    InstrumentCreationItem readInstrumentCreation(long actor, long request);

    LabItemTemplate readLabRequestDetails(long actor, long lab);

    FileItem readFileDetailsWithConditions(long actor, long fileId, long experimentId);

    List<ShortFileWithConditions> readFilesAndConditionsForExperiment(long actor, long experimentId);

    com.google.common.base.Optional<MSExperimentResolutionType> getExperimentResolutionType(long experiment, String ms1FunctionName, String ms2FunctionName);

    List<ShortExperimentFileItem> readFilesInOtherExperiments(long actor, long experiment);

    AttachmentsReaderTemplate.AttachmentItem readExperimentAnnotationAttachment(long actor, long experiment);

    AttachmentsReaderTemplate.AttachmentItem readAnnotationAttachmentDetails(long actor, long attachmentId);

    AttachmentItem readProcessingRunPluginAttachment(long actor, long attachmentId);

    ProteinSearchAttachmentItem readProteinSearchAttachment(long actor, long attachment);

    class ProteinSearchAttachmentItem extends AttachmentItem {
        public final String prefix;
        public final String searchPrefix;

        public ProteinSearchAttachmentItem(long id, String name, long sizeInBytes, Date uploadDate, long ownerId, String prefix, String searchPrefix) {
            super(id, name, sizeInBytes, uploadDate, ownerId);
            this.prefix = prefix;
            this.searchPrefix = searchPrefix;
        }
    }

    public class FileReference {
        public final String bucket;
        public final String key;

        public FileReference(String bucket, String key) {
            this.bucket = bucket;
            this.key = key;
        }
    }

    final class MSFunctionDetails {
        public final String name;
        public final String translatedPath;
        public final MSFunctionType type;
        public final MSResolutionType resolution;
        public final MSFunctionMassAnalyzerType msFunctionMassAnalyzerType;
        public final String fileName;
        public final long fileId;
        public final boolean dia;

        public MSFunctionDetails(
                String name,
                String translatedPath,
                MSFunctionType type,
                MSResolutionType resolution,
                MSFunctionMassAnalyzerType msFunctionMassAnalyzerType,
                String fileName,
                long fileId,
                boolean dia
        ) {
            this.name = name;
            this.translatedPath = translatedPath;
            this.type = type;
            this.resolution = resolution;
            this.msFunctionMassAnalyzerType = msFunctionMassAnalyzerType;
            this.fileName = fileName;
            this.fileId = fileId;
            this.dia = dia;
        }
    }

    final class MSFunctions {
        public final Set<MSFunctionDetails> functionDetails = new HashSet<>();
    }

    final class MsFunctionItemDetails {
        public final String functionName;

        public MsFunctionItemDetails(String functionName) {
            this.functionName = functionName;
        }
    }


    class InstrumentCreationItem extends RequestsDetailsReaderTemplate.InstrumentCreationItemTemplate {

        public final String hplc;
        public final List<LockMzItem> lockMasses;
        public final boolean autoTranslate;

        public InstrumentCreationItem(RequestsDetailsReaderTemplate.InstrumentCreationItemTemplate other, String hplc, List<LockMzItem> lockMasses, boolean autoTranslate) {
            super(other);
            this.hplc = hplc;
            this.lockMasses = lockMasses;
            this.autoTranslate = autoTranslate;
        }
    }

    class ExperimentShortInfoDetailed extends ExperimentShortInfo {

        public final ExperimentCategory category;

        public ExperimentShortInfoDetailed(ExperimentShortInfo info, ExperimentCategory category) {
            super(info.id, info.labName, info.name, info.description, info.projectName,
                    info.species, info.files, info.attachments, info.ownerEmail);
            this.category = category;
        }
    }
}
