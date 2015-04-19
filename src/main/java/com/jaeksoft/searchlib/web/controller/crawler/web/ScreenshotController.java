/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2013 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of OpenSearchServer.
 *
 * OpenSearchServer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OpenSearchServer.  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.web.controller.crawler.web;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.browser.BrowserDriverEnum;
import com.jaeksoft.searchlib.crawler.web.database.CredentialItem;
import com.jaeksoft.searchlib.crawler.web.database.WebPropertyManager;
import com.jaeksoft.searchlib.crawler.web.screenshot.ScreenshotManager;
import com.jaeksoft.searchlib.crawler.web.screenshot.ScreenshotMethodEnum;
import com.jaeksoft.searchlib.crawler.web.screenshot.ScreenshotThread;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.util.LastModifiedAndSize;
import com.jaeksoft.searchlib.util.LinkUtils;
import com.jaeksoft.searchlib.web.ScreenshotServlet;
import com.jaeksoft.searchlib.web.controller.AlertController;
import com.jaeksoft.searchlib.web.controller.crawler.CrawlerController;

@AfterCompose(superclass = true)
public class ScreenshotController extends CrawlerController {

	private transient URL url;

	private transient ScreenshotThread currentScreenshotThread;

	private transient LastModifiedAndSize screenshotInfos;

	private boolean showImage;

	public ScreenshotController() throws SearchLibException {
		super();
		reset();
	}

	@Override
	public void reset() {
		url = null;
		currentScreenshotThread = null;
		showImage = false;
		screenshotInfos = null;
	}

	/**
	 * @param url
	 *            the url to set
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	public void setUrl(String url) throws MalformedURLException,
			URISyntaxException {
		this.url = url == null ? null : LinkUtils.newEncodedURL(url);
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url == null ? null : url.toExternalForm();
	}

	public WebPropertyManager getProperties() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		return client.getWebPropertyManager();
	}

	@Command
	public void onCapture() throws SearchLibException, ParseException,
			IOException, SyntaxError, URISyntaxException,
			ClassNotFoundException, InterruptedException,
			InstantiationException, IllegalAccessException {
		synchronized (this) {
			if (currentScreenshotThread != null
					&& currentScreenshotThread.isRunning())
				throw new SearchLibException("A capture is already running");
			Client client = getClient();
			if (client == null)
				return;
			showImage = false;
			ScreenshotManager screenshotManager = client.getScreenshotManager();
			if (!screenshotManager.getMethod().doScreenshot(url)) {
				new AlertController(
						"The capture is not allowed by the current method");
				return;
			}
			CredentialItem credentialItem = client.getWebCredentialManager()
					.getCredential(url.toExternalForm());
			currentScreenshotThread = screenshotManager.capture(url,
					credentialItem, false, 0);
			currentScreenshotThread.waitForStart(60);
			reload();
		}
	}

	@Command
	public void onCheck() throws SearchLibException, InterruptedException,
			IOException {
		synchronized (this) {
			BufferedImage currentImage = getClient().getScreenshotManager()
					.getImage(url);
			if (currentImage == null)
				new AlertController("Screenshot not found.");
			else
				showImage = true;
			reload();
		}
	}

	@Override
	@Command
	public void onTimer() throws SearchLibException {
		if (currentScreenshotThread != null)
			showImage = currentScreenshotThread.getImage() != null;
		reload();
	}

	@Override
	public boolean isRefresh() {
		synchronized (this) {
			if (currentScreenshotThread == null)
				return false;
			return currentScreenshotThread.isRunning();
		}
	}

	public boolean isError() {
		if (currentScreenshotThread == null)
			return false;
		return currentScreenshotThread.getException() != null;
	}

	public String getErrorMessage() {
		if (currentScreenshotThread == null)
			return null;
		Exception e = currentScreenshotThread.getException();
		if (e == null)
			return null;
		return e.getMessage();
	}

	public boolean isImageAvailable() {
		return showImage;
	}

	public String getApiCaptureUrl() throws SearchLibException,
			UnsupportedEncodingException {
		Client client = getClient();
		if (client == null)
			return null;
		if (url == null)
			return null;
		return ScreenshotServlet.captureUrl(getBaseUrl(), client,
				getLoggedUser(), url);
	}

	public String getApiImageUrl() throws SearchLibException,
			UnsupportedEncodingException {
		Client client = getClient();
		if (client == null)
			return null;
		if (url == null)
			return null;
		return ScreenshotServlet.imageUrl(getBaseUrl(), client,
				getLoggedUser(), url);
	}

	@Command
	public void onInfos() throws SearchLibException {
		screenshotInfos = getClient().getScreenshotManager().getInfos();
		reload();
	}

	public LastModifiedAndSize getInfos() {
		return screenshotInfos;

	}

	public ScreenshotMethodEnum[] getMethodList() throws SearchLibException {
		return ScreenshotMethodEnum.values();
	}

	public ScreenshotManager getScreenshotManager() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		return client.getScreenshotManager();
	}

	public BrowserDriverEnum[] getBrowserList() {
		return BrowserDriverEnum.values();
	}
}
