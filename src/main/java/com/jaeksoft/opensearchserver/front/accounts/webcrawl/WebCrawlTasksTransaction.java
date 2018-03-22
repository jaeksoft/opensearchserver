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
import com.jaeksoft.opensearchserver.front.accounts.tasks.TaskResult;
import com.jaeksoft.opensearchserver.model.AccountRecord;
import com.jaeksoft.opensearchserver.model.TaskRecord;
import com.jaeksoft.opensearchserver.model.WebCrawlRecord;
import com.jaeksoft.opensearchserver.model.WebCrawlTaskDefinition;
import com.jaeksoft.opensearchserver.services.IndexesService;
import com.jaeksoft.opensearchserver.services.TasksService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.NotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WebCrawlTasksTransaction extends AccountTransaction {

	private final static String TEMPLATE = "accounts/crawlers/web/tasks.ftl";

	private final WebCrawlRecord webCrawlRecord;
	private final IndexesService indexesService;
	private final TasksService tasksService;

	public WebCrawlTasksTransaction(final Components components, final AccountRecord accountRecord,
			final UUID webCrawlUuid, final HttpServletRequest request, final HttpServletResponse response) {
		super(components, accountRecord, request, response);
		webCrawlRecord = components.getWebCrawlsService().read(accountRecord.getId(), webCrawlUuid);
		if (webCrawlRecord == null)
			throw new NotFoundException("Crawl not found: " + webCrawlUuid);
		indexesService = components.getIndexesService();
		tasksService = components.getTasksService();
	}

	@Override
	protected String getTemplate() {
		return TEMPLATE;
	}

	private WebCrawlTaskDefinition getWebCrawlTask() {
		final String index = request.getParameter("index");
		final UUID indexUuid =
				UUID.fromString(indexesService.getIndex(accountRecord.id, index).getIndexStatus().index_uuid);
		return new WebCrawlTaskDefinition(webCrawlRecord, indexUuid);
	}

	public void activate() {
		final WebCrawlTaskDefinition webCrawlTask = getWebCrawlTask();
		final TaskRecord taskRecord = tasksService.getTask(webCrawlTask.getTaskId());
		if (taskRecord != null) {
			addMessage(Message.Css.warning, "Warning!", "This Web crawl was already activated");
			return;
		}
		tasksService.createTask(TaskRecord.of(accountRecord.getId())
				.definition(webCrawlTask)
				.status(TaskRecord.Status.ACTIVE, "Activated by the user")
				.build());
		addMessage(Message.Css.success, "Success!", "The Web crawl has been activated.");
	}

	@Override
	protected void doGet() throws IOException, ServletException {
		final List<TaskRecord> tasks = new ArrayList<>();
		final long totalCount = tasksService.collectTasksByDefinition(webCrawlRecord.getUuid(), 0, 1000, tasks);
		final TaskResult.Builder resultBuilder = TaskResult.of(indexesService, accountRecord.getId());
		for (TaskRecord task : tasks)
			resultBuilder.add(task);

		request.setAttribute("webCrawlRecord", webCrawlRecord);
		request.setAttribute("tasks", resultBuilder.build());
		request.setAttribute("totalCount", totalCount);
		request.setAttribute("indexes", indexesService.getIndexes(accountRecord.id));
		super.doGet();
	}
}
