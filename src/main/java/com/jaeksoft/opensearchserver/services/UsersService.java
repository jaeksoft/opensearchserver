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
import com.qwazr.utils.LoggerUtils;
import com.qwazr.utils.StringUtils;
import com.qwazr.utils.concurrent.ThreadUtils;
import io.undertow.security.idm.Account;
import io.undertow.security.idm.Credential;
import io.undertow.security.idm.IdentityManager;
import io.undertow.security.idm.PasswordCredential;

import javax.ws.rs.NotAuthorizedException;
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
		} catch (IOException | ReflectiveOperationException e) {
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
	 * @throws IOException
	 * @throws ReflectiveOperationException
	 */
	private UserRecord login(final String email, final String clearPassword)
			throws IOException, ReflectiveOperationException {
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
		} catch (IOException | ReflectiveOperationException e) {
			LOGGER.log(Level.SEVERE, "Account verify failed for " + email, e);
			return null;
		}
	}

	public UserRecord verify(String email, String clearPassword) {
		final UserAccount account = (UserAccount) verify(email, new PasswordCredential(clearPassword.toCharArray()));
		return account == null ? null : account.getPrincipal();
	}

	@Override
	public Account verify(Credential credential) {
		return null;
	}

	private UserRecord getUserById(String id) throws IOException, ReflectiveOperationException {
		return users.getRow(id, UserRecord.COLUMNS_SET);
	}

	private UserRecord getUserByEmail(String email) throws IOException, ReflectiveOperationException {
		final TableRequestResultRecords<UserRecord> result = users.queryRows(TableRequest.from(0, 1)
				.column(UserRecord.COLUMNS)
				.query(new TableQuery.StringTerm("email", email))
				.build());
		return result != null && result.count != null && result.count == 1 ? result.records.get(0) : null;
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
