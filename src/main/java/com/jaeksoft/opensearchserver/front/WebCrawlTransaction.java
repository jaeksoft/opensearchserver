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
import com.jaeksoft.opensearchserver.services.WebCrawlsService;
import com.qwazr.crawler.web.WebCrawlDefinition;
import com.qwazr.crawler.web.WebCrawlStatus;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

class WebCrawlTransaction extends ServletTransaction {

	private final static String TEMPLATE_INDEX = "web_crawl.ftl";

	private final WebCrawlsService webCrawlsService;
	private final WebCrawlRecord webCrawlRecord;
	private final WebCrawlStatus webCrawlStatus;

	WebCrawlTransaction(final CrawlerWebServlet servlet, final UUID webCrawlUuid, final HttpServletRequest request,
			final HttpServletResponse response) throws IOException {
		super(servlet.freemarker, request, response);
		this.webCrawlsService = servlet.webCrawlsService;
		webCrawlRecord = webCrawlsService.read(webCrawlUuid);
		webCrawlStatus = webCrawlsService.getCrawlStatus(webCrawlUuid);
	}

	void delete() throws IOException, ServletException {
		if (webCrawlRecord == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		final String crawlName = request.getParameter("crawlName");
		if (webCrawlRecord.name.equals(crawlName)) {
			webCrawlsService.remove(webCrawlRecord.getUuid());
			addMessage(Css.success, null, "Crawl \"" + webCrawlRecord.name + "\" deleted");
			response.sendRedirect("/crawlers/web");
			return;
		} else
			addMessage(ServletTransaction.Css.warning, null, "Please confirm the name of the crawl to delete");
		doGet();
	}

	void save() throws IOException {
		if (webCrawlRecord == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		final String crawlName = request.getParameter("crawlName");
		final String entryUrl = request.getParameter("entryUrl");
		final Integer maxDepth = getRequestParameter("maxDepth", null);
		final WebCrawlDefinition.Builder webCrawlDefBuilder =
				WebCrawlDefinition.of().setEntryUrl(entryUrl).setMaxDepth(maxDepth);
		webCrawlsService.save(
				WebCrawlRecord.of(webCrawlRecord).name(crawlName).crawlDefinition(webCrawlDefBuilder.build()).build());
		response.sendRedirect("/crawlers/web/" + webCrawlRecord.getUuid());
	}

	void start() throws IOException, ServletException {
		if (webCrawlRecord == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		//TODO
		doGet();
	}

	@Override
	void doGet() throws IOException, ServletException {
		if (webCrawlRecord == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		request.setAttribute("webCrawlRecord", webCrawlRecord);
		request.setAttribute("webCrawlStatus", webCrawlStatus);
		doTemplate(TEMPLATE_INDEX);
	}
}
