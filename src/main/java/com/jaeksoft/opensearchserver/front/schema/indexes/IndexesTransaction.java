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

package com.jaeksoft.opensearchserver.front.schema.indexes;

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

	private final static String TEMPLATE = "schemas/indexes/indexes.ftl";

	private final String schemaName;
	private final IndexesService indexesService;

	public IndexesTransaction(final Components components, final String schemaName, final HttpServletRequest request,
			final HttpServletResponse response) throws IOException, URISyntaxException, NoSuchMethodException {
		super(components, request, response);
		this.indexesService = components.getIndexesService();
		this.schemaName = schemaName;
	}

	public void create() throws IOException, ServletException {
		final String indexName = request.getParameter("indexName");
		if (!StringUtils.isBlank(indexName))
			indexesService.createIndex(schemaName, indexName);
		doGet();
	}

	@Override
	protected void doGet() throws IOException, ServletException {
		request.setAttribute("schema", schemaName);
		request.setAttribute("indexes", indexesService.getIndexes(schemaName));
		doTemplate(TEMPLATE);
	}

}
