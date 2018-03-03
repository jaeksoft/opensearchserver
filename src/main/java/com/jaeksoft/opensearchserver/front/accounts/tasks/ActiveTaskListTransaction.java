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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

public class ActiveTaskListTransaction extends AccountTransaction {

	private final static String TEMPLATE = "accounts/tasks/active_list.ftl";

	public ActiveTaskListTransaction(final Components components, final UUID accountId,
			final HttpServletRequest request, final HttpServletResponse response)
			throws IOException, URISyntaxException, NoSuchMethodException {
		super(components, accountId, request, response);

		final int start = getRequestParameter("start", 0);
		final int rows = getRequestParameter("rows", 25);

		final TaskResult.Builder resultBuilder =
				TaskResult.of(components.getIndexesService(), accountRecord.id, components.getWebCrawlsService());
		int totalCount =
				components.getTasksService().collectActiveTasks(accountRecord.id, start, rows, resultBuilder::add);
		final List<TaskResult> tasks = resultBuilder.build();

		request.setAttribute("accountId", accountId);
		request.setAttribute("tasks", tasks);
		request.setAttribute("totalCount", totalCount);
	}

	@Override
	protected String getTemplate() {
		return TEMPLATE;
	}

}
