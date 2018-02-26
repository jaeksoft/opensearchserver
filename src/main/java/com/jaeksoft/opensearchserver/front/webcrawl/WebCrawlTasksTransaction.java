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
package com.jaeksoft.opensearchserver.front.webcrawl;

import com.jaeksoft.opensearchserver.front.Message;
import com.jaeksoft.opensearchserver.front.ServletTransaction;
import com.jaeksoft.opensearchserver.front.tasks.TaskResult;
import com.jaeksoft.opensearchserver.model.WebCrawlRecord;
import com.jaeksoft.opensearchserver.model.WebCrawlTaskRecord;
import com.jaeksoft.opensearchserver.services.IndexesService;
import com.jaeksoft.opensearchserver.services.TasksService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class WebCrawlTasksTransaction extends ServletTransaction {

	private final static String TEMPLATE_INDEX = "web_crawl/tasks.ftl";

	private final WebCrawlRecord webCrawlRecord;
	private final IndexesService indexesService;
	private final TasksService tasksService;

	WebCrawlTasksTransaction(final CrawlerWebServlet servlet, final UUID webCrawlUuid, final HttpServletRequest request,
			final HttpServletResponse response) throws IOException {
		super(servlet.freemarker, request, response);
		webCrawlRecord = servlet.webCrawlsService.read(getAccountSchema(), webCrawlUuid);
		indexesService = servlet.indexesService;
		tasksService = servlet.tasksService;
	}

	public void crawl() throws IOException, ServletException {
		if (webCrawlRecord == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		final String index = request.getParameter("index");
		final String accountSchema = getAccountSchema();
		final UUID indexUuid =
				UUID.fromString(indexesService.getIndex(accountSchema, index).getIndexStatus().index_uuid);
		final WebCrawlTaskRecord record = WebCrawlTaskRecord.of(webCrawlRecord, indexUuid).build();
		if (tasksService.getActiveTask(accountSchema, record.getTaskId()) != null) {
			addMessage(Message.Css.warning, "Web crawl already started",
					"This Web crawl has already been started on " + index);
		} else {
			tasksService.saveActiveTask(accountSchema, record);
			addMessage(Message.Css.success, "Web crawl started", "The Web crawl has been started on " + index);
		}
		doGet();
	}

	@Override
	protected void doGet() throws IOException, ServletException {
		if (webCrawlRecord == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		final String accountSchema = getAccountSchema();
		final TaskResult.Builder resultBuilder = TaskResult.of(indexesService, accountSchema, null);
		int totalCount =
				tasksService.collectActiveTasks(accountSchema, 0, 1000, webCrawlRecord.getUuid(), resultBuilder::add);
		final List<TaskResult> tasks = resultBuilder.build();

		request.setAttribute("webCrawlRecord", webCrawlRecord);
		request.setAttribute("tasks", tasks);
		request.setAttribute("totalCount", totalCount);
		request.setAttribute("indexes", indexesService.getIndexes(accountSchema));
		doTemplate(TEMPLATE_INDEX);
	}
}
