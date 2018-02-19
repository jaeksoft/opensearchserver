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
package com.jaeksoft.opensearchserver.front.tasks;

import com.jaeksoft.opensearchserver.front.ServletTransaction;
import com.jaeksoft.opensearchserver.services.IndexesService;
import com.jaeksoft.opensearchserver.services.TasksService;
import com.jaeksoft.opensearchserver.services.WebCrawlsService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class ActiveTaskListTransaction extends ServletTransaction {

	private final static String TEMPLATE_INDEX = "tasks/active_list.ftl";

	private final TasksService tasksService;
	private final IndexesService indexesService;
	private final WebCrawlsService webCrawlsService;

	ActiveTaskListTransaction(final TasksServlet servlet, final HttpServletRequest request,
			final HttpServletResponse response) {
		super(servlet.freemarker, request, response);
		tasksService = servlet.tasksService;
		indexesService = servlet.indexesService;
		webCrawlsService = servlet.webCrawlsService;
	}

	@Override
	protected void doGet() throws IOException, ServletException {
		final int start = getRequestParameter("start", 0);
		final int rows = getRequestParameter("rows", 25);

		final TaskResult.Builder resultBuilder = TaskResult.of(indexesService, webCrawlsService);
		int totalCount = tasksService.collectActiveTasks(start, rows, resultBuilder::add);
		final List<TaskResult> tasks = resultBuilder.build();

		request.setAttribute("tasks", tasks);
		request.setAttribute("totalCount", totalCount);
		doTemplate(TEMPLATE_INDEX);
	}

}
