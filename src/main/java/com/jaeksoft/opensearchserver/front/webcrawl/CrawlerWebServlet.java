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

import com.jaeksoft.opensearchserver.Components;
import com.jaeksoft.opensearchserver.front.BaseServlet;
import com.jaeksoft.opensearchserver.front.ServletTransaction;
import com.jaeksoft.opensearchserver.services.IndexesService;
import com.jaeksoft.opensearchserver.services.TasksService;
import com.jaeksoft.opensearchserver.services.WebCrawlsService;
import com.qwazr.library.freemarker.FreeMarkerTool;
import com.qwazr.utils.StringUtils;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@WebServlet(CrawlerWebServlet.PATH + "/*")
public class CrawlerWebServlet extends BaseServlet {

	public final static String PATH = "/crawlers/web";

	final static String TASKS = "tasks";

	final FreeMarkerTool freemarker;
	final IndexesService indexesService;
	final WebCrawlsService webCrawlsService;
	final TasksService tasksService;

	public CrawlerWebServlet(final Components components) throws IOException {
		this.freemarker = components.getFreemarkerTool();
		this.indexesService = components.getIndexesService();
		this.webCrawlsService = components.getWebCrawlsService();
		this.tasksService = components.getTasksService();
	}

	@Override
	protected ServletTransaction getServletTransaction(final HttpServletRequest request,
			final HttpServletResponse response) throws IOException {
		final String[] pathParts = StringUtils.split(request.getPathInfo(), '/');
		if (pathParts == null || pathParts.length == 0)
			return new WebCrawlListTransaction(this, request, response);
		final UUID webCrawlUuid = UUID.fromString(pathParts[0]);
		if (pathParts.length == 1)
			return new WebCrawlEditTransaction(this, webCrawlUuid, request, response);
		if (pathParts.length == 2 && TASKS.equals(pathParts[1]))
			return new WebCrawlTasksTransaction(this, webCrawlUuid, request, response);
		return null;
	}

}