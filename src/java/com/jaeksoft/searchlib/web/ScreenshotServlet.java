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
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.screenshot.ScreenshotManager;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.user.User;

public class ScreenshotServlet extends AbstractServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3693856378071358552L;

	private void doCapture(ServletTransaction transaction,
			ScreenshotManager screenshotManager, String url,
			HttpServletRequest request) throws SearchLibException {
		int captureWidth = Integer.parseInt(request
				.getParameter("captureWidth"));
		int captureHeight = Integer.parseInt(request
				.getParameter("captureHeight"));
		int resizeWidth = Integer.parseInt(request.getParameter("resizeWidth"));
		int resizeHeight = Integer.parseInt(request
				.getParameter("resizeHeight"));
		screenshotManager.capture(url, captureWidth, captureHeight,
				resizeWidth, resizeHeight).waitForEnd(300);
		transaction.addXmlResponse("Status", "OK");
	}

	private void doImage(ServletTransaction transaction,
			ScreenshotManager screenshotManager, String url)
			throws SearchLibException {
		File file = screenshotManager.getPngFile(url);
		if (file == null) {
			transaction.addXmlResponse("Error", "File not found");
			return;
		}
		transaction.sendFile(file, "image/png");
	}

	public final static String captureUrl(String baseUrl, Client client,
			User user, String screenshotUrl, int captureWidth,
			int captureHeight, int resizeWidth, int resizeHeight)
			throws UnsupportedEncodingException {
		StringBuffer sb = getApiUrl(baseUrl, "/screenshot", client, user);
		sb.append("&action=capture&url=");
		sb.append(URLEncoder.encode(screenshotUrl, "UTF-8"));
		sb.append("&captureWidth=");
		sb.append(Integer.toString(captureWidth));
		sb.append("&captureHeight=");
		sb.append(Integer.toString(captureHeight));
		sb.append("&resizeWidth=");
		sb.append(Integer.toString(resizeWidth));
		sb.append("&resizeHeight=");
		sb.append(Integer.toString(resizeHeight));
		return sb.toString();
	}

	public final static String imageUrl(String baseUrl, Client client,
			User user, String screenshotUrl)
			throws UnsupportedEncodingException {
		StringBuffer sb = getApiUrl(baseUrl, "/screenshot", client, user);
		sb.append("&action=image&url=");
		sb.append(URLEncoder.encode(screenshotUrl, "UTF-8"));
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
			HttpServletRequest request = transaction.getServletRequest();
			String action = request.getParameter("action");
			String url = request.getParameter("url");
			if ("capture".equalsIgnoreCase(action))
				doCapture(transaction, screenshotManager, url, request);
			if ("image".equalsIgnoreCase(action))
				doImage(transaction, screenshotManager, url);

		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

}
