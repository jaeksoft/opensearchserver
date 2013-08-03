/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of OpenSearchServer.
 *
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.web;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import org.apache.http.client.ClientProtocolException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.spider.HttpDownloader;
import com.jaeksoft.searchlib.renderer.Renderer;
import com.jaeksoft.searchlib.renderer.Viewer;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.user.User;
import com.jaeksoft.searchlib.web.controller.CommonController;

public class ViewerServlet extends AbstractServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3091105613577638419L;

	private String getContentType(Client client, String uri)
			throws SearchLibException, ClientProtocolException,
			MalformedURLException, IOException, URISyntaxException {
		HttpDownloader httpDownloader = client.getWebCrawlMaster()
				.getNewHttpDownloader(false);
		try {
			httpDownloader.head(new URI(uri), client.getWebCredentialManager()
					.getCredential(uri));
			return httpDownloader.getContentBaseType();
		} finally {
			if (httpDownloader != null)
				httpDownloader.release();
		}
	}

	@Override
	protected void doRequest(ServletTransaction transaction)
			throws ServletException {
		try {
			User user = transaction.getLoggedUser();
			if (user != null
					&& !user.hasRole(transaction.getIndexName(),
							Role.INDEX_QUERY))
				throw new SearchLibException("Not permitted");

			Client client = transaction.getClient();

			Renderer renderer = client.getRendererManager().get(
					transaction.getParameterString("renderer"));
			if (renderer == null)
				throw new SearchLibException("The renderer has not been found");
			String sUri = transaction.getParameterString("uri");
			String contentType = transaction.getParameterString("contentType");
			if (contentType == null)
				contentType = getContentType(client, sUri);
			transaction.setRequestAttribute("uri", sUri);
			transaction.setRequestAttribute("viewer",
					Viewer.getInstance(contentType));
			transaction.setRequestAttribute("renderer", renderer);
			transaction.forward("/WEB-INF/jsp/viewer.jsp");
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	public static String doViewer(String renderer, String uri, String url)
			throws UnsupportedEncodingException {
		StringBuffer sb = CommonController.getApiUrl("/renderer");
		if (renderer != null) {
			sb.append("&renderer=");
			sb.append(URLEncoder.encode(renderer, "UTF-8"));
		}
		if (uri != null) {
			sb.append("&uri=");
			sb.append(URLEncoder.encode(uri, "UTF-8"));
		}
		if (url != null) {
			sb.append("&url=");
			sb.append(URLEncoder.encode(uri, "UTF-8"));
		}
		return sb.toString();
	}
}
