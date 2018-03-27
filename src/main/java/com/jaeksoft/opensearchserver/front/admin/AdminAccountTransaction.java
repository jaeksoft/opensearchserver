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
import java.io.IOException;
import java.util.Map;

public class AdminAccountTransaction extends ServletTransaction {

	private final static String TEMPLATE = "admin/account.ftl";

	private final UsersService usersService;
	private final AccountsService accountsService;
	private final PermissionsService permissionsService;

	private final AccountRecord accountRecord;

	AdminAccountTransaction(final Components components, final AccountRecord accountRecord,
			final HttpServletRequest request, final HttpServletResponse response) {
		super(components.getFreemarkerTool(), request, response, false);
		this.usersService = components.getUsersService();
		this.accountsService = components.getAccountsService();
		this.permissionsService = components.getPermissionsService();
		this.accountRecord = accountRecord;
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
		if (permissionsService.setPermission(userRecord.getId(), accountRecord.getId(), level))
			addMessage(Message.Css.success, "Permission added", null);
	}

	public void removePermission() {
		final UserRecord userRecord = getExistingRecordByEmail();
		if (permissionsService.removePermission(userRecord.getId(), accountRecord.getId()))
			addMessage(Message.Css.success, "Permission removed", null);
	}

	public void updateStatus() {
		final ActiveStatus status = ActiveStatus.resolve(request.getParameter("status"));
		if (accountsService.update(accountRecord.getId(), builder -> builder.status(status)))
			addMessage(Message.Css.success, "Status updated", "Status set to " + status);
		else
			addMessage(Message.Css.warning, "Nothing to update", null);
	}

	public String updateName() {
		final String accountName = request.getParameter("accountName");
		if (accountsService.update(accountRecord.getId(), builder -> builder.name(accountName)))
			addMessage(Message.Css.success, "Name updated", "Name set to " + accountName);
		else
			addMessage(Message.Css.warning, "Nothing to update", null);
		return "/admin/accounts/" + accountsService.getExistingAccount(accountRecord.getId()).getName();
	}

	public void setLimits() {
		final int crawlNumberLimit = getRequestParameter("crawlNumberLimit", 0, 0, null);
		final int tasksNumberLimit = getRequestParameter("tasksNumberLimit", 0, 0, null);
		final int indexNumberLimit = getRequestParameter("indexNumberLimit", 0, 0, null);
		final int recordNumberLimit = getRequestParameter("recordNumberLimit", 0, 0, null);
		final int storageLimit = getRequestParameter("storageLimit", 0, 0, null) * 1024 * 1024;
		if (accountsService.update(accountRecord.getId(), b -> b.crawlNumberLimit(crawlNumberLimit)
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
		final TableRequestResultRecords<PermissionRecord> permissions =
				permissionsService.getPermissionsByAccount(accountRecord.getId(), 0, 1000);
		final Map<UserRecord, PermissionRecord> users = usersService.getUsersByIds(permissions);
		request.setAttribute("accountRecord", accountRecord);
		request.setAttribute("users", users);
		super.doGet();
	}

}
