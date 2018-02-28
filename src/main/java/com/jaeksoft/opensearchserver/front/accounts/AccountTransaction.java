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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.NotAllowedException;
import java.io.IOException;

public class AccountTransaction extends ServletTransaction {

	private final static String TEMPLATE = "accounts/account.ftl";

	private final String accountId;

	AccountTransaction(final Components components, final String accountId, final HttpServletRequest request,
			final HttpServletResponse response) {
		super(components, request, response, true);
		requireLoggedUser();
		this.accountId = accountId;
	}

	@Override
	protected void doGet() throws IOException, ServletException {
		if (!isUserAccount(accountId))
			throw new NotAllowedException("Not allowed");
		request.setAttribute("accountId", accountId);
		doTemplate(TEMPLATE);
	}

}
