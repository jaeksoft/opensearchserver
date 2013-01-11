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

package com.jaeksoft.searchlib.web.controller;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.naming.NamingException;

import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.spider.HttpDownloadThread;

public class ViewerController extends CommonController {

	private int page;

	private int zoom;

	private final static int[] zoomScale = { 10, 20, 30, 40, 50, 60, 70, 80,
			90, 100, 150, 200, 250, 300, 400 };

	private HttpDownloadThread downloadThread;

	private File tempFile;

	public ViewerController() throws SearchLibException, IOException,
			NamingException, URISyntaxException {
		super();
		page = 1;
		zoom = 100;
		String index = getRequestParameter("index");
		String uri = getRequestParameter("uri");
		downloadThread = null;
		if (uri != null) {
			tempFile = File.createTempFile("oss", "pdfviewer");
			Client client = ClientCatalog.getClient(index);
			downloadThread = new HttpDownloadThread(client, uri, tempFile);
			downloadThread.execute();
		}
	}

	@Override
	protected void reset() throws SearchLibException {
	}

	/**
	 * @return the page
	 */
	public int getPage() {
		return page;
	}

	/**
	 * @param page
	 *            the page to set
	 * @throws SearchLibException
	 */
	@NotifyChange({ "currentImage", "page" })
	public void setPage(int page) {
		this.page = page;
	}

	/**
	 * @return the zoom
	 */
	public int getZoom() {
		return zoom;
	}

	/**
	 * @param zoom
	 *            the zoom to set
	 */
	@NotifyChange({ "currentImage", "zoom" })
	public void setZoom(int zoom) {
		this.zoom = zoom;
	}

	@Command
	@NotifyChange({ "currentImage", "page" })
	public void onPageUp() {
		page++;
	}

	@Command
	@NotifyChange({ "currentImage", "page" })
	public void onPageDown() {
		if (page > 1)
			page--;
	}

	@Command
	@NotifyChange({ "currentImage", "zoom" })
	public void onZoomUp() {
		for (int zc : zoomScale) {
			if (zc > zoom) {
				zoom = zc;
				return;
			}
		}
	}

	@Command
	@NotifyChange({ "currentImage", "zoom" })
	public void onZoomDown() {
		int lastzc = zoomScale[0];
		for (int zc : zoomScale) {
			if (zc >= zoom) {
				zoom = lastzc;
				return;
			}
			lastzc = zc;
		}
	}

	@Command
	@NotifyChange("*")
	public void onTimer() {
	}

	public boolean isDownloading() {
		if (downloadThread == null)
			return false;
		return downloadThread.isRunning();
	}

	public boolean isDownloaded() {
		return !isDownloading();
	}

	public boolean isError() {
		if (downloadThread == null)
			return false;
		return downloadThread.getException() != null;
	}

	public String getErrorMessage() {
		if (downloadThread == null)
			return null;
		Exception e = downloadThread.getException();
		if (e == null)
			return null;
		return e.getMessage();
	}

	public int getDownloadPercent() {
		if (downloadThread == null)
			return 0;
		return downloadThread.getPercent();
	}

	public String getCurrentImage() {
		return null;
	}
}
