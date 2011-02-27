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
package com.jaeksoft.searchlib.web.screenshot;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.firefox.FirefoxDriver;

public class ScreenshotManager {

	private File screenshotDir;

	public ScreenshotManager(File indexDir) {
		screenshotDir = new File(indexDir, "screeshot");
		if (!screenshotDir.exists())
			screenshotDir.mkdir();
	}

	public void screenshot(String url, File destFile) throws IOException {

		FirefoxDriver driver = new FirefoxDriver();
		try {
			driver.executeScript("window.resizeTo(1024, 768); window.moveTo(0,0);");
			driver.get(url);
			byte[] data = driver.getScreenshotAs(OutputType.BYTES);
			FileUtils.writeByteArrayToFile(destFile, data);
		} finally {
			driver.quit();
		}
	}

}
