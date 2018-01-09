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

	CrawlerWebs(final IndexServlet servlet, final String indexName, final HttpServletRequest request,
			final HttpServletResponse response) {
		super(servlet, indexName, request, response);
	}

	@Override
	void doPost() throws IOException, ServletException {
		final List<WebCrawlRecord> webCrawlRecordList = webCrawlsService.get(indexName);
		final Map<UUID, WebCrawlRecord> webCrawlRecordMap = new LinkedHashMap<>();
		if (webCrawlRecordList != null)
			webCrawlRecordList.forEach(r -> webCrawlRecordMap.put(UUID.fromString(r.uuid), r));

		final UUID crawlUuid = getRequestParameter("crawlUuid", UUID::fromString, HashUtils::newTimeBasedUUID);
		final String crawlName = request.getParameter("crawlName");
		final String action = request.getParameter("action");
		switch (action) {
		case "create":
			final String entryUrl = request.getParameter("entryUrl");
			final Integer maxDepth = getRequestParameter("maxDepth", null);
			final WebCrawlDefinition.Builder webCrawlDefBuilder = WebCrawlDefinition.of();
			if (!StringUtils.isBlank(entryUrl))
				webCrawlDefBuilder.setEntryUrl(entryUrl);
			if (maxDepth != null)
				webCrawlDefBuilder.setMaxDepth(maxDepth);
			webCrawlRecordMap.put(crawlUuid,
					WebCrawlRecord.of(crawlUuid).name(crawlName).crawlDefinition(webCrawlDefBuilder.build()).build());
			webCrawlsService.set(indexName, webCrawlRecordMap.values());
			break;
		case "del":
			webCrawlRecordMap.remove(crawlUuid);
			webCrawlsService.set(indexName, webCrawlRecordMap.values());
			break;
		}
		doGet();
	}

	@Override
	void doGet() throws IOException, ServletException {
		request.setAttribute("indexName", indexName);
		final List<WebCrawlRecord> webCrawlRecords = webCrawlsService.get(indexName);
		if (webCrawlRecords != null) {
			Collections.sort(webCrawlRecords, (r1, r2) -> StringUtils.compare(r1.name, r2.name));
			request.setAttribute("webCrawlRecords", webCrawlRecords);
		}
		doTemplate(TEMPLATE_INDEX);
	}
}
