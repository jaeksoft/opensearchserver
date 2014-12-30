/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2014 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of OpenSearchServer.
 *
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/
package com.jaeksoft.searchlib.web.servlet.ui;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import freemarker.template.TemplateException;

class UITransaction {

	final static String SERVLET_KEY = "request";
	final static String SESSION_KEY = "session";

	final HttpServletRequest request;
	final HttpServletResponse response;
	final Map<String, Object> variables;
	final UISession session;

	UITransaction(HttpServletRequest request, HttpServletResponse response) {
		this.request = request;
		this.response = response;
		this.session = new UISession(request.getSession());
		variables = new HashMap<String, Object>();
		variables.put(SERVLET_KEY, request);
		variables.put(SESSION_KEY, session);
	}

	void redirectContext(String url) throws IOException {
		response.sendRedirect(request.getContextPath() + url);
	}

	void template(String templatePath) throws IOException, TemplateException {
		TemplateManager.INSTANCE.template(templatePath, variables, response);
	}

}
