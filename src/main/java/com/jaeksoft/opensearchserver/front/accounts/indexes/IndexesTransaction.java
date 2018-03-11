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
import com.jaeksoft.opensearchserver.front.Message;
import com.jaeksoft.opensearchserver.front.accounts.AccountTransaction;
import com.jaeksoft.opensearchserver.model.AccountRecord;
import com.jaeksoft.opensearchserver.services.IndexesService;
import com.qwazr.utils.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class IndexesTransaction extends AccountTransaction {

	private final static String TEMPLATE = "accounts/indexes/indexes.ftl";

	private final IndexesService indexesService;

	public IndexesTransaction(final Components components, final AccountRecord accountRecord,
			final HttpServletRequest request, final HttpServletResponse response) {
		super(components, accountRecord, request, response);
		this.indexesService = components.getIndexesService();
		request.setAttribute("indexes", indexesService.getIndexes(accountRecord.id));
	}

	@Override
	protected String getTemplate() {
		return TEMPLATE;
	}

	public void create() {
		final String indexName = request.getParameter("indexName");
		if (StringUtils.isBlank(indexName))
			return;
		indexesService.createIndex(accountRecord.id, indexName);
		addMessage(Message.Css.success, "Index created: " + indexName, null);
	}

}
