/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.web;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;

import org.apache.lucene.queryParser.ParseException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.remote.StreamReadObject;
import com.jaeksoft.searchlib.remote.UriWriteObject;
import com.jaeksoft.searchlib.render.Render;
import com.jaeksoft.searchlib.render.RenderJsp;
import com.jaeksoft.searchlib.render.RenderObject;
import com.jaeksoft.searchlib.render.RenderXml;
import com.jaeksoft.searchlib.request.SearchRequest;
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
			SearchRequest searchRequest = (SearchRequest) sro.read();
			Result result = client.search(searchRequest);
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
			throws IOException, ParseException, SyntaxError,
			URISyntaxException, ClassNotFoundException, InterruptedException,
			SearchLibException, InstantiationException, IllegalAccessException {
		SearchRequest searchRequest = client.getNewSearchRequest(httpRequest);
		Result result = client.search(searchRequest);
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
			Client client = ClientCatalog.getClient(httpRequest);

			Render render = null;
			String p = httpRequest.getParameter("render");
			if ("object".equalsIgnoreCase(p))
				render = doObjectRequest(client, httpRequest);
			else
				render = doQueryRequest(client, httpRequest, p, httpRequest
						.getParameter("jsp"));

			render.render(servletTransaction);

		} catch (Exception e) {
			throw new ServletException(e);
		}

	}

	public static Result search(URI uri, SearchRequest searchRequest,
			String indexName) throws IOException, URISyntaxException {
		uri = buildUri(uri, "/select", null, "render=object");
		UriWriteObject uwo = null;
		IOException err = null;
		Result res = null;
		try {
			uwo = new UriWriteObject(uri, searchRequest);
			if (uwo.getResponseCode() != 200)
				throw new IOException(uwo.getResponseMessage());
			res = (Result) uwo.getResponseObject();
			res.setSearchRequest(searchRequest);
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
