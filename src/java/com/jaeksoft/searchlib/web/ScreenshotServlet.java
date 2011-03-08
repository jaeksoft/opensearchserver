/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2011 Emmanuel Keller / Jaeksoft
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

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.database.CredentialItem;
import com.jaeksoft.searchlib.crawler.web.database.CredentialManager;
import com.jaeksoft.searchlib.crawler.web.screenshot.ScreenshotManager;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.user.User;

public class ScreenshotServlet extends AbstractServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3693856378071358552L;

	private void doCapture(ServletTransaction transaction,
			ScreenshotManager screenshotManager,
			CredentialManager credentialManager, URL url)
			throws SearchLibException, MalformedURLException {
		if (!screenshotManager.getMethod().doScreenshot(url))
			throw new SearchLibException(
					"The capture is not allowed by the current method");
		CredentialItem credentialItem = credentialManager.getCredential(url
				.toExternalForm());
		screenshotManager.capture(url, credentialItem, true, 300);
		transaction.addXmlResponse("Status", "OK");
	}

	private void doImage(ServletTransaction transaction,
			ScreenshotManager screenshotManager, URL url)
			throws SearchLibException {
		File file = screenshotManager.getPngFile(url);
		if (file == null) {
			transaction.addXmlResponse("Error", "File not found");
			return;
		}
		transaction.sendFile(file, "image/png");
	}

	public final static String captureUrl(String baseUrl, Client client,
			User user, URL screenshotUrl) throws UnsupportedEncodingException {
		StringBuffer sb = getApiUrl(baseUrl, "/screenshot", client, user);
		sb.append("&action=capture&url=");
		sb.append(URLEncoder.encode(screenshotUrl.toExternalForm(), "UTF-8"));
		return sb.toString();
	}

	public final static String imageUrl(String baseUrl, Client client,
			User user, URL screenshotUrl) throws UnsupportedEncodingException {
		StringBuffer sb = getApiUrl(baseUrl, "/screenshot", client, user);
		sb.append("&action=image&url=");
		sb.append(URLEncoder.encode(screenshotUrl.toExternalForm(), "UTF-8"));
		return sb.toString();
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
			ScreenshotManager screenshotManager = client.getScreenshotManager();
			CredentialManager credentialManager = client
					.getWebCredentialManager();
			String action = transaction.getParameterString("action");
			URL url = new URL(transaction.getParameterString("url"));
			if ("capture".equalsIgnoreCase(action))
				doCapture(transaction, screenshotManager, credentialManager,
						url);
			if ("image".equalsIgnoreCase(action))
				doImage(transaction, screenshotManager, url);

		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

}
