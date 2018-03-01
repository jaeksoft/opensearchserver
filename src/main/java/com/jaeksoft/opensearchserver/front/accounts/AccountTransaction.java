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

package com.jaeksoft.opensearchserver.front.accounts;

import com.jaeksoft.opensearchserver.Components;
import com.jaeksoft.opensearchserver.front.ServletTransaction;
import com.jaeksoft.opensearchserver.services.PermissionsService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.NotAllowedException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.UUID;

public class AccountTransaction extends ServletTransaction {

	private final static String TEMPLATE = "accounts/account.ftl";

	private final PermissionsService permissions;
	private final UUID accountId;

	AccountTransaction(final Components components, final UUID accountId, final HttpServletRequest request,
			final HttpServletResponse response) throws NoSuchMethodException, IOException, URISyntaxException {
		super(components, request, response, true);
		this.accountId = accountId;
		this.permissions = components.getPermissionsService();
	}

	@Override
	protected void doGet() throws IOException, ServletException {
		if (permissions.getPermission(userRecord.getId(), accountId) == null)
			throw new NotAllowedException("Not allowed");
		request.setAttribute("accountId", accountId);
		doTemplate(TEMPLATE);
	}

}
