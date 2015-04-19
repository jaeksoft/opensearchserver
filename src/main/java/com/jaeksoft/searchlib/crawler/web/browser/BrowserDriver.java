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

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.htmlcleaner.XPatherException;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Timeouts;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.xml.sax.SAXException;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.database.CookieItem;
import com.jaeksoft.searchlib.crawler.web.spider.HtmlArchiver;
import com.jaeksoft.searchlib.crawler.web.spider.HttpDownloader;
import com.jaeksoft.searchlib.script.commands.Selectors.Selector;
import com.jaeksoft.searchlib.util.IOUtils;

public abstract class BrowserDriver<T extends WebDriver> implements Closeable {

	protected final BrowserDriverEnum type;
	protected T driver = null;

	protected BrowserDriver(BrowserDriverEnum type) {
		this.type = type;
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

	public BrowserDriverEnum getType() {
		return type;
	}

	public Object javascript(String javascript, boolean faultTolerant,
			Object... objects) throws IOException, SearchLibException {
		try {
			if (!(driver instanceof JavascriptExecutor))
				throw new IOException(
						"The Web driver don't support javascript execution");
			JavascriptExecutor js = (JavascriptExecutor) driver;
			return js.executeScript(javascript, objects);
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

	public List<?> getElementByTag(String tag, boolean faultTolerant)
			throws IOException, SearchLibException {
		List<?> result = (List<?>) javascript(
				"return document.getElementsByTagName(arguments[0])",
				faultTolerant, tag);
		return result;
	}

	public String getJavascriptInnerHtml() throws IOException,
			SearchLibException {
		String source = (String) javascript(
				"document.getElementsByTagName('body')[0].innerHTML", false);
		return source;
	}

	private static String XPATH_SCRIPT = null;

	private final synchronized static String getXPath() throws IOException {
		if (XPATH_SCRIPT != null)
			return XPATH_SCRIPT;
		URL url = Resources
				.getResource("/com/jaeksoft/searchlib/crawler/web/browser/get_xpath.js");
		String content = Resources.toString(url, Charsets.UTF_8);
		BufferedReader br = new BufferedReader(new StringReader(content));
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = br.readLine()) != null)
			sb.append(line.trim());
		br.close();
		XPATH_SCRIPT = sb.toString();
		return XPATH_SCRIPT;
	}

	public String getXPath(WebElement webElement, boolean faultTolerant)
			throws IOException, SearchLibException {
		String xPath = (String) javascript(getXPath(), faultTolerant,
				webElement);
		if (xPath == null)
			Logging.warn("XPATH extraction failed on " + webElement);
		return xPath;
	}

	final public BufferedImage getScreenshot() throws IOException {
		if (!(driver instanceof TakesScreenshot))
			throw new IOException(
					"This browser driver does not support screenshot");
		TakesScreenshot takesScreenshot = (TakesScreenshot) driver;
		byte[] data = takesScreenshot.getScreenshotAs(OutputType.BYTES);
		return ImageIO.read(new ByteArrayInputStream(data));
	}

	final public Rectangle getRectangle(WebElement element) {
		if (element == null)
			return null;
		Rectangle box = new Rectangle(element.getLocation().x,
				element.getLocation().y, element.getSize().width,
				element.getSize().height);
		return box;
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

	final public List<WebElement> locateBy(By by) throws SearchLibException {
		return driver.findElements(by);
	}

	final public int locateBy(By by, Collection<WebElement> elements,
			boolean faultTolerant) throws SearchLibException {
		try {
			List<WebElement> list = driver.findElements(by);
			if (list == null)
				return 0;
			elements.addAll(list);
			return list.size();
		} catch (Exception e) {
			if (!faultTolerant)
				throw new SearchLibException("Web element location failed: "
						+ by);
			Logging.warn(e);
			return 0;
		}
	}

	public final List<WebElement> locateBy(WebElement originElement, By by,
			boolean faultTolerant) throws SearchLibException {
		try {
			if (originElement == null)
				return null;
			return originElement.findElements(by);
		} catch (Exception e) {
			if (!faultTolerant)
				throw new SearchLibException("Web element location failed: "
						+ by);
			Logging.warn(e);
			return null;
		}
	}

	final public HtmlArchiver saveArchive(HttpDownloader httpDownloader,
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
			Set<WebElement> disableScriptWebElements = new HashSet<WebElement>();
			Set<String> xPathDisableScriptSet = new HashSet<String>();
			if (selectors != null)
				for (Selector selector : selectors)
					if (selector.disableScript)
						locateBy(selector.getBy(), disableScriptWebElements,
								true);
			for (WebElement webElement : disableScriptWebElements) {
				String xPath = getXPath(webElement, true);
				if (xPath != null)
					xPathDisableScriptSet.add(xPath);
			}
			archiver.archive(this, xPathDisableScriptSet);
			return archiver;
		} finally {
			IOUtils.close(reader);
		}
	}

	final public String getWindow() {
		return driver.getWindowHandle();
	}

	final public void switchToWindow(String window) {
		driver.switchTo().window(window);
	}

	final public void switchToFrame(WebElement frameWebelement) {
		driver.switchTo().frame(frameWebelement);
	}

	final public void switchToMain() {
		driver.switchTo().defaultContent();
	}

	final public void getFrameSource(WebElement frameWebelement,
			File captureDirectory) throws IOException {
		if (!captureDirectory.exists())
			captureDirectory.mkdir();
		File sourceFile = new File(captureDirectory, "source.html");
		switchToFrame(frameWebelement);
		FileUtils.write(sourceFile, getSourceCode());
		switchToMain();
	}

	/**
	 * Click on the given WebElement using Actions
	 * 
	 * @param element
	 * @return
	 */
	public void click(WebElement element) {
		Actions builder = new Actions(driver);
		Action click = builder.moveToElement(element).click(element).build();
		click.perform();
	}

	public void switchToLastWindow() {
		String window = null;
		Iterator<String> iterator = driver.getWindowHandles().iterator();
		while (iterator.hasNext())
			window = iterator.next();
		driver.switchTo().window(window);
	}

	public void openNewWindow() throws IOException, SearchLibException {
		javascript("window.open()", false);
		switchToLastWindow();
	}

	public void closeWindow() {
		driver.close();
	}

	public String getCurrentUrl() {
		return driver.getCurrentUrl();
	}

	public List<CookieItem> getCookies() {
		Set<Cookie> cookies = driver.manage().getCookies();
		if (CollectionUtils.isEmpty(cookies))
			return null;
		List<CookieItem> cookieList = new ArrayList<CookieItem>(cookies.size());
		for (Cookie cookie : cookies) {
			BasicClientCookie basicCookie = new BasicClientCookie(
					cookie.getName(), cookie.getValue());
			basicCookie.setDomain(cookie.getDomain());
			basicCookie.setExpiryDate(cookie.getExpiry());
			basicCookie.setPath(cookie.getPath());
			basicCookie.setSecure(cookie.isSecure());
			cookieList.add(new CookieItem(basicCookie));
		}
		return cookieList;
	}

	public WebElement getParent(String tagName, WebElement element) {
		try {
			WebElement parent = element.findElement(By.xpath(".."));
			if (parent == null)
				return null;
			if (tagName == null)
				return parent;
			if (tagName.equalsIgnoreCase(parent.getTagName()))
				return parent;
			return getParent(tagName, parent);
		} catch (NoSuchElementException e) {
			Logging.warn(e);
			return null;
		}
	}
}
