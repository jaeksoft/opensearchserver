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

package com.jaeksoft.opensearchserver.front;

import com.jaeksoft.opensearchserver.Components;
import com.jaeksoft.opensearchserver.front.schema.AccountTransaction;
import com.jaeksoft.opensearchserver.front.schema.AccountsTransaction;
import com.jaeksoft.opensearchserver.front.schema.indexes.IndexTransaction;
import com.jaeksoft.opensearchserver.front.schema.indexes.IndexesTransaction;
import com.jaeksoft.opensearchserver.front.schema.tasks.ActiveTaskListTransaction;
import com.jaeksoft.opensearchserver.front.schema.tasks.ActiveTaskStatusTransaction;
import com.jaeksoft.opensearchserver.front.schema.tasks.ArchivedTaskListTransaction;
import com.jaeksoft.opensearchserver.front.schema.tasks.ArchivedTaskStatusTransaction;
import com.jaeksoft.opensearchserver.front.schema.webcrawl.WebCrawlEditTransaction;
import com.jaeksoft.opensearchserver.front.schema.webcrawl.WebCrawlListTransaction;
import com.jaeksoft.opensearchserver.front.schema.webcrawl.WebCrawlTasksTransaction;
import com.qwazr.utils.StringUtils;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.UUID;

@WebServlet("/accounts/*")
public class AccountsServlet extends BaseServlet {

	final Components components;

	public AccountsServlet(final Components components) {
		this.components = components;
	}

	@Override
	protected ServletTransaction getServletTransaction(HttpServletRequest request, HttpServletResponse response)
			throws IOException, URISyntaxException, NoSuchMethodException {
		final String[] pathParts = StringUtils.split(request.getPathInfo(), '/');
		if (pathParts == null || pathParts.length == 0)
			return new AccountsTransaction(components, request, response);
		final String accountId = pathParts[0];
		if (pathParts.length == 1)
			return new AccountTransaction(components, accountId, request, response);
		final String cmd = pathParts[1];
		switch (cmd) {
		case "indexes":
			return doIndexes(accountId, pathParts, request, response);
		case "crawlers":
			return doCrawlers(accountId, pathParts, request, response);
		case "tasks":
			return doTasks(accountId, pathParts, request, response);
		default:
			return null;
		}
	}

	private ServletTransaction doIndexes(final String accountId, final String[] pathParts,
			final HttpServletRequest request, final HttpServletResponse response)
			throws IOException, URISyntaxException, NoSuchMethodException {
		if (pathParts.length == 2)
			return new IndexesTransaction(components, accountId, request, response);
		if (pathParts.length == 3) {
			final String indexName = pathParts[2];
			return new IndexTransaction(components, accountId, indexName, request, response);
		}
		return null;
	}

	private ServletTransaction doCrawlers(final String accountId, final String[] pathParts,
			final HttpServletRequest request, final HttpServletResponse response)
			throws IOException, URISyntaxException, NoSuchMethodException {
		if (pathParts.length == 2)
			return null;
		final String crawlerType = pathParts[2];
		switch (crawlerType) {
		case "web":
			return doCrawlerWeb(accountId, pathParts, request, response);
		default:
			return null;
		}
	}

	private ServletTransaction doCrawlerWeb(final String accountId, final String[] pathParts,
			final HttpServletRequest request, final HttpServletResponse response)
			throws IOException, URISyntaxException, NoSuchMethodException {
		if (pathParts.length == 3)
			return new WebCrawlListTransaction(components, accountId, request, response);
		final UUID webCrawlUuid = UUID.fromString(pathParts[3]);
		if (pathParts.length == 4)
			return new WebCrawlEditTransaction(components, accountId, webCrawlUuid, request, response);
		if (pathParts.length == 5 && "tasks".equals(pathParts[4]))
			return new WebCrawlTasksTransaction(components, accountId, webCrawlUuid, request, response);
		return null;
	}

	private ServletTransaction doTasks(final String accountId, final String[] pathParts,
			final HttpServletRequest request, final HttpServletResponse response)
			throws IOException, URISyntaxException, NoSuchMethodException {
		if (pathParts.length == 2)
			return new ActiveTaskListTransaction(components, accountId, request, response);
		final String taskName = pathParts[2];
		if ("archives".equals(taskName))
			return doArchivesTasks(accountId, pathParts, request, response);
		if (pathParts.length == 3)
			return new ActiveTaskStatusTransaction(components, accountId, taskName, request, response);
		return null;
	}

	private ServletTransaction doArchivesTasks(final String accountId, final String[] pathParts,
			final HttpServletRequest request, final HttpServletResponse response)
			throws IOException, URISyntaxException, NoSuchMethodException {
		if (pathParts.length == 3)
			return new ArchivedTaskListTransaction(components, accountId, request, response);
		final String taskName = pathParts[3];
		if (pathParts.length == 4)
			return new ArchivedTaskStatusTransaction(components, accountId, taskName, request, response);
		return null;
	}
}
