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
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
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
import com.jaeksoft.searchlib.util.SimpleLock;

public class ScreenshotThread extends ThreadAbstract {

	private volatile URL url;
	private volatile Dimension capture;
	private volatile Dimension resize;
	private volatile BrowserDriverEnum browserDriverEnum;
	private volatile BrowserDriver<?> browserDriver;
	private volatile ScreenshotManager screenshotManager;
	private volatile BufferedImage finalImage;
	private volatile CredentialItem credentialItem;

	private final SimpleLock lock = new SimpleLock();

	public ScreenshotThread(Config config, ScreenshotManager screenshotManager,
			URL url, CredentialItem credentialItem,
			BrowserDriverEnum browserDriverEnum) {
		super(config, null);
		this.browserDriverEnum = browserDriverEnum;
		browserDriver = null;
		this.url = url;
		finalImage = null;
		this.screenshotManager = screenshotManager;
		this.capture = screenshotManager.getCaptureDimension();
		this.resize = screenshotManager.getResizeDimension();
		this.credentialItem = credentialItem;
	}

	private final void initDriver() throws SearchLibException {
		lock.rl.lock();
		try {
			browserDriver = browserDriverEnum.getNewInstance();
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		} finally {
			lock.rl.unlock();
		}

	}

	private final BufferedImage scaleWidth(BufferedImage image) {
		BufferedImage scaledImage = new BufferedImage(capture.width,
				image.getHeight(), image.getType());
		Graphics2D graphics2D = scaledImage.createGraphics();
		graphics2D.drawImage(image, (capture.width - image.getWidth()) / 2, 0,
				image.getWidth(), image.getHeight(), null);
		graphics2D.dispose();
		return scaledImage;
	}

	private final BufferedImage scaleHeight(BufferedImage image) {
		BufferedImage scaledImage = new BufferedImage(image.getWidth(),
				capture.height, image.getType());
		Graphics2D graphics2D = scaledImage.createGraphics();
		graphics2D.drawImage(image, 0,
				(capture.height - image.getHeight()) / 2, image.getWidth(),
				image.getHeight(), null);
		graphics2D.dispose();
		return scaledImage;
	}

	private final BufferedImage extractSubImage(BufferedImage image) {
		int left = (image.getWidth() - capture.width) / 2;
		return image.getSubimage(left, 0, capture.width, capture.height);
	}

	private final BufferedImage resizeImage(BufferedImage image) {
		int type = (image.getTransparency() == Transparency.OPAQUE) ? BufferedImage.TYPE_INT_RGB
				: BufferedImage.TYPE_INT_ARGB;
		BufferedImage ret = (BufferedImage) image;
		int w = image.getWidth();
		int h = image.getHeight();

		while (w != resize.width || h != resize.height) {
			if (w > resize.width) {
				w /= 2;
				if (w < resize.width)
					w = resize.width;
			}

			if (h > resize.height) {
				h /= 2;
				if (h < resize.height)
					h = resize.height;
			}

			BufferedImage tmp = new BufferedImage(w, h, type);

			Graphics2D g2 = tmp.createGraphics();
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g2.drawImage(ret, 0, 0, w, h, null);
			g2.dispose();

			ret = tmp;
		}

		return ret;
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
			BufferedImage image = browserDriver.getScreenshot(sUrl);
			if (image.getWidth() < capture.width)
				image = scaleWidth(image);
			if (image.getHeight() < capture.height)
				image = scaleHeight(image);
			image = extractSubImage(image);
			if (resize.width != capture.width
					&& resize.height != capture.height)
				image = resizeImage(image);
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
