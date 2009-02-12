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

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.remote.StreamReadObject;
import com.jaeksoft.searchlib.remote.UriWriteObject;
import com.jaeksoft.searchlib.render.Render;
import com.jaeksoft.searchlib.render.RenderObject;
import com.jaeksoft.searchlib.request.DocumentsRequest;
import com.jaeksoft.searchlib.result.ResultDocument;

public class DocumentsServlet extends AbstractServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -343590007504141181L;

	/**
	 * 
	 */

	private Render doObjectRequest(Client client, HttpServletRequest httpRequest)
			throws ServletException {
		StreamReadObject sro = null;
		try {
			sro = new StreamReadObject(httpRequest.getInputStream());
			DocumentsRequest documentsRequest = (DocumentsRequest) sro.read();
			ResultDocument[] resultDocuments = client
					.documents(documentsRequest);
			return new RenderObject(resultDocuments);
		} catch (Exception e) {
			throw new ServletException(e);
		} finally {
			if (sro != null)
				sro.close();
		}
	}

	@Override
	protected void doRequest(ServletTransaction servletTransaction)
			throws ServletException {

		try {

			HttpServletRequest httpRequest = servletTransaction
					.getServletRequest();
			Client client = Client.getWebAppInstance();

			Render render = doObjectRequest(client, httpRequest);

			render.render(servletTransaction);

			// TODO Info should be queryString
			servletTransaction.setInfo(httpRequest.toString());

		} catch (Exception e) {
			throw new ServletException(e);
		}

	}

	public static ResultDocument[] documents(URI uri,
			DocumentsRequest documentsRequest) throws IOException,
			URISyntaxException {
		uri = buildUri(uri, "/documents", null);
		UriWriteObject uwo = null;
		IOException err = null;
		ResultDocument[] documentsResult = null;
		try {
			uwo = new UriWriteObject(uri, documentsRequest);
			if (uwo.getResponseCode() != 200)
				throw new IOException(uwo.getResponseMessage());
			documentsResult = (ResultDocument[]) uwo.getResponseObject();
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
		return documentsResult;
	}
}
