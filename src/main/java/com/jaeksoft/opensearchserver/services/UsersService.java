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

import com.jaeksoft.opensearchserver.model.UserRecord;
import com.qwazr.database.TableServiceInterface;
import com.qwazr.database.annotations.AnnotatedTableService;
import com.qwazr.database.annotations.TableRequestResultRecords;
import com.qwazr.database.model.TableQuery;
import com.qwazr.database.model.TableRequest;
import com.qwazr.utils.HashUtils;
import com.qwazr.utils.LoggerUtils;
import com.qwazr.utils.StringUtils;
import com.qwazr.utils.concurrent.ThreadUtils;
import io.undertow.security.idm.Account;
import io.undertow.security.idm.Credential;
import io.undertow.security.idm.IdentityManager;
import io.undertow.security.idm.PasswordCredential;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UsersService implements IdentityManager {

	private final static Logger LOGGER = LoggerUtils.getLogger(UsersService.class);

	private final ConfigService configService;

	private AnnotatedTableService<UserRecord> users;

	public UsersService(final ConfigService configService, final TableServiceInterface tableServiceInterface)
			throws NoSuchMethodException, URISyntaxException {
		this.configService = configService;
		users = new AnnotatedTableService<>(tableServiceInterface, UserRecord.class);
		users.createUpdateTable();
		users.createUpdateFields();
	}

	@Override
	public Account verify(Account account) {
		try {
			return new UserAccount(getUserById(((UserAccount) account).userRecord.getId()));
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Account verify failed for " + account, e);
			return null;
		}
	}

	public Integer getUserCount() {
		return users.getTableStatus().getNumRows();
	}

	/**
	 * User login
	 *
	 * @param email         the email address of the user
	 * @param clearPassword the clear password
	 * @return
	 */
	private UserRecord login(final String email, final String clearPassword) {
		if (email == null || clearPassword == null)
			return null;
		final UserRecord user = getUserByEmail(email);
		if (user != null && user.isActivated() && user.matchPassword(configService.getApplicationSalt(), clearPassword))
			return user;
		ThreadUtils.sleep(2, TimeUnit.SECONDS);
		throw new NotAuthorizedException("Authentication failure");
	}

	@Override
	public Account verify(final String email, final Credential credential) {
		if (StringUtils.isBlank(email) || credential == null)
			return null;
		if (!(credential instanceof PasswordCredential))
			return null;
		final String password = new String(((PasswordCredential) credential).getPassword());
		if (StringUtils.isBlank(password))
			return null;
		try {
			return new UserAccount(login(email, password));
		} catch (NotAuthorizedException e) {
			return null;
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Account verify failed for " + email, e);
			return null;
		}
	}

	public UserRecord verify(final String email, final String clearPassword) {
		final UserAccount account = (UserAccount) verify(email, new PasswordCredential(clearPassword.toCharArray()));
		return account == null ? null : account.getPrincipal();
	}

	@Override
	public Account verify(Credential credential) {
		return null;
	}

	public UserRecord getUserById(final String id) {
		try {
			return users.getRow(id, UserRecord.COLUMNS_SET);
		} catch (IOException | ReflectiveOperationException e) {
			throw new InternalServerErrorException("Cannot get user by id", e);
		}
	}

	public UserRecord getUserByEmail(final String email) {
		try {
			final TableRequestResultRecords<UserRecord> result = users.queryRows(TableRequest.from(0, 1)
					.column(UserRecord.COLUMNS)
					.query(new TableQuery.StringTerm("email", email))
					.build());
			return result != null && result.count != null && result.count == 1 ? result.records.get(0) : null;
		} catch (IOException | ReflectiveOperationException e) {
			throw new InternalServerErrorException("Cannot get user by email", e);
		}
	}

	public TableRequestResultRecords<UserRecord> getUsers(final int start, final int rows) {
		try {
			return users.queryRows(TableRequest.from(start, rows).column(UserRecord.COLUMNS).build());
		} catch (IOException | ReflectiveOperationException e) {
			throw new InternalServerErrorException("Cannot get user list", e);
		}
	}

	public synchronized String createUser(final String userEmail) {
		final UserRecord userRecord = getUserByEmail(userEmail);
		if (userRecord != null)
			return userRecord.getId();
		final String userId = HashUtils.newTimeBasedUUID().toString();
		users.upsertRow(userId, UserRecord.of().email(userEmail).build());
		return userId;
	}

	public final static String PASSWORD_STRENGTH_MESSAGE =
			"The password must contains at least 8 characters, one digit, one lowercase character, and one uppercase character.";

	public static boolean checkPasswordStrength(final String password) {
		return (!StringUtils.isBlank(password) && !StringUtils.isAnyBlank(password) && password.length() >= 8 &&
				StringUtils.anyLowercase(password) && StringUtils.anyUpperCase(password) &&
				StringUtils.anyDigit(password));
	}

	private UserRecord getExistingUser(String userId) {
		final UserRecord user = getUserById(userId);
		if (user == null)
			throw new NotFoundException("User not found: " + userId);
		return user;
	}

	public void resetPassword(final String userId, final String newPassword) {
		final UserRecord user = getExistingUser(userId);
		if (!checkPasswordStrength(newPassword))
			throw new NotAcceptableException(PASSWORD_STRENGTH_MESSAGE);
		final UserRecord userRecord = UserRecord.of(user).password(configService.applicationSalt, newPassword).build();
		users.upsertRow(userId, userRecord);
	}

	public boolean updateStatus(final String userId, final int status) {
		final UserRecord user = getExistingUser(userId);
		if (user.getStatus() != null && user.getStatus() == status)
			return false;
		users.upsertRow(userId, UserRecord.of(user).status(status).build());
		return true;
	}

	public boolean addAccount(final String userId, final String accountId) {
		final UserRecord user = getExistingUser(userId);
		if (user.hasAccount(accountId))
			return false;
		users.upsertRow(userId, UserRecord.of(user).addAccount(accountId).build());
		return true;
	}

	public boolean removeAccount(final String userId, final String accountId) {
		final UserRecord user = getExistingUser(userId);
		if (!user.hasAccount(accountId))
			return false;
		users.upsertRow(userId, UserRecord.of(user).removeAccount(accountId).build());
		return true;
	}

	public class UserAccount implements Account {

		private final UserRecord userRecord;

		private UserAccount(final UserRecord userRecord) {
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
