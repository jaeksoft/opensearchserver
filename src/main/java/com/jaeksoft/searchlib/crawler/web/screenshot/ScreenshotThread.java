/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.web.screenshot;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.web.browser.BrowserDriver;
import com.jaeksoft.searchlib.crawler.web.browser.BrowserDriverEnum;
import com.jaeksoft.searchlib.crawler.web.database.CredentialItem;
import com.jaeksoft.searchlib.process.ThreadAbstract;
import com.jaeksoft.searchlib.util.ImageUtils;
import com.jaeksoft.searchlib.util.SimpleLock;

public class ScreenshotThread extends ThreadAbstract<ScreenshotThread> {

	private final URL url;
	private final Dimension capture;
	private final Dimension resize;
	private final int reductionPercent;
	private final BrowserDriverEnum browserDriverEnum;
	private final ScreenshotManager screenshotManager;
	private final boolean visiblePartOnly;
	private volatile BrowserDriver<?> browserDriver;
	private volatile BufferedImage finalImage;
	private final CredentialItem credentialItem;
	private final int waitSec;

	private final SimpleLock lock = new SimpleLock();

	public ScreenshotThread(Config config, ScreenshotManager screenshotManager,
			URL url, CredentialItem credentialItem,
			BrowserDriverEnum browserDriverEnum) {
		super(config, null, null, null);
		this.browserDriverEnum = browserDriverEnum;
		this.url = url;
		this.screenshotManager = screenshotManager;
		this.capture = screenshotManager.getCaptureDimension();
		this.resize = screenshotManager.getResizeDimension();
		this.credentialItem = credentialItem;
		this.waitSec = 0;
		this.reductionPercent = 100;
		this.visiblePartOnly = true;
	}

	public ScreenshotThread(Dimension capture, int reduction,
			boolean visiblePartOnly, URL url, int waitSec,
			BrowserDriverEnum browserDriverEnum) {
		super(null, null, null, null);
		this.browserDriverEnum = browserDriverEnum;
		this.url = url;
		this.screenshotManager = null;
		this.credentialItem = null;
		this.capture = capture;
		this.resize = null;
		this.waitSec = waitSec;
		this.reductionPercent = reduction;
		this.visiblePartOnly = visiblePartOnly;
		browserDriver = null;
		finalImage = null;

	}

	private final void initDriver() throws SearchLibException {
		lock.rl.lock();
		try {
			browserDriver = browserDriverEnum.getNewInstance();
			browserDriver.setTimeouts(60, 60);
			browserDriver.setSize(capture.width, capture.height);
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		} finally {
			lock.rl.unlock();
		}

	}

	@Override
	public void runner() throws Exception {
		try {
			initDriver();
			String sUrl;
			if (credentialItem != null) {
				sUrl = new URI(url.getProtocol(),
						credentialItem.getURLUserInfo(), url.getHost(),
						url.getPort(), url.getPath(), url.getQuery(),
						url.getRef()).toString();
			} else
				sUrl = url.toExternalForm();
			browserDriver.get(sUrl);
			if (waitSec > 0)
				sleepSec(waitSec);
			BufferedImage image = browserDriver.getScreenshot();
			if (visiblePartOnly)
				image = ImageUtils.getSubimage(image, 0, 0, capture.width,
						capture.height);

			if (resize != null)
				image = ImageUtils.reduceImage(image, resize.width,
						resize.height);
			if (reductionPercent < 100)
				image = ImageUtils.reduceImage(image, reductionPercent);
			if (screenshotManager != null)
				screenshotManager.store(url, image);
			finalImage = image;
		} finally {
			release();
		}
	}

	public BufferedImage getImage() {
		return finalImage;
	}

	@Override
	public void release() {
		lock.rl.lock();
		try {
			if (browserDriver == null)
				return;
			browserDriver.close();
		} catch (IOException e) {
			Logging.warn(e);
		} finally {
			lock.rl.unlock();
		}
	}

}
