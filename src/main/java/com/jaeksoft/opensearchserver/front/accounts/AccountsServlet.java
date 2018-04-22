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

package com.jaeksoft.opensearchserver.front.accounts;

import com.jaeksoft.opensearchserver.Components;
import com.jaeksoft.opensearchserver.front.BaseServlet;
import com.jaeksoft.opensearchserver.front.ServletTransaction;
import com.jaeksoft.opensearchserver.front.accounts.indexes.IndexesTransaction;
import com.jaeksoft.opensearchserver.front.accounts.indexes.InfosTransaction;
import com.jaeksoft.opensearchserver.front.accounts.indexes.TemplateTransaction;
import com.jaeksoft.opensearchserver.front.accounts.indexes.ViewTransaction;
import com.jaeksoft.opensearchserver.front.accounts.tasks.TaskListTransaction;
import com.jaeksoft.opensearchserver.front.accounts.tasks.TaskPlanningTransaction;
import com.jaeksoft.opensearchserver.front.accounts.tasks.TaskStatusTransaction;
import com.jaeksoft.opensearchserver.front.accounts.webcrawl.WebCrawlEditTransaction;
import com.jaeksoft.opensearchserver.front.accounts.webcrawl.WebCrawlListTransaction;
import com.jaeksoft.opensearchserver.front.accounts.webcrawl.WebCrawlTasksTransaction;
import com.jaeksoft.opensearchserver.model.AccountRecord;
import com.qwazr.utils.StringUtils;
import freemarker.template.TemplateException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@WebServlet("/accounts/*")
public class AccountsServlet extends BaseServlet {

	final Components components;

	public AccountsServlet(final Components components) {
		this.components = components;
	}

	@Override
	protected ServletTransaction getServletTransaction(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		final String[] pathParts = StringUtils.split(request.getPathInfo(), '/');
		if (pathParts == null || pathParts.length == 0)
			return new AccountsTransaction(components, request, response);
		final AccountRecord accountRecord = components.getAccountsService().findExistingAccount(pathParts[0]);
		if (pathParts.length == 1)
			return new AccountTransaction(components, accountRecord, request, response);
		final String cmd = pathParts[1];
		switch (cmd) {
		case "indexes":
			return doIndexes(accountRecord, pathParts, request, response);
		case "crawlers":
			return doCrawlers(accountRecord, pathParts, request, response);
		case "tasks":
			return doTasks(accountRecord, pathParts, request, response);
		default:
			return null;
		}
	}

	private ServletTransaction doIndexes(final AccountRecord accountRecord, final String[] pathParts,
			final HttpServletRequest request, final HttpServletResponse response) throws IOException {
		if (pathParts.length == 2)
			return new IndexesTransaction(components, accountRecord, request, response);
		if (pathParts.length == 4) {
			final String indexName = pathParts[2];
			final String panel = pathParts[3];
			switch (panel) {
			case "infos":
				return new InfosTransaction(components, accountRecord, indexName, request, response);
			case "template":
				return new TemplateTransaction(components, accountRecord, indexName, request, response);
			case "view":
				return new ViewTransaction(components, accountRecord, indexName, request, response);
			}
		}
		return null;
	}

	private ServletTransaction doCrawlers(final AccountRecord accountRecord, final String[] pathParts,
			final HttpServletRequest request, final HttpServletResponse response) {
		if (pathParts.length == 2)
			return null;
		final String crawlerType = pathParts[2];
		switch (crawlerType) {
		case "web":
			return doCrawlerWeb(accountRecord, pathParts, request, response);
		default:
			return null;
		}
	}

	private ServletTransaction doCrawlerWeb(final AccountRecord accountRecord, final String[] pathParts,
			final HttpServletRequest request, final HttpServletResponse response) {
		if (pathParts.length == 3)
			return new WebCrawlListTransaction(components, accountRecord, request, response);
		final UUID webCrawlUuid = UUID.fromString(pathParts[3]);
		if (pathParts.length == 4)
			return new WebCrawlEditTransaction(components, accountRecord, webCrawlUuid, request, response);
		if (pathParts.length == 5 && "tasks".equals(pathParts[4]))
			return new WebCrawlTasksTransaction(components, accountRecord, webCrawlUuid, request, response);
		return null;
	}

	private ServletTransaction doTasks(final AccountRecord accountRecord, final String[] pathParts,
			final HttpServletRequest request, final HttpServletResponse response) {
		if (pathParts.length == 2)
			return new TaskListTransaction(components, accountRecord, request, response);
		if (pathParts.length == 3) {
			final String part3 = pathParts[2];
			if ("planning".equals(part3))
				return new TaskPlanningTransaction(components, accountRecord, request, response);
			else
				return new TaskStatusTransaction(components, accountRecord, part3, request, response);
		}
		return null;
	}

}
