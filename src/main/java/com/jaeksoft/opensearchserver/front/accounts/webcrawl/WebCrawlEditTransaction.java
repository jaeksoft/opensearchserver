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
package com.jaeksoft.opensearchserver.front.accounts.webcrawl;

import com.jaeksoft.opensearchserver.Components;
import com.jaeksoft.opensearchserver.front.Message;
import com.jaeksoft.opensearchserver.front.accounts.AccountTransaction;
import com.jaeksoft.opensearchserver.model.AccountRecord;
import com.jaeksoft.opensearchserver.model.WebCrawlRecord;
import com.jaeksoft.opensearchserver.model.WebCrawlTaskDefinition;
import com.jaeksoft.opensearchserver.services.TasksService;
import com.jaeksoft.opensearchserver.services.WebCrawlsService;
import com.qwazr.crawler.web.WebCrawlDefinition;
import com.qwazr.utils.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.NotFoundException;
import java.util.UUID;

public class WebCrawlEditTransaction extends AccountTransaction {

	private final static String TEMPLATE = "accounts/crawlers/web/edit.ftl";

	private final WebCrawlsService webCrawlsService;
	private final WebCrawlRecord webCrawlRecord;
	private final TasksService tasksService;

	public WebCrawlEditTransaction(final Components components, final AccountRecord accountRecord,
			final UUID webCrawlUuid, final HttpServletRequest request, final HttpServletResponse response) {
		super(components, accountRecord, request, response);

		this.webCrawlsService = components.getWebCrawlsService();
		this.tasksService = components.getTasksService();
		webCrawlRecord = webCrawlsService.read(accountRecord.getId(), webCrawlUuid);
		if (webCrawlRecord == null)
			throw new NotFoundException("Web crawl not found: " + webCrawlUuid);
		request.setAttribute("webCrawlRecord", webCrawlRecord);
	}

	@Override
	protected String getTemplate() {
		return TEMPLATE;
	}

	public String delete() {
		final String crawlName = request.getParameter("crawlName");
		if (webCrawlRecord.name.equals(crawlName)) {
			webCrawlsService.remove(accountRecord.getId(), webCrawlRecord.getUuid());
			addMessage(Message.Css.success, null, "Crawl \"" + webCrawlRecord.name + "\" deleted");
			return StringUtils.EMPTY;
		}
		addMessage(Message.Css.warning, null, "Please confirm the name of the crawl to delete");
		return null;
	}

	public String save() {
		final String crawlName = request.getParameter("crawlName");
		final String entryUrl = request.getParameter("entryUrl");
		final Integer maxDepth = getRequestParameter("maxDepth", null, null, null);
		final Integer maxUrlNumber = getRequestParameter("maxUrlNumber", null, null, null);
		final Boolean deleteOlderSession = getRequestParameter("deleteOlderSession", false);
		final WebCrawlDefinition.Builder webCrawlDefBuilder =
				WebCrawlDefinition.of().setEntryUrl(entryUrl).setMaxDepth(maxDepth).setMaxUrlNumber(maxUrlNumber);
		final String[] inclusions = request.getParameterValues("inclusion");
		if (inclusions != null)
			for (String inclusion : inclusions)
				webCrawlDefBuilder.addInclusionPattern(inclusion);
		final String[] exclusions = request.getParameterValues("exclusion");
		if (exclusions != null)
			for (String exclusion : exclusions)
				webCrawlDefBuilder.addExclusionPattern(exclusion);
		final WebCrawlRecord newWebCrawlRecord = webCrawlRecord.from()
				.name(crawlName)
				.crawlDefinition(webCrawlDefBuilder.build())
				.deleteOlderSession(deleteOlderSession)
				.build();
		webCrawlsService.save(accountRecord.getId(), newWebCrawlRecord);
		tasksService.updateDefinitions(webCrawlRecord.uuid, oldTaskDef -> {
			final WebCrawlTaskDefinition oldWebCrawl = (WebCrawlTaskDefinition) oldTaskDef;
			return new WebCrawlTaskDefinition(newWebCrawlRecord, oldWebCrawl.indexUuid);
		});
		return "/accounts/" + accountRecord.id + "/crawlers/web/" + webCrawlRecord.getUuid();
	}

	public String revert() {
		return null;
	}

}
