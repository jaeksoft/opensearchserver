/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011 Emmanuel Keller / Jaeksoft
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

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.database.CredentialItem;
import com.jaeksoft.searchlib.crawler.web.database.CredentialManager;
import com.jaeksoft.searchlib.crawler.web.screenshot.ScreenshotManager;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.user.User;
import com.jaeksoft.searchlib.util.LinkUtils;

public class ScreenshotServlet extends AbstractServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3693856378071358552L;

	final public static void doCapture(ServletTransaction transaction,
			ScreenshotManager screenshotManager,
			CredentialManager credentialManager, URL url)
			throws SearchLibException, MalformedURLException,
			URISyntaxException {
		if (!screenshotManager.getMethod().doScreenshot(url))
			throw new SearchLibException(
					"The capture is not allowed by the current method");
		CredentialItem credentialItem = credentialManager.getCredential(url
				.toExternalForm());
		screenshotManager.capture(url, credentialItem, true, 300);
		if (transaction != null)
			transaction.addXmlResponse("Status", "OK");
	}

	final private static String getPublicFileName(File file) {
		StringBuilder sb = new StringBuilder(file.getName());
		File p = file.getParentFile();
		sb.insert(0, p.getName());
		p = p.getParentFile();
		sb.insert(0, p.getName());
		return sb.toString();
	}

	final public static void doImage(ServletTransaction transaction,
			ScreenshotManager screenshotManager, URL url)
			throws SearchLibException {
		File file = screenshotManager.getPngFile(url);
		if (file == null)
			throw new SearchLibException("File not found");
		if (transaction != null)
			transaction.sendFile(file, getPublicFileName(file), "image/png",
					false);
	}

	final public static String doCheck(ScreenshotManager screenshotManager,
			URL url) throws SearchLibException {
		File file = screenshotManager.getPngFile(url);
		if (file == null)
			throw new SearchLibException("File not found");
		return getPublicFileName(file);
	}

	public final static String captureUrl(StringBuilder sbBaseUrl,
			Client client, User user, URL screenshotUrl)
			throws UnsupportedEncodingException {
		StringBuilder sb = getApiUrl(sbBaseUrl, "/screenshot", client, user);
		sb.append("&action=capture&url=");
		sb.append(URLEncoder.encode(screenshotUrl.toExternalForm(), "UTF-8"));
		return sb.toString();
	}

	public final static String imageUrl(StringBuilder sbBbaseUrl,
			Client client, User user, URL screenshotUrl)
			throws UnsupportedEncodingException {
		StringBuilder sb = getApiUrl(sbBbaseUrl, "/screenshot", client, user);
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
			URL url = LinkUtils.newEncodedURL(transaction
					.getParameterString("url"));
			if ("capture".equalsIgnoreCase(action))
				doCapture(transaction, screenshotManager, credentialManager,
						url);
			else if ("image".equalsIgnoreCase(action))
				doImage(transaction, screenshotManager, url);
			else if ("check".equalsIgnoreCase(action))
				transaction.addXmlResponse("Check",
						doCheck(screenshotManager, url));

		} catch (Exception e) {
			throw new ServletException(e);
		}
	}
}
