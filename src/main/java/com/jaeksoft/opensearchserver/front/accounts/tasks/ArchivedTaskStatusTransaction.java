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
package com.jaeksoft.opensearchserver.front.accounts.tasks;

import com.jaeksoft.opensearchserver.Components;
import com.jaeksoft.opensearchserver.front.ServletTransaction;
import com.jaeksoft.opensearchserver.model.TaskRecord;
import com.jaeksoft.opensearchserver.services.TasksService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;

public class ArchivedTaskStatusTransaction extends ServletTransaction {

	private final static String TEMPLATE_INDEX = "accounts/tasks/achived_status.ftl";

	private final String accountId;
	private final TasksService tasksService;
	private final TaskRecord taskRecord;

	public ArchivedTaskStatusTransaction(final Components components, final String accountId, final String taskId,
			final HttpServletRequest request, final HttpServletResponse response)
			throws IOException, URISyntaxException {
		super(components, request, response, true);
		this.accountId = accountId;
		tasksService = components.getTasksService();
		taskRecord = tasksService.getActiveTask(accountId, taskId);
	}

	@Override
	protected void doGet() throws IOException, ServletException {
		if (taskRecord == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		request.setAttribute("accountId", accountId);
		request.setAttribute("task", taskRecord);
		doTemplate(TEMPLATE_INDEX);
	}
}
