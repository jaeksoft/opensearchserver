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
import com.jaeksoft.opensearchserver.model.UserRecord;
import com.jaeksoft.opensearchserver.services.UsersService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.NotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

public class AdminUserTransaction extends ServletTransaction {

	private final static String TEMPLATE = "admin/user.ftl";

	private final UsersService usersService;

	private final String userId;

	public AdminUserTransaction(final Components components, final String userId, final HttpServletRequest request,
			final HttpServletResponse response) throws NoSuchMethodException, IOException, URISyntaxException {
		super(components, request, response, false);
		this.usersService = components.getUsersService();
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

	public void addAccount() throws IOException, ServletException {
		final String account = request.getParameter("account");
		if (usersService.addAccount(userId, account))
			addMessage(Message.Css.success, "Account added", account);
		doGet();
	}

	public void removeAccount() throws IOException, ServletException {
		final String account = request.getParameter("account");
		if (usersService.removeAccount(userId, account))
			addMessage(Message.Css.success, "Account removed", account);
		doGet();
	}

	public void updateStatus() throws IOException, ServletException {
		final int status = getRequestParameter("status", 0);
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
