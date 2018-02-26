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
import com.jaeksoft.opensearchserver.model.TaskRecord;
import com.jaeksoft.opensearchserver.services.TasksService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;

public class ActiveTaskStatusTransaction extends ServletTransaction {

	private final static String TEMPLATE_INDEX = "schemas/tasks/active_status.ftl";

	private final String schemaName;
	private final TasksService tasksService;
	private final String taskId;

	public ActiveTaskStatusTransaction(final Components components, final String schemaName, final String taskId,
			final HttpServletRequest request, final HttpServletResponse response)
			throws IOException, URISyntaxException, NoSuchMethodException {
		super(components, request, response);
		this.schemaName = schemaName;
		this.tasksService = components.getTasksService();
		this.taskId = taskId;
	}

	private TaskRecord checkTaskRecord() throws IOException {
		final TaskRecord taskRecord = tasksService.getActiveTask(schemaName, taskId);
		if (taskRecord != null)
			return taskRecord;
		response.sendError(HttpServletResponse.SC_NOT_FOUND);
		return null;
	}

	public void pause() throws IOException, ServletException {
		tasksService.pause(schemaName, taskId);
		doGet();
	}

	public void start() throws IOException, ServletException {
		tasksService.start(schemaName, taskId);
		doGet();
	}

	@Override
	protected void doGet() throws IOException, ServletException {
		final TaskRecord taskRecord = checkTaskRecord();
		if (taskRecord == null)
			return;
		request.setAttribute("schema", schemaName);
		request.setAttribute("task", taskRecord);
		doTemplate(TEMPLATE_INDEX);
	}
}
