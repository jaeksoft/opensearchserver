/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011 Emmanuel Keller / Jaeksoft
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
import java.util.List;

import org.zkoss.zul.Image;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.database.CredentialItem;
import com.jaeksoft.searchlib.crawler.web.database.WebPropertyManager;
import com.jaeksoft.searchlib.crawler.web.screenshot.ScreenshotManager;
import com.jaeksoft.searchlib.crawler.web.screenshot.ScreenshotMethod;
import com.jaeksoft.searchlib.crawler.web.screenshot.ScreenshotThread;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.util.LastModifiedAndSize;
import com.jaeksoft.searchlib.web.ScreenshotServlet;
import com.jaeksoft.searchlib.web.controller.AlertController;
import com.jaeksoft.searchlib.web.controller.crawler.CrawlerController;

public class ScreenshotController extends CrawlerController {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5859798476907958475L;

	private transient URL url;

	private transient ScreenshotThread currentScreenshotThread;

	private transient BufferedImage currentImage;

	private transient LastModifiedAndSize screenshotInfos;

	public ScreenshotController() throws SearchLibException {
		super();
		reset();
	}

	@Override
	public void reset() {
		url = null;
		currentScreenshotThread = null;
		currentImage = null;
		screenshotInfos = null;
	}

	/**
	 * @param url
	 *            the url to set
	 * @throws MalformedURLException
	 */
	public void setUrl(String url) throws MalformedURLException {
		this.url = url == null ? null : new URL(url);
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
			setImage(null);
			reloadPage();
		}
	}

	public void onCheck() throws SearchLibException, InterruptedException,
			IOException {
		synchronized (this) {
			currentImage = getClient().getScreenshotManager().getImage(url);
			if (currentImage == null)
				new AlertController("Screenshot not found.");
			setImage(currentImage);
			reloadPage();
		}
	}

	public void onTimer() throws SearchLibException {
		if (currentImage == null)
			if (currentScreenshotThread != null)
				setImage(currentScreenshotThread.getImage());
		reloadPage();
	}

	public boolean isRefresh() {
		synchronized (this) {
			if (currentScreenshotThread == null)
				return false;
			return currentScreenshotThread.isRunning();
		}
	}

	public void setImage(BufferedImage img) {
		synchronized (this) {
			currentImage = img;
			Image image = (Image) getFellow("capture").getFellow("img");
			if (img != null)
				image.setContent(img);
		}
	}

	public boolean isImageAvailable() {
		return currentImage != null;
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

	public void onInfos() throws SearchLibException {
		screenshotInfos = getClient().getScreenshotManager().getInfos();
		reloadPage();
	}

	public LastModifiedAndSize getInfos() {
		return screenshotInfos;

	}

	public List<ScreenshotMethod> getMethodList() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		return client.getScreenshotManager().getMethodList();
	}

	public ScreenshotMethod getMethod() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		return client.getScreenshotManager().getMethod();
	}

	public void setMethod(ScreenshotMethod method) throws SearchLibException,
			IOException {
		Client client = getClient();
		if (client == null)
			return;
		client.getScreenshotManager().setMethod(method);
	}
}
