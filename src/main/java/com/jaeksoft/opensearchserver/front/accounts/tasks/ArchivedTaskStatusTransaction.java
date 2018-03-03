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
import com.jaeksoft.opensearchserver.front.accounts.AccountTransaction;
import com.jaeksoft.opensearchserver.model.TaskRecord;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.NotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.UUID;

public class ArchivedTaskStatusTransaction extends AccountTransaction {

	private final static String TEMPLATE = "accounts/tasks/achived_status.ftl";

	public ArchivedTaskStatusTransaction(final Components components, final UUID accountId, final String taskId,
			final HttpServletRequest request, final HttpServletResponse response)
			throws IOException, URISyntaxException, NoSuchMethodException {
		super(components, accountId, request, response);

		final TaskRecord taskRecord = components.getTasksService().getActiveTask(accountRecord.id, taskId);
		if (taskRecord == null)
			throw new NotFoundException("Task not found : " + taskId);

		request.setAttribute("task", taskRecord);
	}

	@Override
	protected String getTemplate() {
		return TEMPLATE;
	}
}
