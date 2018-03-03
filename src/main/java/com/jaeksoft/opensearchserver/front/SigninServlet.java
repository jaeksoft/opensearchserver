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
import com.qwazr.utils.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/signin")
public class SigninServlet extends BaseServlet {

	private final Components components;

	private final static String TEMPLATE = "signin.ftl";

	public SigninServlet(final Components components) {
		this.components = components;
	}

	@Override
	protected ServletTransaction getServletTransaction(final HttpServletRequest request,
			final HttpServletResponse response) {
		return new Transaction(request, response);
	}

	class Transaction extends ServletTransaction {

		Transaction(final HttpServletRequest request, final HttpServletResponse response) {
			super(components, request, response, false);
		}

		@Override
		protected String getTemplate() {
			return TEMPLATE;
		}

		public void signin() throws ServletException, IOException {
			request.login(request.getParameter("email"), request.getParameter("current-pwd"));
			final String url = request.getParameter("url");
			if (request.getUserPrincipal() != null) {
				addMessage(Message.Css.success, "Welcome Back !", null);
				response.sendRedirect(StringUtils.isBlank(url) ? "/accounts" : url);
				return;
			}
			request.setAttribute("url", url);
			doGet();
		}

	}
}
