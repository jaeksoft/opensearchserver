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
package com.jaeksoft.opensearchserver.front.schema.tasks;

import com.jaeksoft.opensearchserver.Components;
import com.jaeksoft.opensearchserver.front.ServletTransaction;
import com.jaeksoft.opensearchserver.services.IndexesService;
import com.jaeksoft.opensearchserver.services.TasksService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public class ArchivedTaskListTransaction extends ServletTransaction {

	private final static String TEMPLATE_INDEX = "accounts/tasks/archived.ftl";

	private final String accountId;
	private final TasksService tasksService;
	private final IndexesService indexesService;

	public ArchivedTaskListTransaction(final Components components, final String accountId,
			final HttpServletRequest request, final HttpServletResponse response)
			throws IOException, URISyntaxException, NoSuchMethodException {
		super(components, request, response);
		this.accountId = accountId;
		tasksService = components.getTasksService();
		indexesService = components.getIndexesService();
	}

	@Override
	protected void doGet() throws IOException, ServletException {
		final int start = getRequestParameter("start", 0);
		final int rows = getRequestParameter("rows", 25);

		final TaskResult.Builder resultBuilder = TaskResult.of(indexesService, accountId, null);
		int totalCount = tasksService.collectActiveTasks(accountId, start, rows, resultBuilder::add);
		final List<TaskResult> tasks = resultBuilder.build();

		request.setAttribute("accountId", accountId);
		request.setAttribute("tasks", tasks);
		request.setAttribute("totalCount", totalCount);
		doTemplate(TEMPLATE_INDEX);
	}
}
