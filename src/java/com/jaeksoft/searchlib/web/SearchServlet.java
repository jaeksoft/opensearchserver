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

import javax.servlet.http.HttpServletRequest;

import org.apache.lucene.queryParser.ParseException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.remote.StreamReadObject;
import com.jaeksoft.searchlib.remote.UriWriteObject;
import com.jaeksoft.searchlib.render.Render;
import com.jaeksoft.searchlib.render.RenderJsp;
import com.jaeksoft.searchlib.render.RenderObject;
import com.jaeksoft.searchlib.render.RenderXml;
import com.jaeksoft.searchlib.request.Request;
import com.jaeksoft.searchlib.result.Result;

public class SearchServlet extends AbstractServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2241064786260022955L;

	private Render doObjectRequest(Client client, HttpServletRequest httpRequest)
			throws ServletException {
		StreamReadObject sro = null;
		try {
			sro = new StreamReadObject(httpRequest.getInputStream());
			Request request = (Request) sro.read();
			Result result = client.search(request);
			return new RenderObject(result);
		} catch (Exception e) {
			throw new ServletException(e);
		} finally {
			if (sro != null)
				sro.close();
		}
	}

	private Render doQueryRequest(Client client,
			HttpServletRequest httpRequest, String render, String jsp)
			throws IOException, ParseException, SyntaxError, URISyntaxException {
		Request request = client.getNewRequest(httpRequest);
		Result result = client.search(request);
		if ("jsp".equals(render) && jsp != null)
			return new RenderJsp(jsp, result);
		return new RenderXml(result);
	}

	@Override
	protected void doRequest(ServletTransaction servletTransaction)
			throws ServletException {

		try {

			HttpServletRequest httpRequest = servletTransaction
					.getServletRequest();
			Client client = Client.getWebAppInstance();

			Render render = null;
			String p = httpRequest.getParameter("render");
			if ("object".equalsIgnoreCase(p))
				render = doObjectRequest(client, httpRequest);
			else
				render = doQueryRequest(client, httpRequest, p, httpRequest
						.getParameter("jsp"));

			render.render(servletTransaction);

			// TODO Info should be queryString
			servletTransaction.setInfo(httpRequest.toString());

		} catch (Exception e) {
			throw new ServletException(e);
		}

	}

	public static Result search(URI uri, Request req, String indexName)
			throws IOException, URISyntaxException {
		uri = buildUri(uri, "/select", "render=object");
		UriWriteObject uwo = null;
		IOException err = null;
		Result res = null;
		try {
			uwo = new UriWriteObject(uri, req);
			if (uwo.getResponseCode() != 200)
				throw new IOException(uwo.getResponseMessage());
			res = (Result) uwo.getResponseObject();
			res.setRequest(req);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			e.printStackTrace();
			err = e;
		} finally {
			if (uwo != null)
				uwo.close();
			if (err != null)
				throw err;
		}
		return res;
	}
}
