package com.infoclinika.sso.model.internal.write.application;

import com.infoclinika.sso.model.ApplicationType;
import com.infoclinika.sso.model.internal.entity.User;

/**
 * @author andrii.loboda
 */
public interface ApplicationTypeManagement {
    User findByUsername(String username);

    /**
     * @throws com.infoclinika.sso.model.exception.AccountIsAlreadyLinkedException
     */
    User updateDetailsAndPersist(String username, String userSecretKey, User targetUser);

    ApplicationType getApplicationType();
}
