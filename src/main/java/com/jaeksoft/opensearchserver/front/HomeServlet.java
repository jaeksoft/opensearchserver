/*
 * Copyright 2017 Emmanuel Keller / Jaeksoft
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jaeksoft.opensearchserver.front;

import com.jaeksoft.opensearchserver.services.IndexesService;
import com.qwazr.library.freemarker.FreeMarkerTool;
import com.qwazr.utils.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("")
public class HomeServlet extends BaseServlet {

	private final static String TEMPLATE = "home.ftl";

	public HomeServlet(final FreeMarkerTool freemarker, final IndexesService indexesService) {
		super(freemarker, indexesService);
	}

	@Override
	protected ServletTransaction getServletTransaction(final HttpServletRequest request,
			final HttpServletResponse response) {
		return new Transaction(request, response);
	}

	class Transaction extends ServletTransaction {

		Transaction(final HttpServletRequest request, final HttpServletResponse response) {
			super(HomeServlet.this, request, response);
		}

		@Override
		void doPost() throws IOException, ServletException {
			final String action = request.getParameter("action");
			final String indexName = request.getParameter("indexName");
			if ("create".equals(action) && !StringUtils.isBlank(indexName))
				indexesService.createIndex(indexName);
			doGet();
		}

		@Override
		void doGet() throws IOException, ServletException {
			request.setAttribute("indexes", indexesService.getIndexes());
			doTemplate(TEMPLATE);
		}
	}
}
