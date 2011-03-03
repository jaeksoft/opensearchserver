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

package com.jaeksoft.searchlib.crawler.web.screenshot;

import java.io.File;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.firefox.FirefoxDriver;

import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.process.ThreadAbstract;

public class ScreenshotThread extends ThreadAbstract {

	private String url;
	private byte[] data;
	private int width, height;
	private FirefoxDriver driver;
	private File destFile;

	private final ReentrantLock lock = new ReentrantLock();

	public ScreenshotThread(Config config, String url, int width, int height,
			File destFile) {
		super(config, null);
		this.destFile = destFile;
		driver = null;
		this.url = url;
		data = null;
		this.width = width;
		this.height = height;
	}

	private void initDriver() {
		lock.lock();
		try {
			driver = new FirefoxDriver();
		} finally {
			lock.unlock();
		}

	}

	@Override
	public void runner() throws Exception {
		try {
			initDriver();
			driver.get(url);
			data = driver.getScreenshotAs(OutputType.BYTES);
			if (destFile != null)
				FileUtils.writeByteArrayToFile(destFile, data);

		} finally {
			release();
		}
	}

	public File getPngFile() {
		return destFile;
	}

	@Override
	public void release() {
		lock.lock();
		try {
			if (driver == null)
				return;
			driver.quit();
			driver = null;
		} finally {
			lock.unlock();
		}
	}

}
