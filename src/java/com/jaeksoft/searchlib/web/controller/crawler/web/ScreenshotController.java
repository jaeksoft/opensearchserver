/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2011 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer.  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.web.controller.crawler.web;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

import org.apache.lucene.queryParser.ParseException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.screenshot.ScreenshotThread;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.web.ScreenshotServlet;
import com.jaeksoft.searchlib.web.controller.crawler.CrawlerController;

public class ScreenshotController extends CrawlerController {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5859798476907958475L;

	private String url;

	private int captureWidth;

	private int captureHeight;

	private int resizeWidth;

	private int resizeHeight;

	private ScreenshotThread currentScreenshotThread;

	public ScreenshotController() throws SearchLibException {
		super();
		reset();
	}

	@Override
	public void reset() {
		url = null;
		currentScreenshotThread = null;
		setCaptureWidth(1024);
		setCaptureHeight(768);
		setResizeWidth(240);
		setResizeHeight(180);
	}

	/**
	 * @param url
	 *            the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param captureWidth
	 *            the captureWidth to set
	 */
	public void setCaptureWidth(int captureWidth) {
		this.captureWidth = captureWidth;
	}

	/**
	 * @param captureHeight
	 *            the captureHeight to set
	 */
	public void setCaptureHeight(int captureHeight) {
		this.captureHeight = captureHeight;
	}

	/**
	 * @return the captureHeight
	 */
	public int getCaptureHeight() {
		return captureHeight;
	}

	/**
	 * @return the captureWidth
	 */
	public int getCaptureWidth() {
		return captureWidth;
	}

	/**
	 * @param resizeWidth
	 *            the resizeWidth to set
	 */
	public void setResizeWidth(int resizeWidth) {
		this.resizeWidth = resizeWidth;
	}

	/**
	 * @param resizeHeight
	 *            the resizeHeight to set
	 */
	public void setResizeHeight(int resizeHeight) {
		this.resizeHeight = resizeHeight;
	}

	/**
	 * @return the resizeHeight
	 */
	public int getResizeHeight() {
		return resizeHeight;
	}

	/**
	 * @return the captureWidth
	 */
	public int getResizeWidth() {
		return resizeWidth;
	}

	public void onCapture() throws SearchLibException, ParseException,
			IOException, SyntaxError, URISyntaxException,
			ClassNotFoundException, InterruptedException,
			InstantiationException, IllegalAccessException {
		synchronized (this) {
			if (currentScreenshotThread != null
					&& currentScreenshotThread.isRunning())
				throw new SearchLibException("A capture is already running");
			currentScreenshotThread = getClient().getScreenshotManager()
					.capture(url, captureWidth, captureHeight, resizeWidth,
							resizeHeight);
			currentScreenshotThread.waitForStart(60);
			reloadPage();
		}
	}

	public void onTimer() {
		reloadPage();
	}

	public boolean isRefresh() {
		synchronized (this) {
			if (currentScreenshotThread == null)
				return false;
			return currentScreenshotThread.isRunning();
		}
	}

	public BufferedImage getImage() {
		synchronized (this) {
			if (currentScreenshotThread == null)
				return null;
			return currentScreenshotThread.getImage();
		}
	}

	public boolean isImageAvailable() {
		return getImage() != null;
	}

	public String getApiCaptureUrl() throws SearchLibException,
			UnsupportedEncodingException {
		Client client = getClient();
		if (client == null)
			return null;
		if (url == null)
			return null;
		return ScreenshotServlet.captureUrl(getBaseUrl(), client,
				getLoggedUser(), url, captureWidth, captureHeight, resizeWidth,
				resizeHeight);
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
}
