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
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.lucene.util.IOUtils;
import org.htmlcleaner.XPatherException;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Timeouts;
import org.openqa.selenium.WebElement;
import org.xml.sax.SAXException;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.spider.HtmlArchiver;
import com.jaeksoft.searchlib.crawler.web.spider.HttpDownloader;
import com.jaeksoft.searchlib.script.commands.Selectors;
import com.jaeksoft.searchlib.script.commands.Selectors.Selector;

public abstract class BrowserDriver<T extends WebDriver> implements Closeable {

	protected T driver = null;

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

	public String javascript(String javascript, boolean faultTolerant,
			Object... objects) throws IOException, SearchLibException {
		try {
			if (!(driver instanceof JavascriptExecutor))
				throw new IOException(
						"The Web driver don't support javascript execution");
			JavascriptExecutor js = (JavascriptExecutor) driver;
			return (String) js.executeScript(javascript, objects);
		} catch (IOException e) {
			if (!faultTolerant)
				throw e;
			Logging.warn(e);
		} catch (Exception e) {
			if (!faultTolerant)
				throw new SearchLibException(e);
			Logging.warn(e);
		}
		return null;
	}

	private static String XPATH_SCRIPT = null;

	private final synchronized static String getXPath() throws IOException {
		if (XPATH_SCRIPT != null)
			return XPATH_SCRIPT;
		URL url = Resources
				.getResource("/com/jaeksoft/searchlib/crawler/web/browser/get_xpath.js");
		String content = Resources.toString(url, Charsets.UTF_8);
		BufferedReader br = new BufferedReader(new StringReader(content));
		StringBuffer sb = new StringBuffer();
		String line;
		while ((line = br.readLine()) != null)
			sb.append(line.trim());
		br.close();
		XPATH_SCRIPT = sb.toString();
		return XPATH_SCRIPT;
	}

	public String getXPath(WebElement webElement, boolean faultTolerant)
			throws IOException, SearchLibException {
		String xPath = javascript(getXPath(), faultTolerant, webElement);
		if (xPath == null)
			Logging.warn("XPATH extraction failed on " + webElement);
		return xPath;
	}

	public boolean clearElement(WebElement webElement, boolean faultTolerant)
			throws IOException, SearchLibException {
		javascript("arguments[0].innerHTML = \"\";", faultTolerant, webElement);
		return true;
	}

	final public BufferedImage getScreenshot() throws IOException {
		if (!(driver instanceof TakesScreenshot))
			throw new IOException(
					"This browser driver does not support screenshot");
		TakesScreenshot takesScreenshot = (TakesScreenshot) driver;
		byte[] data = takesScreenshot.getScreenshotAs(OutputType.BYTES);
		return ImageIO.read(new ByteArrayInputStream(data));
	}

	public String getSourceCode() {
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

	final public void setTimeouts(Integer pageLoad, Integer script) {
		Timeouts timeOuts = driver.manage().timeouts();
		timeOuts.pageLoadTimeout(pageLoad, TimeUnit.SECONDS);
		timeOuts.setScriptTimeout(script, TimeUnit.SECONDS);
	}

	final public int locateBy(Selectors.Selector selector, Set<WebElement> set,
			boolean faultTolerant) throws SearchLibException {
		try {
			List<WebElement> list = driver.findElements(selector.getBy());
			if (list == null)
				return 0;
			for (WebElement element : list)
				set.add(element);
			return list.size();
		} catch (Exception e) {
			if (!faultTolerant)
				throw new SearchLibException(selector.type.name() + " failed: "
						+ selector.query, e);
			Logging.warn(e);
			return 0;
		}
	}

	final public void saveArchive(HttpDownloader httpDownloader,
			File parentDirectory, Collection<Selector> selectors)
			throws ClientProtocolException, IllegalStateException, IOException,
			SearchLibException, URISyntaxException, SAXException,
			ParserConfigurationException, ClassCastException,
			ClassNotFoundException, InstantiationException,
			IllegalAccessException, XPatherException {

		URL currentURL = new URL(driver.getCurrentUrl());
		StringReader reader = null;
		try {
			HtmlArchiver archiver = new HtmlArchiver(this, parentDirectory,
					httpDownloader, currentURL);
			Set<WebElement> webElements = new HashSet<WebElement>();
			Set<String> xPathDisableScriptSet = new HashSet<String>();
			if (selectors != null)
				for (Selector selector : selectors)
					if (selector.disableScript)
						locateBy(selector, webElements, true);
			for (WebElement webElement : webElements) {
				// If it is a script, just use the clear method of the web
				// element
				if ("script".equalsIgnoreCase(webElement.getTagName()))
					clearElement(webElement, true);
				else {
					// Try to find the XPATH
					String xPath = getXPath(webElement, true);
					if (xPath != null)
						xPathDisableScriptSet.add(xPath);
				}
			}
			archiver.archive(this, xPathDisableScriptSet);
		} finally {
			if (reader != null)
				IOUtils.close(reader);
		}
	}

	final public String getFrameSource(WebElement frameWebelement) {
		driver.switchTo().frame(frameWebelement);
		String source = driver.getPageSource();
		driver.switchTo().defaultContent();
		return source;
	}

	final public void getFrameSource(WebElement frameWebelement,
			File captureDirectory) throws IOException {
		if (!captureDirectory.exists())
			captureDirectory.mkdir();
		File sourceFile = new File(captureDirectory, "source.html");
		FileUtils.write(sourceFile, getFrameSource(frameWebelement));
	}

}
