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

package com.jaeksoft.opensearchserver.front.accounts.indexes;

import com.jaeksoft.opensearchserver.Components;
import com.jaeksoft.opensearchserver.front.ServletTransaction;
import com.jaeksoft.opensearchserver.services.IndexesService;
import com.qwazr.utils.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;

public class IndexesTransaction extends ServletTransaction {

	private final static String TEMPLATE = "accounts/indexes/indexes.ftl";

	private final String accountId;
	private final IndexesService indexesService;

	public IndexesTransaction(final Components components, final String accountId, final HttpServletRequest request,
			final HttpServletResponse response) throws IOException, URISyntaxException {
		super(components, request, response, true);
		this.indexesService = components.getIndexesService();
		this.accountId = accountId;
	}

	public void create() throws IOException, ServletException {
		final String indexName = request.getParameter("indexName");
		if (!StringUtils.isBlank(indexName))
			indexesService.createIndex(accountId, indexName);
		doGet();
	}

	@Override
	protected void doGet() throws IOException, ServletException {
		request.setAttribute("accountId", accountId);
		request.setAttribute("indexes", indexesService.getIndexes(accountId));
		doTemplate(TEMPLATE);
	}

}
