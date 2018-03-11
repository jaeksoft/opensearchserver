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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.RedirectionException;
import java.io.IOException;

public abstract class BaseServlet extends HttpServlet {

	protected abstract ServletTransaction getServletTransaction(final HttpServletRequest request,
			final HttpServletResponse response) throws IOException, ServletException;

	private void doTransaction(final HttpServletRequest request, final HttpServletResponse response,
			final DoMethod doMethod) throws IOException, ServletException {
		final ServletTransaction transaction;
		try {
			transaction = getServletTransaction(request, response);
		} catch (RedirectionException e) {
			response.sendRedirect(e.getLocation().toString());
			return;
		} catch (NotFoundException e) {
			response.sendError(404, e.getMessage());
			return;
		} catch (InternalServerErrorException e) {
			response.sendError(500, e.getMessage());
			return;
		}
		if (transaction != null)
			doMethod.apply(transaction);
		else
			response.sendError(404);
	}

	@Override
	final public void doPost(final HttpServletRequest request, final HttpServletResponse response)
			throws IOException, ServletException {
		doTransaction(request, response, ServletTransaction::doPost);
	}

	@Override
	final public void doGet(final HttpServletRequest request, final HttpServletResponse response)
			throws IOException, ServletException {
		doTransaction(request, response, ServletTransaction::doGet);
	}

	@FunctionalInterface
	interface DoMethod {
		void apply(ServletTransaction transaction) throws IOException, ServletException;
	}

}
