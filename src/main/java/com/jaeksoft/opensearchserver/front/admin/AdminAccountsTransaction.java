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
import com.jaeksoft.opensearchserver.front.ServletTransaction;
import com.jaeksoft.opensearchserver.services.AccountsService;
import com.qwazr.utils.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.UUID;

public class AdminAccountsTransaction extends ServletTransaction {

	private final static String TEMPLATE = "admin/accounts.ftl";

	private final AccountsService accountsService;

	AdminAccountsTransaction(final Components components, final HttpServletRequest request,
			final HttpServletResponse response) throws NoSuchMethodException, IOException, URISyntaxException {
		super(components, request, response, false);
		accountsService = components.getAccountsService();
	}

	public void create() throws IOException, ServletException {
		final String accountName = request.getParameter("accountName");
		if (!StringUtils.isBlank(accountName)) {
			final UUID accountId = accountsService.createAccount(accountName);
			response.sendRedirect("/admin/accounts/" + accountId);
			return;
		}
		doGet();
	}

	@Override
	protected void doGet() throws IOException, ServletException {
		final int start = getRequestParameter("start", 0);
		request.setAttribute("accounts", accountsService.getAccounts(start, 20));
		doTemplate(TEMPLATE);
	}
}
