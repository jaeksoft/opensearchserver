/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft SearchLib Community.
 *
 * Jaeksoft SearchLib Community is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft SearchLib Community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft SearchLib Community. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.web;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpException;

import com.jaeksoft.searchlib.remote.UriRead;
import com.jaeksoft.searchlib.util.Timer;
import com.jaeksoft.searchlib.web.ServletTransaction.Method;

public abstract class AbstractServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	final private static Logger logger = Logger.getLogger(AbstractServlet.class
			.getCanonicalName());

	protected abstract void doRequest(ServletTransaction transaction)
			throws ServletException;

	final private void doRequest(HttpServletRequest request, Method method,
			HttpServletResponse response) {

		ServletTransaction transaction = new ServletTransaction(this, request,
				method, response);

		String p;
		if ((p = request.getParameter("log")) != null)
			logger.setLevel(Level.parse(p.toUpperCase()));

		Timer timer = new Timer();

		try {
			doRequest(transaction);
			timer.end();
			logger.info(this.getClass().getSimpleName()
					+ (transaction.getInfo() == null ? "" : " "
							+ transaction.getInfo()) + " " + timer.duration());
		} catch (Exception e) {
			try {
				response.sendError(500, e.getMessage());
				e.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) {
		doRequest(request, Method.POST, response);
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) {
		doRequest(request, Method.GET, response);
	}

	@Override
	protected void doPut(HttpServletRequest request,
			HttpServletResponse response) {
		doRequest(request, Method.PUT, response);
	}

	protected static URI buildUri(URI uri, String additionalPath,
			String queryString) throws URISyntaxException {
		return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri
				.getPort(), uri.getPath() + additionalPath, queryString, uri
				.getFragment());

	}

	protected static void call(URI uri, String additionalPath,
			String queryString) throws HttpException, IOException,
			URISyntaxException {
		uri = buildUri(uri, additionalPath, queryString);
		UriRead uriRead = null;

		try {
			uriRead = new UriRead(uri);
			if (uriRead.getResponseCode() != 200)
				throw new IOException(uri + " returns "
						+ uriRead.getResponseMessage() + "("
						+ uriRead.getResponseCode() + ")");
		} catch (IOException e) {
			throw e;
		} finally {
			if (uriRead != null)
				uriRead.close();
		}
	}
}
