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
import com.qwazr.utils.LinkUtils;
import com.qwazr.utils.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

class CrawlerWeb extends IndexBase {

	private final static String TEMPLATE_INDEX = "web_crawl.ftl";

	private final List<WebCrawlRecord> webCrawlRecords;
	private final WebCrawlRecord webCrawlRecord;

	CrawlerWeb(final IndexServlet servlet, final String indexName, final String webCrawlUuid,
			final HttpServletRequest request, final HttpServletResponse response) throws IOException {
		super(servlet, indexName, request, response);
		webCrawlRecords = webCrawlsService.get(indexName);
		if (webCrawlRecords != null)
			webCrawlRecord = webCrawlRecords.stream().filter(r -> webCrawlUuid.equals(r.uuid)).findAny().orElse(null);
		else
			webCrawlRecord = null;
	}

	@Override
	void doPost() throws IOException, ServletException {
		if (webCrawlRecord == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		final String action = request.getParameter("action");
		final String crawlName = request.getParameter("crawlName");
		if ("delete".equals(action) && !StringUtils.isBlank(crawlName)) {
			if (crawlName.equals(webCrawlRecord.name)) {
				webCrawlRecords.remove(webCrawlRecord);
				webCrawlsService.set(indexName, webCrawlRecords);
				addMessage(ServletTransaction.Css.info, null, "Crawl \"" + webCrawlRecord.name + "\" deleted");
				response.sendRedirect("/index/" + LinkUtils.urlEncode(indexName) + "/crawler/web");
				return;
			} else
				addMessage(ServletTransaction.Css.warning, null, "Please confirm the name of the crawl to delete");
		}
		doGet();
	}

	@Override
	void doGet() throws IOException, ServletException {
		if (webCrawlRecord == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		request.setAttribute("indexName", indexName);
		request.setAttribute("webCrawlRecord", webCrawlRecord);
		doTemplate(TEMPLATE_INDEX);
	}
}
