/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2012 Emmanuel Keller / Jaeksoft
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
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import javax.imageio.ImageIO;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.common.database.PropertyItem;
import com.jaeksoft.searchlib.crawler.common.database.PropertyItemListener;
import com.jaeksoft.searchlib.crawler.web.database.CredentialItem;
import com.jaeksoft.searchlib.crawler.web.database.WebPropertyManager;
import com.jaeksoft.searchlib.util.LastModifiedAndSize;
import com.jaeksoft.searchlib.util.Md5Spliter;

public class ScreenshotManager implements PropertyItemListener {

	private File screenshotDir;

	private Config config;

	private final ReentrantLock captureLock = new ReentrantLock();

	private ScreenshotMethodEnum screenshotMethodEnum;

	private ScreenshotMethod screenshotMethod;

	private Dimension captureDimension;

	private Dimension resizeDimension;

	public ScreenshotManager(Config config) throws SearchLibException {
		this.config = config;
		screenshotDir = new File(config.getDirectory(), "screenshot");
		if (!screenshotDir.exists())
			screenshotDir.mkdir();
		screenshotMethodEnum = new ScreenshotMethodEnum();
		WebPropertyManager props = config.getWebPropertyManager();
		screenshotMethod = getMethod(props);
		captureDimension = getCaptureDimension(props);
		resizeDimension = getResizeDimension(props);
		props.getScreenshotCaptureHeight().addListener(this);
		props.getScreenshotCaptureWidth().addListener(this);
		props.getScreenshotResizeHeight().addListener(this);
		props.getScreenshotResizeWidth().addListener(this);
		props.getScreenshotMethod().addListener(this);
	}

	private final File buildFile(URL url) throws SearchLibException {
		try {
			String md5host = Md5Spliter.getMD5Hash(url.getHost());
			File dirPath = new File(screenshotDir, md5host.substring(0, 1)
					+ File.separator + md5host.substring(1, 2));
			return new File(dirPath,
					Md5Spliter.getMD5Hash(url.toExternalForm()) + ".png");
		} catch (NoSuchAlgorithmException e) {
			throw new SearchLibException(e);
		}
	}

	public final File getPngFile(URL url) throws SearchLibException {
		File file = buildFile(url);
		return file.exists() ? file : null;
	}

	public final BufferedImage getImage(URL url) throws SearchLibException,
			IOException {
		File file = buildFile(url);
		return file.exists() ? ImageIO.read(file) : null;
	}

	public List<ScreenshotMethod> getMethodList() {
		return screenshotMethodEnum.getList();
	}

	private ScreenshotMethod getMethod(WebPropertyManager props) {
		String name = props.getScreenshotMethod().getValue();
		ScreenshotMethod method = screenshotMethodEnum.getValue(name);
		if (method == null)
			method = screenshotMethodEnum.getFirst();
		return method;
	}

	public ScreenshotMethod getMethod() throws SearchLibException {
		return screenshotMethod;
	}

	private Dimension getCaptureDimension(WebPropertyManager props)
			throws SearchLibException {
		Dimension dimension = new Dimension(props.getScreenshotCaptureWidth()
				.getValue(), props.getScreenshotCaptureHeight().getValue());
		return dimension;
	}

	public Dimension getCaptureDimension() {
		return captureDimension;
	}

	private Dimension getResizeDimension(WebPropertyManager props) {
		Dimension dimension = new Dimension(props.getScreenshotResizeWidth()
				.getValue(), props.getScreenshotResizeHeight().getValue());
		return dimension;
	}

	public Dimension getResizeDimension() {
		return resizeDimension;
	}

	public void setMethod(ScreenshotMethod method) throws IOException,
			SearchLibException {
		config.getWebPropertyManager().getScreenshotMethod()
				.setValue(method.getName());
	}

	@Override
	public void hasBeenSet(PropertyItem<?> prop) throws SearchLibException {
		WebPropertyManager props = config.getWebPropertyManager();
		if (prop == props.getScreenshotCaptureHeight()
				|| prop == props.getScreenshotCaptureWidth())
			captureDimension = getCaptureDimension(props);
		else if (prop == props.getScreenshotResizeHeight()
				|| prop == props.getScreenshotResizeWidth())
			resizeDimension = getResizeDimension(props);
		else if (prop == props.getScreenshotMethod())
			screenshotMethod = getMethod(props);
	}

	public ScreenshotThread capture(URL url, CredentialItem credentialItem,
			boolean waitForEnd, int secTimeOut) throws SearchLibException {
		if (!screenshotMethod.doScreenshot(url))
			return null;
		captureLock.lock();
		try {
			ScreenshotThread thread = new ScreenshotThread(config, this, url,
					credentialItem);
			thread.execute();
			if (waitForEnd)
				thread.waitForEnd(secTimeOut);
			return thread;
		} finally {
			captureLock.unlock();
		}
	}

	public void store(URL url, BufferedImage image) throws SearchLibException,
			IOException {
		File file = buildFile(url);
		File parentDir = file.getParentFile();
		if (!parentDir.exists())
			parentDir.mkdirs();
		ImageIO.write(image, "png", file);
	}

	public void delete(URL url) throws SearchLibException {
		getPngFile(url).delete();
	}

	public void delete(List<String> urlList) throws SearchLibException {
		for (String u : urlList) {
			try {
				URL url = new URL(u);
				File f = getPngFile(url);
				if (f != null)
					getPngFile(url).delete();
			} catch (MalformedURLException e) {
				Logging.warn(e);
			}
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
		return new LastModifiedAndSize(screenshotDir, true);
	}

}
