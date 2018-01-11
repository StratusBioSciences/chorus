package com.infoclinika.mssharing.model.internal.entity;

import com.infoclinika.mssharing.model.internal.entity.restorable.TranslationStatus;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;
import java.util.Collection;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

/**
 * @author Herman Zamula
 */
@Entity
@Table(name = "user_translation_data")
public class UserLabFileTranslationData extends AbstractPersistable<Long> {

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    @Fetch(FetchMode.JOIN)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "lab_id")
    @Fetch(FetchMode.JOIN)
    private Lab lab;

    //Real file data store path for translation for this user and lab
    @Column(name = "temp_file_content_id")
    private String tempFileContentId;

    @Column(name = "to_temp_folder")
    private boolean toTempFolder;

    @Lob
    @Column(name = "translation_error", insertable = false, updatable = false)
    private String translationError;

    @Embedded
    private TranslationStatus translationStatus = new TranslationStatus();

    public UserLabFileTranslationData(User user, Lab lab) {
        this.user = user;
        this.lab = lab;
    }

    protected UserLabFileTranslationData() {
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Lab getLab() {
        return lab;
    }

    public void setLab(Lab lab) {
        this.lab = lab;
    }

    public String getTempFileContentId() {
        return tempFileContentId;
    }

    public void setTempFileContentId(String tempFilePath) {
        this.tempFileContentId = tempFilePath;
    }

    public boolean isToTempFolder() {
        return toTempFolder;
    }

    public void setToTempFolder(boolean toTempFolder) {
        this.toTempFolder = toTempFolder;
    }

    public String getTranslationError() {
        return translationError;
    }

    public void setTranslationError(String translationError) {
        this.translationError = translationError;
    }

    public TranslationStatus getTranslationStatus() {
        return translationStatus;
    }

    public void setTranslationStatus(TranslationStatus translationStatus) {
        this.translationStatus = translationStatus;
    }
}
