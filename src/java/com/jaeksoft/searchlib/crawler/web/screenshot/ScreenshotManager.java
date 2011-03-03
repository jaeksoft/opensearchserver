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
import java.net.MalformedURLException;
import java.security.NoSuchAlgorithmException;

import org.apache.tomcat.util.net.URL;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.util.Md5Spliter;

public class ScreenshotManager {

	private File screenshotDir;

	private Config config;

	public ScreenshotManager(Config config) {
		this.config = config;
		screenshotDir = new File(config.getDirectory(), "screenshot");
		if (!screenshotDir.exists())
			screenshotDir.mkdir();
	}

	private final File getPngFile(String url) throws MalformedURLException,
			NoSuchAlgorithmException {
		URL u = new URL(url);
		String md5host = Md5Spliter.getMD5Hash(u.getHost().getBytes());
		File dirPath = new File(screenshotDir, md5host.substring(0, 1)
				+ File.separator + md5host.substring(1, 2));
		dirPath.mkdirs();
		return new File(dirPath, Md5Spliter.getMD5Hash(url.getBytes()) + ".png");
	}

	public ScreenshotThread capture(String url, int width, int height)
			throws SearchLibException {
		try {
			ScreenshotThread thread = new ScreenshotThread(config, url, width,
					height, getPngFile(url));
			thread.execute();
			return thread;
		} catch (MalformedURLException e) {
			throw new SearchLibException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new SearchLibException(e);
		}
	}

}
