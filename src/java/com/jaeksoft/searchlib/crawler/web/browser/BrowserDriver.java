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

package com.jaeksoft.searchlib.crawler.web.browser;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.jaeksoft.searchlib.SearchLibException;

public abstract class BrowserDriver<T extends WebDriver> implements Closeable {

	private T driver = null;

	public BrowserDriver() {
		driver = initialize();
	}

	protected abstract T initialize();

	@Override
	public void close() throws IOException {
		if (driver == null)
			return;
		driver.quit();
		driver = null;
	}

	final public void get(String sUrl) {
		driver.get(sUrl);
	}

	final public BufferedImage getScreenshot() throws IOException {
		if (!(driver instanceof TakesScreenshot))
			throw new IOException(
					"This browser driver does not support screenshot");
		TakesScreenshot takesScreenshot = (TakesScreenshot) driver;
		byte[] data = takesScreenshot.getScreenshotAs(OutputType.BYTES);
		return ImageIO.read(new ByteArrayInputStream(data));
	}

	final public BufferedImage getScreenshot(String sUrl) throws IOException {
		get(sUrl);
		return getScreenshot();
	}

	final public String getSourceCode() {
		return driver.getPageSource();
	}

	final public String getSourceCode(String sUrl) {
		get(sUrl);
		return driver.getPageSource();
	}

	final public String getTitle() {
		return driver.getTitle();
	}

	final public String getTitle(String sUrl) {
		get(sUrl);
		return driver.getTitle();
	}

	final public void setSize(int width, int height) throws SearchLibException {
		driver.manage().window().setSize(new Dimension(width, height));
	}

	final public int locateByCss(String selector, Set<WebElement> set) {
		List<WebElement> list = driver.findElements(new By.ByCssSelector(
				selector));
		if (list == null)
			return 0;
		for (WebElement element : list)
			set.add(element);
		return list.size();
	}

	final public void saveArchive() {
		List<WebElement> elements = driver.findElements(new By.ByXPath(
				"//*[@src]"));
		for (WebElement element : elements) {
			System.out.println(element.getTagName() + " "
					+ element.getAttribute("src"));
		}

	}
}
