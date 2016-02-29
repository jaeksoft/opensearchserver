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
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.imageio.ImageIO;

import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.web.browser.BrowserDriverEnum;
import com.jaeksoft.searchlib.crawler.web.database.CredentialItem;
import com.jaeksoft.searchlib.crawler.web.database.WebPropertyManager;
import com.jaeksoft.searchlib.util.LastModifiedAndSize;
import com.jaeksoft.searchlib.util.LinkUtils;
import com.jaeksoft.searchlib.util.Md5Spliter;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.SimpleLock;
import com.jaeksoft.searchlib.util.properties.PropertyItem;
import com.jaeksoft.searchlib.util.properties.PropertyItemListener;

public class ScreenshotManager implements PropertyItemListener {

	private File screenshotDir;

	private Config config;

	private final ReadWriteLock rwl = new ReadWriteLock();

	private final SimpleLock captureLock = new SimpleLock();

	private ScreenshotMethodEnum screenshotMethodEnum;

	private Dimension captureDimension;

	private Dimension resizeDimension;

	private BrowserDriverEnum browserDriverEnum;

	public ScreenshotManager(Config config) throws SearchLibException, IOException {
		this.config = config;
		screenshotDir = new File(config.getDirectory(), "screenshot");
		if (!screenshotDir.exists())
			screenshotDir.mkdir();
		BrowserDriverEnum defaultBrowserDriverEnum = BrowserDriverEnum
				.find(ClientFactory.INSTANCE.getDefaultWebBrowserDriver().getValue(), BrowserDriverEnum.FIREFOX);
		WebPropertyManager props = config.getWebPropertyManager();
		browserDriverEnum = BrowserDriverEnum.find(props.getScreenshotBrowser().getValue(), defaultBrowserDriverEnum);
		screenshotMethodEnum = ScreenshotMethodEnum.find(props.getScreenshotMethod().getValue());
		captureDimension = getCaptureDimension(props);
		resizeDimension = getResizeDimension(props);
		props.getScreenshotCaptureHeight().addListener(this);
		props.getScreenshotCaptureWidth().addListener(this);
		props.getScreenshotResizeHeight().addListener(this);
		props.getScreenshotResizeWidth().addListener(this);
		props.getScreenshotMethod().addListener(this);
		props.getScreenshotBrowser().addListener(this);
	}

	private final File buildFile(URL url) throws SearchLibException {
		try {
			String md5host = Md5Spliter.getMD5Hash(url.getHost());
			File dirPath = new File(screenshotDir, md5host.substring(0, 1) + File.separator + md5host.substring(1, 2));
			return new File(dirPath, Md5Spliter.getMD5Hash(url.toExternalForm()) + ".png");
		} catch (NoSuchAlgorithmException e) {
			throw new SearchLibException(e);
		} catch (UnsupportedEncodingException e) {
			throw new SearchLibException(e);
		}
	}

	public final File getPngFile(URL url) throws SearchLibException {
		rwl.r.lock();
		try {
			File file = buildFile(url);
			return file.exists() ? file : null;
		} finally {
			rwl.r.unlock();
		}
	}

	public final BufferedImage getImage(URL url) throws SearchLibException, IOException {
		rwl.r.lock();
		try {
			File file = buildFile(url);
			return file.exists() ? ImageIO.read(file) : null;
		} finally {
			rwl.r.unlock();
		}
	}

	private static BrowserDriverEnum getBrowser(WebPropertyManager props) {
		return BrowserDriverEnum.find(props.getScreenshotBrowser().getValue(), BrowserDriverEnum.FIREFOX);
	}

	private static ScreenshotMethodEnum getMethod(WebPropertyManager props) {
		return ScreenshotMethodEnum.find(props.getScreenshotMethod().getValue());
	}

	public ScreenshotMethodEnum getMethod() throws SearchLibException {
		rwl.r.lock();
		try {
			return screenshotMethodEnum;
		} finally {
			rwl.r.unlock();
		}
	}

	private Dimension getCaptureDimension(WebPropertyManager props) {
		Dimension dimension = new Dimension(props.getScreenshotCaptureWidth().getValue(),
				props.getScreenshotCaptureHeight().getValue());
		return dimension;
	}

	public Dimension getCaptureDimension() {
		rwl.r.lock();
		try {
			return captureDimension;
		} finally {
			rwl.r.unlock();
		}
	}

	private Dimension getResizeDimension(WebPropertyManager props) {
		Dimension dimension = new Dimension(props.getScreenshotResizeWidth().getValue(),
				props.getScreenshotResizeHeight().getValue());
		return dimension;
	}

	public Dimension getResizeDimension() {
		rwl.r.lock();
		try {
			return resizeDimension;
		} finally {
			rwl.r.unlock();
		}
	}

	public void setMethod(ScreenshotMethodEnum method) throws IOException, SearchLibException {
		rwl.w.lock();
		try {
			config.getWebPropertyManager().getScreenshotMethod().setValue(method.name());
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	public void hasBeenSet(PropertyItem<?> prop) throws IOException {
		rwl.r.lock();
		try {
			WebPropertyManager props = config.getWebPropertyManager();
			if (prop == props.getScreenshotCaptureHeight() || prop == props.getScreenshotCaptureWidth())
				captureDimension = getCaptureDimension(props);
			else if (prop == props.getScreenshotResizeHeight() || prop == props.getScreenshotResizeWidth())
				resizeDimension = getResizeDimension(props);
			else if (prop == props.getScreenshotMethod())
				screenshotMethodEnum = getMethod(props);
			else if (prop == props.getScreenshotBrowser())
				browserDriverEnum = getBrowser(props);
		} finally {
			rwl.r.unlock();
		}
	}

	public ScreenshotThread capture(URL url, CredentialItem credentialItem, boolean waitForEnd, int secTimeOut)
			throws SearchLibException {
		rwl.r.lock();
		try {
			if (!screenshotMethodEnum.doScreenshot(url))
				return null;
			captureLock.rl.lock();
			try {
				ScreenshotThread thread = new ScreenshotThread(config, this, url, credentialItem, browserDriverEnum);
				thread.execute(180);
				if (waitForEnd)
					thread.waitForEnd(secTimeOut);
				return thread;
			} finally {
				captureLock.rl.unlock();
			}
		} finally {
			rwl.r.unlock();
		}
	}

	public void store(URL url, BufferedImage image) throws SearchLibException, IOException {
		rwl.r.lock();
		try {
			File file = buildFile(url);
			File parentDir = file.getParentFile();
			if (!parentDir.exists())
				parentDir.mkdirs();
			ImageIO.write(image, "png", file);
		} finally {
			rwl.r.unlock();
		}
	}

	public void delete(URL url) throws SearchLibException {
		rwl.r.lock();
		try {
			getPngFile(url).delete();
		} finally {
			rwl.r.unlock();
		}
	}

	public void delete(List<String> urlList) throws SearchLibException {
		rwl.r.lock();
		try {
			for (String u : urlList) {
				try {
					URL url = LinkUtils.newEncodedURL(u);
					File f = getPngFile(url);
					if (f != null)
						getPngFile(url).delete();
				} catch (MalformedURLException e) {
					Logging.warn(e);
				} catch (URISyntaxException e) {
					Logging.warn(e);
				}
			}
		} finally {
			rwl.r.unlock();
		}
	}

	private static final void purge(File directory, long timeLimit) {
		for (File file : directory.listFiles()) {
			if (file.isDirectory()) {
				purge(file, timeLimit);
			} else {
				if (file.lastModified() < timeLimit)
					file.delete();
			}
		}
		if (directory.list().length == 0)
			directory.delete();
	}

	public void purgeOldFiles(long timeLimit) {
		purge(screenshotDir, timeLimit);
	}

	public LastModifiedAndSize getInfos() throws SearchLibException {
		rwl.r.lock();
		try {
			return new LastModifiedAndSize(screenshotDir, true);
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @return the browserDriverEnum
	 */
	public BrowserDriverEnum getBrowserDriver() {
		rwl.r.lock();
		try {
			return browserDriverEnum;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param browserDriverEnum
	 *            the browserDriverEnum to set
	 * @throws SearchLibException
	 * @throws IOException
	 */
	public void setBrowserDriver(BrowserDriverEnum browserDriverEnum) throws IOException, SearchLibException {
		rwl.w.lock();
		try {
			config.getWebPropertyManager().getScreenshotBrowser().setValue(browserDriverEnum.name());
		} finally {
			rwl.w.unlock();
		}
	}

}
