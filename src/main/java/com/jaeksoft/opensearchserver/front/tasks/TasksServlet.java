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

import com.jaeksoft.opensearchserver.Components;
import com.jaeksoft.opensearchserver.front.BaseServlet;
import com.jaeksoft.opensearchserver.front.ServletTransaction;
import com.jaeksoft.opensearchserver.services.IndexesService;
import com.jaeksoft.opensearchserver.services.TasksService;
import com.qwazr.library.freemarker.FreeMarkerTool;
import com.qwazr.utils.StringUtils;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(TasksServlet.PATH + "/*")
public class TasksServlet extends BaseServlet {

	final static String PATH = "/tasks";
	final static String ARCHIVED_PATH = "archived";

	final FreeMarkerTool freemarker;
	final IndexesService indexesService;
	final TasksService tasksService;

	public TasksServlet(final Components components) throws IOException {
		this.freemarker = components.getFreemarkerTool();
		this.indexesService = components.getIndexesService();
		this.tasksService = components.getTasksService();
	}

	@Override
	protected ServletTransaction getServletTransaction(final HttpServletRequest request,
			final HttpServletResponse response) throws IOException {
		final String[] pathParts = StringUtils.split(request.getPathInfo(), '/');
		if (pathParts == null || pathParts.length == 0)
			return new ActiveTaskListTransaction(this, request, response);
		final String part1 = pathParts[0];
		if (pathParts.length == 1) {
			if (ARCHIVED_PATH.equals(part1))
				return new ArchivedTaskListTransaction(this, request, response);
			else
				return new ActiveTaskStatusTransaction(this, part1, request, response);
		} else if (pathParts.length == 2) {
			final String part2 = pathParts[1];
			if (ARCHIVED_PATH.equals(part1))
				return new ArchivedTaskStatusTransaction(this, part2, request, response);
		}
		return null;
	}

}