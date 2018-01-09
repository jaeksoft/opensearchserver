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

import com.qwazr.library.freemarker.FreeMarkerTool;
import freemarker.template.TemplateException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

abstract class ServletTransaction<T extends BaseServlet> {

	private final FreeMarkerTool freemarker;
	protected final HttpServletRequest request;
	protected final HttpServletResponse response;
	protected final HttpSession session;

	ServletTransaction(final FreeMarkerTool freemarker, final HttpServletRequest request,
			final HttpServletResponse response) {
		this.freemarker = freemarker;
		this.request = request;
		this.response = response;
		this.session = request.getSession();
	}

	/**
	 * @return the message list
	 */
	synchronized List<Message> getMessages() {
		List<Message> messages = (List<Message>) session.getAttribute("messages");
		if (messages == null) {
			messages = new ArrayList<>();
			session.setAttribute("messages", messages);
		}
		return messages;
	}

	/**
	 * Returns the parameter value from the HTTP request or the default value.
	 *
	 * @param parameterName the name of the parameter
	 * @param defaultValue  a default value
	 * @return the value of the given parameter
	 */
	protected Integer getRequestParameter(String parameterName, Integer defaultValue) {
		final String value = request.getParameter(parameterName);
		return value == null ? defaultValue : Integer.parseInt(value);
	}

	/**
	 * This default implementation for HTTP GET method returns a 405 error code.
	 *
	 * @throws IOException      if any I/O error occured
	 * @throws ServletException if any Servlet error occured
	 */
	void doGet() throws IOException, ServletException {
		response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	}

	/**
	 * This default implementation for HTTP POST method returns a 405 error code.
	 *
	 * @throws IOException
	 * @throws ServletException
	 */
	void doPost() throws IOException, ServletException {
		response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	}

	/**
	 * Display a Freemarker template
	 *
	 * @param templatePath the path to the freemarker template
	 * @throws IOException      if any I/O exception occured
	 * @throws ServletException if any templace exception occured
	 */
	protected void doTemplate(String templatePath) throws IOException, ServletException {
		try {
			final List<Message> messages = getMessages();
			request.setAttribute("messages", messages);
			freemarker.template(templatePath, request, response);
			messages.clear();
		} catch (TemplateException e) {
			throw new ServletException(e);
		}
	}

	/**
	 * Add a message. The message may be displayed to the user.
	 *
	 * @param css     the CSS type to use
	 * @param title   the title of the message
	 * @param message the content of the message
	 */
	protected void addMessage(final Css css, final String title, final String message) {
		getMessages().add(new Message(css, title, message));
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
