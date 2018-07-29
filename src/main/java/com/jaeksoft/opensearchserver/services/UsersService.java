/*
 * Copyright 2017-2018 Emmanuel Keller / Jaeksoft
 *  <p>
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  <p>
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  <p>
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.jaeksoft.opensearchserver.services;

import com.jaeksoft.opensearchserver.model.ActiveStatus;
import com.jaeksoft.opensearchserver.model.PermissionRecord;
import com.jaeksoft.opensearchserver.model.UserRecord;
import com.qwazr.utils.StringUtils;
import io.undertow.security.idm.Account;
import io.undertow.security.idm.IdentityManager;

import javax.ws.rs.NotSupportedException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * This interface describe a user service. This service is in charge of handling signing.
 */
public interface UsersService extends IdentityManager {

    /**
     * Set a new password.
     *
     * @param userId        the ID of the user to update
     * @param clearPassword the clear password
     */
    default void setPassword(UUID userId, String clearPassword) {
        throw new NotSupportedException("This user implementation does not support password changes.");
    }

    /**
     * @param userId the ID of the user
     * @param status the status of the user
     * @return true if the status has been updated
     */
    default boolean updateStatus(UUID userId, ActiveStatus status) {
        throw new NotSupportedException("This user implementation does not support status changes.");
    }

    /**
     * Get a user record by its id
     *
     * @param userId the ID of the user
     * @return a User record
     */
    default UserRecord getUserById(UUID userId) {
        throw new NotSupportedException("This user implementation does not support user retrieval.");
    }

    /**
     * Get a user record by its email address
     *
     * @param userEmail the ID of the user
     * @return a User record
     */
    default UserRecord getUserByEmail(String userEmail) {
        throw new NotSupportedException("This user implementation does not support user retrieval.");
    }

    /**
     * Creates a new user and returns its ID.
     *
     * @param userEmail the email of the new user
     * @return a new user ID
     */
    default UUID createUser(String userEmail) {
        throw new NotSupportedException("This user implementation does not support user creation.");
    }

    /**
     * Return a user list with paging feature.
     *
     * @param start
     * @param rows
     * @param collector
     * @return the total number of users
     */
    default int getUsers(final int start, final int rows, Consumer<UserRecord> collector) {
        throw new NotSupportedException("This user implementation does not support user list retrieval.");
    }

    default Map<UserRecord, PermissionRecord> getUsersByIds(List<PermissionRecord> permissions) {
        throw new NotSupportedException("This user implementation does not support user permission retrieval.");
    }

    boolean isSingleSignOn();

    default String getSingleSignOnRedirectUrl() {
        throw new NotSupportedException("This user implementation does not support SingleSignOn redirection.");
    }

    String PASSWORD_STRENGTH_MESSAGE =
        "The password must contains at least 8 characters, one digit, one lowercase character, and one uppercase character.";

    static boolean checkPasswordStrength(final String password) {
        return (!StringUtils.isBlank(password) && !StringUtils.isAnyBlank(password) && password.length() >= 8 &&
            StringUtils.anyLowercase(password) && StringUtils.anyUpperCase(password) && StringUtils.anyDigit(password));
    }

    class UserAccount implements Account {

        private final UserRecord userRecord;

        UserAccount(final UserRecord userRecord) {
            this.userRecord = userRecord;
        }

        @Override
        public UserRecord getPrincipal() {
            return userRecord;
        }

        @Override
        public Set<String> getRoles() {
            return Collections.emptySet();
        }

    }
}
