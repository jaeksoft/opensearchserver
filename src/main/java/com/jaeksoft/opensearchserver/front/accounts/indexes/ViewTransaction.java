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
import com.jaeksoft.opensearchserver.front.accounts.AccountTransaction;
import com.jaeksoft.opensearchserver.model.AccountRecord;
import freemarker.template.TemplateException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

public class ViewTransaction extends AccountTransaction {

	private final static String TEMPLATE = "accounts/indexes/view.ftl";

	private final String indexName;
	private final String htmlCode;

	public ViewTransaction(final Components components, final AccountRecord accountRecord, final String indexName,
			final HttpServletRequest request, final HttpServletResponse response) throws IOException {
		super(components, accountRecord, request, response);
		this.indexName = indexName;
		try {
			final Map data = new HashMap<>();
			data.put("account", accountRecord);
			data.put("indexName", indexName);
			this.htmlCode = components.getFreemarkerTool().template("accounts/indexes/includes/view_example.ftl", data);
		} catch (TemplateException e) {
			throw new IOException(e);
		}
	}

	@Override
	protected String getTemplate() {
		return TEMPLATE;
	}

	@Override
	public void doGet() throws IOException, ServletException {
		request.setAttribute("indexName", indexName);
		request.setAttribute("htmlCode", htmlCode);
		super.doGet();
	}

}
