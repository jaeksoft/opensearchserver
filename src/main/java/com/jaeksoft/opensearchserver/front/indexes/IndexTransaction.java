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

package com.jaeksoft.opensearchserver.front.indexes;

import com.jaeksoft.opensearchserver.front.Message;
import com.jaeksoft.opensearchserver.front.ServletTransaction;
import com.jaeksoft.opensearchserver.services.IndexService;
import com.jaeksoft.opensearchserver.services.IndexesService;
import com.qwazr.search.index.IndexStatus;
import com.qwazr.utils.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class IndexTransaction extends ServletTransaction {

	private final static String TEMPLATE_INDEX = "index.ftl";

	private final IndexesService indexesService;
	private final String indexName;

	IndexTransaction(final IndexServlet indexServlet, final String indexName, final HttpServletRequest request,
			final HttpServletResponse response) {
		super(indexServlet.freemarker, request, response);
		this.indexesService = indexServlet.indexesService;
		this.indexName = indexName;
	}

	public void delete() throws IOException, ServletException {
		final String indexName = request.getParameter("indexName");
		if (!StringUtils.isBlank(indexName)) {
			if (indexName.equals(this.indexName)) {
				indexesService.deleteIndex(getAccountSchema(), indexName);
				addMessage(Message.Css.info, null, "Index \"" + indexName + "\" deleted");
				response.sendRedirect("/");
				return;
			} else
				addMessage(Message.Css.warning, null, "Please confirm the name of the index to delete");
		}
		doGet();
	}

	@Override
	protected void doGet() throws IOException, ServletException {
		request.setAttribute("indexName", indexName);
		final IndexService indexService = indexesService.getIndex(getAccountSchema(), indexName);
		final IndexStatus status = indexService.getIndexStatus();
		request.setAttribute("indexSize", status.segments_size);
		request.setAttribute("indexCount", status.num_docs);
		request.setAttribute("crawlStatusMap", indexService.getCrawlStatusCount());
		doTemplate(TEMPLATE_INDEX);
	}

}
