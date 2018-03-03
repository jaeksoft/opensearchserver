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

package com.jaeksoft.opensearchserver.front.admin;

import com.jaeksoft.opensearchserver.Components;
import com.jaeksoft.opensearchserver.front.Message;
import com.jaeksoft.opensearchserver.front.ServletTransaction;
import com.jaeksoft.opensearchserver.model.AccountRecord;
import com.jaeksoft.opensearchserver.model.ActiveStatus;
import com.jaeksoft.opensearchserver.model.PermissionLevel;
import com.jaeksoft.opensearchserver.model.PermissionRecord;
import com.jaeksoft.opensearchserver.model.UserRecord;
import com.jaeksoft.opensearchserver.services.AccountsService;
import com.jaeksoft.opensearchserver.services.PermissionsService;
import com.jaeksoft.opensearchserver.services.UsersService;
import com.qwazr.database.annotations.TableRequestResultRecords;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.NotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.UUID;

public class AdminUserTransaction extends ServletTransaction {

	private final static String TEMPLATE = "admin/user.ftl";

	private final UsersService usersService;
	private final AccountsService accountsService;
	private final PermissionsService permissionsService;

	private final UUID userId;

	AdminUserTransaction(final Components components, final UUID userId, final HttpServletRequest request,
			final HttpServletResponse response) throws NoSuchMethodException, IOException, URISyntaxException {
		super(components, request, response, false);
		this.usersService = components.getUsersService();
		this.accountsService = components.getAccountsService();
		this.permissionsService = components.getPermissionsService();
		this.userId = userId;
	}

	@Override
	protected String getTemplate() {
		return TEMPLATE;
	}

	public void updatePassword() {
		final String password1 = request.getParameter("password1");
		final String password2 = request.getParameter("password2");
		usersService.resetPassword(userId, password1);
		if (!password1.equals(password2))
			addMessage(Message.Css.danger, "Error", "The passwords do not match");
		else
			addMessage(Message.Css.success, "Password updated!", null);
	}

	private AccountRecord getExistingAccountByName() {
		final String accountName = request.getParameter("accountName");
		final AccountRecord accountRecord = accountsService.getAccountByName(accountName);
		if (accountRecord == null)
			throw new NotAcceptableException("Account not found: " + accountName);
		return accountRecord;
	}

	public void setPermission() {
		final AccountRecord accountRecord = getExistingAccountByName();
		final String levelName = request.getParameter("level");
		final PermissionLevel level = PermissionLevel.resolve(levelName);
		if (level == null)
			throw new NotAcceptableException("Unknown level: " + levelName);
		if (permissionsService.setPermission(userId, accountRecord.getId(), level))
			addMessage(Message.Css.success, "Permission added", null);
	}

	private AccountRecord getExistingAccountById() {
		final String accountId = request.getParameter("accountId");
		final AccountRecord accountRecord = accountsService.getAccountById(UUID.fromString(accountId));
		if (accountRecord == null)
			throw new NotAcceptableException("Account not found: " + accountId);
		return accountRecord;
	}

	public void removePermission() {
		final AccountRecord accountRecord = getExistingAccountById();
		if (permissionsService.removePermission(userId, accountRecord.getId()))
			addMessage(Message.Css.success, "Permission removed", null);
	}

	public void updateStatus() {
		final ActiveStatus status = ActiveStatus.resolve(request.getParameter("status"));
		if (usersService.updateStatus(userId, status))
			addMessage(Message.Css.success, "Status updated", "Status set to " + status);
	}

	@Override
	protected void doGet() throws IOException, ServletException {
		final UserRecord userRecord = usersService.getUserById(userId);
		if (userRecord == null)
			throw new NotFoundException("User not found");
		final TableRequestResultRecords<PermissionRecord> permissions =
				permissionsService.getPermissionsByUser(userRecord.getId(), 0, 1000);
		final Map<AccountRecord, PermissionRecord> accounts = accountsService.getAccountsByIds(permissions);
		request.setAttribute("accounts", accounts);
		request.setAttribute("userRecord", userRecord);
		super.doGet();
	}

}
