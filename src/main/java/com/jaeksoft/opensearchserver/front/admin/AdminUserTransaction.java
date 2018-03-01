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
import com.jaeksoft.opensearchserver.model.UserRecord;
import com.jaeksoft.opensearchserver.services.AccountsService;
import com.jaeksoft.opensearchserver.services.PermissionsService;
import com.jaeksoft.opensearchserver.services.UsersService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.NotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.UUID;

public class AdminUserTransaction extends ServletTransaction {

	private final static String TEMPLATE = "admin/user.ftl";

	private final UsersService usersService;
	private final AccountsService accountsService;
	private final PermissionsService permissionsService;

	private final UUID userId;

	public AdminUserTransaction(final Components components, final UUID userId, final HttpServletRequest request,
			final HttpServletResponse response) throws NoSuchMethodException, IOException, URISyntaxException {
		super(components, request, response, false);
		this.usersService = components.getUsersService();
		this.accountsService = components.getAccountsService();
		this.permissionsService = components.getPermissionsService();
		this.userId = userId;
	}

	public void updatePassword() throws IOException, ServletException {
		final String password1 = request.getParameter("password1");
		final String password2 = request.getParameter("password2");
		usersService.resetPassword(userId, password1);
		if (!password1.equals(password2))
			throw new NotAcceptableException("The passwords do not match");
		addMessage(Message.Css.success, "Password updated!", null);
		doGet();
	}

	private AccountRecord getExistingRecordByName() {
		final String accountName = request.getParameter("account");
		final AccountRecord accountRecord = accountsService.getAccountByName(accountName);
		if (accountRecord == null)
			throw new NotAcceptableException("Account not found: " + accountName);
		return accountRecord;
	}

	public void setPermission() throws IOException, ServletException {
		final AccountRecord accountRecord = getExistingRecordByName();
		final String levelName = request.getParameter("level");
		final PermissionLevel level = PermissionLevel.resolve(levelName);
		if (level == null)
			throw new NotAcceptableException("Unknown level: " + levelName);
		if (permissionsService.setPermission(userId, accountRecord.getId(), level))
			addMessage(Message.Css.success, "Permission added", null);
		doGet();
	}

	public void removePermission() throws IOException, ServletException {
		final AccountRecord accountRecord = getExistingRecordByName();
		if (permissionsService.removePermission(userId, accountRecord.getId()))
			addMessage(Message.Css.success, "Permission removed", null);
		doGet();
	}

	public void updateStatus() throws IOException, ServletException {
		final ActiveStatus status = ActiveStatus.resolve(request.getParameter("status"));
		if (usersService.updateStatus(userId, status))
			addMessage(Message.Css.success, "Status updated", "Status set to " + status);
		doGet();
	}

	@Override
	protected void doGet() throws IOException, ServletException {
		final UserRecord userRecord = usersService.getUserById(userId);
		if (userRecord == null)
			throw new NotFoundException("User not found");
		request.setAttribute("userRecord", userRecord);
		doTemplate(TEMPLATE);
	}

}
