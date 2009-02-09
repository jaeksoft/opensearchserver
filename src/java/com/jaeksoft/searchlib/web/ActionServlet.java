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

import org.apache.commons.httpclient.HttpException;

import com.jaeksoft.searchlib.Client;

public class ActionServlet extends AbstractServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = -369063857059673597L;

	@Override
	protected void doRequest(ServletTransaction transaction)
			throws ServletException {
		try {
			Client client = Client.getWebAppInstance();
			HttpServletRequest request = transaction.getServletRequest();
			String index = request.getParameter("index");
			String action = request.getParameter("action");
			if ("optimize".equalsIgnoreCase(action))
				client.getIndex().optimize(index);
			else if ("swap".equalsIgnoreCase(action)) {
				String p = request.getParameter("version");
				long version = (p == null) ? 0 : Long.parseLong(p);
				boolean deleteOld = (request.getParameter("deleteOld") != null);
				client.getIndex().swap(index, version, deleteOld);
			} else if ("reload".equalsIgnoreCase(action)) {
				client.getIndex().reload(index);
			} else if ("online".equalsIgnoreCase(action))
				client.getIndex().setOnline(index, true);
			else if ("offline".equalsIgnoreCase(action))
				client.getIndex().setOnline(index, false);
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	public static void optimize(URI uri, String indexName)
			throws HttpException, IOException, URISyntaxException {
		StringBuffer query = new StringBuffer("?action=optimize");
		query.append("&index=");
		query.append(indexName);
		call(uri, "/action", query.toString());
	}

	public static void reload(URI uri, String indexName) throws HttpException,
			IOException, URISyntaxException {
		StringBuffer query = new StringBuffer("?action=reload");
		query.append("&index=");
		query.append(indexName);
		call(uri, "/action", query.toString());
	}

	public static void swap(URI uri, String indexName, long version,
			boolean deleteOld) throws URISyntaxException, HttpException,
			IOException {
		StringBuffer query = new StringBuffer("?action=swap");
		query.append("&index=");
		query.append(indexName);
		query.append("&version=");
		query.append(version);
		if (deleteOld)
			query.append("&deleteOld");
		call(uri, "/action", query.toString());
	}

	public static void online(URI uri, String indexName) throws HttpException,
			IOException, URISyntaxException {
		call(uri, "/action", "?action=online");
	}

	public static void offline(URI uri, String indexName) throws HttpException,
			IOException, URISyntaxException {
		call(uri, "/action", "?action=offline");
	}
}
