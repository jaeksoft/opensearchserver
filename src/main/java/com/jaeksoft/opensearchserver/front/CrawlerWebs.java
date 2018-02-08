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
import com.qwazr.utils.HashUtils;
import com.qwazr.utils.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

class CrawlerWebs extends IndexBase {

	private final static String TEMPLATE_INDEX = "web_crawls.ftl";

	private final Map<UUID, WebCrawlRecord> webCrawlRecords;

	CrawlerWebs(final IndexServlet servlet, final String indexName, final HttpServletRequest request,
			final HttpServletResponse response) throws IOException {
		super(servlet, indexName, request, response);
		webCrawlRecords = new LinkedHashMap<>();
		webCrawlsService.fillMap(indexName, webCrawlRecords);
	}

	void create() throws IOException, ServletException {
		final UUID crawlUuid = getRequestParameter("crawlUuid", UUID::fromString, HashUtils::newTimeBasedUUID);
		final String crawlName = request.getParameter("crawlName");
		final String entryUrl = request.getParameter("entryUrl");
		final Integer maxDepth = getRequestParameter("maxDepth", null);
		final WebCrawlDefinition.Builder webCrawlDefBuilder =
				WebCrawlDefinition.of().setEntryUrl(entryUrl).setMaxDepth(maxDepth);
		webCrawlRecords.put(crawlUuid,
				WebCrawlRecord.of(crawlUuid).name(crawlName).crawlDefinition(webCrawlDefBuilder.build()).build());
		webCrawlsService.set(indexName, webCrawlRecords.values());
		doGet();
	}

	@Override
	void doGet() throws IOException, ServletException {
		request.setAttribute("indexName", indexName);
		final List<WebCrawlRecord> webCrawlRecords = webCrawlsService.get(indexName);
		if (webCrawlRecords != null) {
			webCrawlRecords.sort((r1, r2) -> StringUtils.compare(r1.name, r2.name));
			request.setAttribute("webCrawlRecords", webCrawlRecords);
		}
		doTemplate(TEMPLATE_INDEX);
	}
}
