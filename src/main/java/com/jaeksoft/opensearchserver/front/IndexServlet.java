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

import com.qwazr.library.freemarker.FreeMarkerTool;
import freemarker.template.TemplateException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/")
public class IndexServlet extends HttpServlet {

	private final FreeMarkerTool freemarker;

	public IndexServlet(final FreeMarkerTool freemarker) {
		this.freemarker = freemarker;
	}

	@Override
	public void doGet(final HttpServletRequest request, final HttpServletResponse response)
			throws IOException, ServletException {
		try {
			freemarker.template("index.ftl", request, response);
		} catch (TemplateException e) {
			throw new ServletException(e);
		}
	}
}
