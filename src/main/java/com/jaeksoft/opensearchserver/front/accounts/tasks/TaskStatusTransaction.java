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
import com.jaeksoft.opensearchserver.front.Message;
import com.jaeksoft.opensearchserver.front.accounts.AccountTransaction;
import com.jaeksoft.opensearchserver.model.AccountRecord;
import com.jaeksoft.opensearchserver.model.TaskRecord;
import com.jaeksoft.opensearchserver.services.TasksService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.NotFoundException;

public class TaskStatusTransaction extends AccountTransaction {

	private final static String TEMPLATE = "accounts/tasks/status.ftl";

	private final TasksService tasksService;
	private final TaskRecord taskRecord;

	public TaskStatusTransaction(final Components components, final AccountRecord accountRecord, final String taskId,
			final HttpServletRequest request, final HttpServletResponse response) {
		super(components, accountRecord, request, response);
		this.tasksService = components.getTasksService();
		taskRecord = tasksService.getTask(taskId);
		if (taskRecord == null)
			throw new NotFoundException("Task not found: " + taskId);
		if (!accountRecord.getId().equals(taskRecord.getAccountId()))
			throw new NotAllowedException("Not allowed: " + taskId);
		request.setAttribute("task", taskRecord);
		request.setAttribute("infos", tasksService.getTaskInfos(taskRecord));
	}

	@Override
	protected String getTemplate() {
		return TEMPLATE;
	}

	public void pause() {
		if (tasksService.updateStatus(taskRecord.getTaskId(), TaskRecord.Status.PAUSED, "Paused by the user"))
			addMessage(Message.Css.success, "The task has been paused", null);
	}

	public void start() {
		if (tasksService.updateStatus(taskRecord.getTaskId(), TaskRecord.Status.ACTIVE, "Activated by the user"))
			addMessage(Message.Css.success, "The task has been activated", null);
	}

	public String remove() {
		if (tasksService.removeTask(taskRecord.getTaskId(), "Stopped by the user")) {
			addMessage(Message.Css.success, "The task has been removed", null);
			return "/accounts/" + accountRecord.getName() + "/tasks";
		}
		return null;
	}

}
