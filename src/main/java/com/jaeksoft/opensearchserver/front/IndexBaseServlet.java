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
import freemarker.template.TemplateException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

abstract class BaseServlet extends HttpServlet {

	private final FreeMarkerTool freemarker;
	private final String templatePath;
	protected final IndexesService indexesService;

	protected BaseServlet(final FreeMarkerTool freemarker, final String templatePath,
			final IndexesService indexesService) {
		this.freemarker = freemarker;
		this.templatePath = templatePath;
		this.indexesService = indexesService;
	}

	protected void doTemplate(final HttpServletRequest request, final HttpServletResponse response)
			throws IOException, ServletException {
		try {
			final List<Message> messages = getMessages(request);
			request.setAttribute("messages", getMessages(request));
			freemarker.template(templatePath, request, response);
			messages.clear();
		} catch (TemplateException e) {
			throw new ServletException(e);
		}
	}

	protected synchronized List<Message> getMessages(final HttpServletRequest request) {
		final HttpSession session = request.getSession();
		List<Message> messages = (List<Message>) session.getAttribute("messages");
		if (messages == null) {
			messages = new ArrayList<>();
			session.setAttribute("messages", messages);
		}
		return messages;
	}

	protected void addMessage(final HttpServletRequest request, final Css css, final String title,
			final String message) {
		getMessages(request).add(new Message(css, title, message));
	}

	public enum Css {
		success, warning, info, danger;
	}

	public static class Message {

		private final Css css;
		private final String title;
		private final String message;

		Message(final Css css, final String title, final String message) {
			this.css = css;
			this.title = title;
			this.message = message;
		}

		public String getCss() {
			return css.name();
		}

		public String getTitle() {
			return title;
		}

		public String getMessage() {
			return message;
		}

	}

}
