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
package com.jaeksoft.opensearchserver.front;

import com.jaeksoft.opensearchserver.model.WebCrawlRecord;
import com.qwazr.crawler.web.WebCrawlDefinition;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

class CrawlerWebs extends IndexBase {

	private final static String TEMPLATE_INDEX = "web_crawls.ftl";

	CrawlerWebs(final IndexServlet servlet, final String indexName, final HttpServletRequest request,
			final HttpServletResponse response) {
		super(servlet, indexName, request, response);
	}

	@Override
	void doPost() throws IOException {
		final WebCrawlRecord webCrawlRecord = webCrawlsService.get(indexName);
		final WebCrawlRecord.Builder builder = WebCrawlRecord.of(webCrawlRecord);
		final String newName = request.getParameter("newName");
		final String oldName = request.getParameter("oldName");
		final String action = request.getParameter("action");
		switch (action) {
		case "save":
			final String startUrl = request.getParameter("url");
			final Integer maxDepth = getRequestParameter("depth", null);
			final WebCrawlDefinition.Builder webCrawlDefBuilder = WebCrawlDefinition.of();
			webCrawlDefBuilder.setEntryUrl(startUrl);
			if (maxDepth != null)
				webCrawlDefBuilder.setMaxDepth(maxDepth);
			builder.remove(oldName);
			builder.set(newName, webCrawlDefBuilder.build());
			webCrawlsService.set(indexName, builder.build());
			break;
		case "del":
			builder.remove(oldName);
			break;
		}
	}

	@Override
	void doGet() throws IOException, ServletException {
		request.setAttribute("indexName", indexName);
		request.setAttribute("webCrawls", webCrawlsService.get(indexName));
		doTemplate(TEMPLATE_INDEX);
	}
}
