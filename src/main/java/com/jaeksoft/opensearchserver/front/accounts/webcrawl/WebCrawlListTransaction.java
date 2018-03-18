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
import com.jaeksoft.opensearchserver.model.TaskRecord;
import com.jaeksoft.opensearchserver.model.WebCrawlRecord;
import com.jaeksoft.opensearchserver.model.WebCrawlTaskDefinition;
import com.jaeksoft.opensearchserver.services.IndexesService;
import com.jaeksoft.opensearchserver.services.TasksService;
import com.jaeksoft.opensearchserver.services.WebCrawlsService;
import com.qwazr.crawler.web.WebCrawlDefinition;
import com.qwazr.utils.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;

public class WebCrawlListTransaction extends AccountTransaction {

	private final static String TEMPLATE = "accounts/crawlers/web/list.ftl";

	private final WebCrawlsService webCrawlsService;
	private final IndexesService indexesService;
	private final TasksService tasksService;

	public WebCrawlListTransaction(final Components components, final AccountRecord accountRecord,
			final HttpServletRequest request, final HttpServletResponse response) {
		super(components, accountRecord, request, response);
		webCrawlsService = components.getWebCrawlsService();
		tasksService = components.getTasksService();
		indexesService = components.getIndexesService();
	}

	@Override
	protected String getTemplate() {
		return TEMPLATE;
	}

	public void create() throws IOException, URISyntaxException {
		final String crawlName = request.getParameter("crawlName");
		final String entryUrl = request.getParameter("entryUrl");
		final Integer maxDepth = getRequestParameter("maxDepth", null);
		final Integer maxUrlNumber = getRequestParameter("maxUrlNumber", null);

		// Extract the URLs
		final Set<URI> uriSet = new LinkedHashSet<>();
		try (final BufferedReader reader = new BufferedReader(new StringReader(entryUrl))) {
			String urlLine;
			while ((urlLine = reader.readLine()) != null) {
				urlLine = urlLine.trim();
				if (StringUtils.isBlank(urlLine))
					continue;
				uriSet.add(new URI(urlLine).normalize());
			}
		}

		int count = 0;
		for (final URI uri : uriSet) {
			final WebCrawlDefinition.Builder webCrawlDefBuilder = WebCrawlDefinition.of()
					.setEntryUrl(uri.toString())
					.setMaxDepth(maxDepth)
					.setMaxUrlNumber(maxUrlNumber);
			final String name;
			if (crawlName == null || crawlName.isEmpty())
				name = uri.getHost();
			else
				name = crawlName + (count == 0 ? StringUtils.EMPTY : count);
			count++;
			webCrawlsService.save(accountRecord.getId(),
					WebCrawlRecord.of().name(name).crawlDefinition(webCrawlDefBuilder.build()).build());
		}
	}

	private Integer forEachCrawl(final BiFunction<WebCrawlTaskDefinition, TaskRecord, Boolean> crawlAction) {
		final String index = request.getParameter("index");
		final String[] crawls = request.getParameterValues("c");
		if (crawls == null || crawls.length == 0) {
			addMessage(Message.Css.warning, "Nothing to do !", "Please select one or more crawl(s)");
			return null;
		}
		final UUID indexUuid =
				UUID.fromString(indexesService.getIndex(accountRecord.id, index).getIndexStatus().index_uuid);

		int count = 0;
		for (String crawlId : crawls) {
			final WebCrawlRecord webCrawlRecord =
					webCrawlsService.read(accountRecord.getId(), UUID.fromString(crawlId));
			final WebCrawlTaskDefinition webCrawlTask = new WebCrawlTaskDefinition(webCrawlRecord, indexUuid);
			final TaskRecord taskRecord = tasksService.getTask(webCrawlTask.getTaskId());
			if (crawlAction.apply(webCrawlTask, taskRecord))
				count++;
		}
		return count;
	}

	public void activate() {
		final Integer count = forEachCrawl((webCrawlTask, taskRecord) -> {
			if (taskRecord == null) {
				tasksService.createTask(TaskRecord.of(accountRecord.getId())
						.definition(webCrawlTask)
						.status(TaskRecord.Status.ACTIVE)
						.build());
				return true;
			}
			return tasksService.updateStatus(taskRecord.getTaskId(), TaskRecord.Status.ACTIVE);
		});
		if (count != null)
			addMessage(Message.Css.success, "Activate Crawl", count + " crawl(s) activated");
	}

	public void pause() {
		final Integer count = forEachCrawl((webCrawlTask, taskRecord) -> taskRecord != null &&
				tasksService.updateStatus(taskRecord.getTaskId(), TaskRecord.Status.PAUSED));
		if (count != null)
			addMessage(Message.Css.success, "Pause Crawl", count + " crawl(s) paused");
	}

	public void stop() {
		final Integer count = forEachCrawl(
				(webCrawlTask, taskRecord) -> taskRecord != null && tasksService.removeTask(taskRecord.getTaskId()));
		if (count != null)
			addMessage(Message.Css.success, "Stop Crawl", count + " crawl(s) stopped");
	}

	@Override
	protected void doGet() throws IOException, ServletException {
		final int start = getRequestParameter("start", 0);
		final int rows = getRequestParameter("rows", 25);

		final List<WebCrawlRecord> webCrawlRecords = new ArrayList<>();
		final int totalCount = webCrawlsService.collect(accountRecord.getId(), start, rows, webCrawlRecords);

		request.setAttribute("webCrawlRecords", webCrawlRecords);
		request.setAttribute("totalCount", totalCount);
		request.setAttribute("indexes", indexesService.getIndexes(accountRecord.id));
		super.doGet();
	}
}
