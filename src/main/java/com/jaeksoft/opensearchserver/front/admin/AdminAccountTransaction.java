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

public class AdminAccountTransaction extends ServletTransaction {

	private final static String TEMPLATE = "admin/account.ftl";

	private final UsersService usersService;
	private final AccountsService accountsService;
	private final PermissionsService permissionsService;

	private final UUID accountId;

	AdminAccountTransaction(final Components components, final UUID accountId, final HttpServletRequest request,
			final HttpServletResponse response) throws NoSuchMethodException, IOException, URISyntaxException {
		super(components, request, response, false);
		this.usersService = components.getUsersService();
		this.accountsService = components.getAccountsService();
		this.permissionsService = components.getPermissionsService();
		this.accountId = accountId;
	}

	@Override
	protected String getTemplate() {
		return TEMPLATE;
	}

	private UserRecord getExistingRecordByEmail() {
		final String userEmail = request.getParameter("userEmail");
		final UserRecord userRecord = usersService.getUserByEmail(userEmail);
		if (userEmail == null)
			throw new NotAcceptableException("User not found: " + userEmail);
		return userRecord;
	}

	public void setPermission() {
		final UserRecord userRecord = getExistingRecordByEmail();
		final String levelName = request.getParameter("level");
		final PermissionLevel level = PermissionLevel.resolve(levelName);
		if (level == null)
			throw new NotAcceptableException("Unknown level: " + levelName);
		if (permissionsService.setPermission(userRecord.getId(), accountId, level))
			addMessage(Message.Css.success, "Permission added", null);
	}

	public void removePermission() {
		final UserRecord userRecord = getExistingRecordByEmail();
		if (permissionsService.removePermission(userRecord.getId(), accountId))
			addMessage(Message.Css.success, "Permission removed", null);
	}

	public void updateStatus() {
		final ActiveStatus status = ActiveStatus.resolve(request.getParameter("status"));
		if (accountsService.update(accountId, builder -> builder.status(status)))
			addMessage(Message.Css.success, "Status updated", "Status set to " + status);
		else
			addMessage(Message.Css.warning, "Nothing to update", null);
	}

	public void updateName() {
		final String accountName = request.getParameter("accountName");
		if (accountsService.update(accountId, builder -> builder.name(accountName)))
			addMessage(Message.Css.success, "Name updated", "Name set to " + accountName);
		else
			addMessage(Message.Css.warning, "Nothing to update", null);
	}

	public void setLimits() {
		final int crawlNumberLimit = getRequestParameter("crawlNumberLimit", 0);
		final int tasksNumberLimit = getRequestParameter("tasksNumberLimit", 0);
		final int indexNumberLimit = getRequestParameter("indexNumberLimit", 0);
		final int recordNumberLimit = getRequestParameter("recordNumberLimit", 0);
		final int storageLimit = getRequestParameter("storageLimit", 0) * 1024 * 1024;
		if (accountsService.update(accountId, b -> b.crawlNumberLimit(crawlNumberLimit)
				.tasksNumberLimit(tasksNumberLimit)
				.indexNumberLimit(indexNumberLimit)
				.recordNumberLimit(recordNumberLimit)
				.storageLimit(storageLimit)))
			addMessage(Message.Css.success, "Limits updated", null);
		else
			addMessage(Message.Css.warning, "Nothing to update", null);
	}

	@Override
	protected void doGet() throws IOException, ServletException {
		final AccountRecord accountRecord = accountsService.getAccountById(accountId);
		if (accountId == null)
			throw new NotFoundException("Account not found");
		request.setAttribute("accountRecord", accountRecord);
		super.doGet();
	}

}
