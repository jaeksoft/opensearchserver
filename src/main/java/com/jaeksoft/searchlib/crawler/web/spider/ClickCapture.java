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

package com.jaeksoft.searchlib.crawler.web.spider;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.imageio.ImageIO;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.browser.BrowserDriver;
import com.jaeksoft.searchlib.script.ScriptCommandContext;
import com.jaeksoft.searchlib.script.commands.Selectors;
import com.jaeksoft.searchlib.util.FilesUtils;
import com.jaeksoft.searchlib.util.ImageUtils;
import com.jaeksoft.searchlib.util.StringUtils;

@JsonInclude(Include.NON_NULL)
public final class ClickCapture implements Comparable<ClickCapture> {

	private final Selectors.Selector selector;
	private final Collection<WebElement> webElements;
	private final Rectangle firstElementBox;

	public String anchorHref = null;
	public String finalUrl = null;
	public String embedSrc = null;
	public String imgSrc = null;
	public String filename = null;
	public String file_md5 = null;
	public String file_phash = null;

	public ClickCapture(BrowserDriver<?> browserDriver,
			Selectors.Selector selector, Collection<WebElement> webElements) {
		this.selector = selector;
		this.webElements = webElements;
		Rectangle box = null;
		if (webElements != null) {
			for (WebElement webElement : webElements) {
				Rectangle r = browserDriver.getRectangle(webElement);
				if (r.width > 0 && r.height > 0) {
					box = r;
					break;
				}
			}
		}
		firstElementBox = box;
	}

	private boolean isEmpty() {
		return anchorHref == null && finalUrl == null && imgSrc == null
				&& embedSrc == null;
	}

	@Override
	public int compareTo(ClickCapture o) {
		int c;
		if ((c = StringUtils.compareNullString(anchorHref, o.anchorHref)) != 0)
			return c;
		if ((c = StringUtils.compareNullString(embedSrc, o.embedSrc)) != 0)
			return c;
		if ((c = StringUtils.compareNullString(imgSrc, o.imgSrc)) != 0)
			return c;
		return 0;
	}

	@JsonIgnore
	@XmlTransient
	private String sql(String sql) {
		sql = StringUtils.replace(
				sql,
				"{custom}",
				selector.custom == null ? StringUtils.EMPTY : StringEscapeUtils
						.escapeEcmaScript(selector.custom));
		sql = StringUtils.replace(
				sql,
				"{anchor_href}",
				anchorHref == null ? StringUtils.EMPTY : StringEscapeUtils
						.escapeEcmaScript(anchorHref));
		sql = StringUtils.replace(
				sql,
				"{final_url}",
				finalUrl == null ? StringUtils.EMPTY : StringEscapeUtils
						.escapeEcmaScript(finalUrl));
		sql = StringUtils.replace(
				sql,
				"{embed_src}",
				embedSrc == null ? StringUtils.EMPTY : StringEscapeUtils
						.escapeEcmaScript(embedSrc));
		sql = StringUtils.replace(
				sql,
				"{img_src}",
				imgSrc == null ? StringUtils.EMPTY : StringEscapeUtils
						.escapeEcmaScript(imgSrc));
		sql = StringUtils.replace(
				sql,
				"{filename}",
				filename == null ? StringUtils.EMPTY : StringEscapeUtils
						.escapeEcmaScript(filename));
		sql = StringUtils.replace(
				sql,
				"{file_md5}",
				file_md5 == null ? StringUtils.EMPTY : StringEscapeUtils
						.escapeEcmaScript(file_md5));
		sql = StringUtils.replace(
				sql,
				"{file_phash}",
				file_phash == null ? StringUtils.EMPTY : StringEscapeUtils
						.escapeEcmaScript(file_phash));
		return sql;
	}

	private static String performClickGetUrl(BrowserDriver<?> browserDriver,
			String url) throws IOException, SearchLibException {
		if (url == null)
			return null;
		try {
			browserDriver.openNewWindow();
			browserDriver.get(url);
			return browserDriver.getCurrentUrl();
		} catch (org.openqa.selenium.TimeoutException e) {
			Logging.warn(e);
			return null;
		}
	}

	private boolean locateAimgClickCapture(WebElement aElement) {
		if (!aElement.isDisplayed())
			return false;
		String ahref = aElement.getAttribute("href");
		List<WebElement> imgElements = aElement.findElements(By
				.cssSelector("img"));
		if (imgElements == null)
			return false;
		for (WebElement imgElement : imgElements) {
			if (!imgElement.isDisplayed())
				continue;
			imgSrc = imgElement.getAttribute("src");
		}
		anchorHref = ahref;
		return true;
	}

	private boolean locateAimgClickCapture(List<WebElement> aElements)
			throws SearchLibException, IOException {
		if (aElements == null)
			return false;
		for (WebElement aElement : aElements)
			if (locateAimgClickCapture(aElement))
				return true;
		return false;
	}

	private boolean locateEmbedClickCapture(List<WebElement> embedElements)
			throws SearchLibException, IOException {
		if (embedElements == null)
			return false;
		for (WebElement embedElement : embedElements) {
			if (!embedElement.isDisplayed())
				continue;
			String flashVars = embedElement.getAttribute("flashvars");
			String[] params = StringUtils.split(flashVars, '&');
			Map<String, String> paramMap = new TreeMap<String, String>();
			if (params != null) {
				for (String param : params) {
					String[] keyValue = StringUtils.split(param, '=');
					if (keyValue != null && keyValue.length == 2)
						paramMap.put(keyValue[0].toLowerCase(),
								URLDecoder.decode(keyValue[1], "UTF-8"));
				}
			}
			embedSrc = embedElement.getAttribute("src");
			if (selector.flashVarsLink != null)
				anchorHref = paramMap.get(selector.flashVarsLink);
			return true;
		}
		return false;
	}

	private boolean locateElement(BrowserDriver<?> browserDriver,
			WebElement webElement) throws SearchLibException, IOException {
		By by = By.cssSelector("a");
		List<WebElement> aElements = webElement == null ? browserDriver
				.locateBy(by) : webElement.findElements(by);
		if (locateAimgClickCapture(aElements))
			return true;
		by = By.cssSelector("embed");
		List<WebElement> embedElements = webElement == null ? browserDriver
				.locateBy(by) : webElement.findElements(by);
		if (locateEmbedClickCapture(embedElements))
			return true;
		by = By.cssSelector("object > object");
		List<WebElement> objectElements = webElement == null ? browserDriver
				.locateBy(by) : webElement.findElements(by);
		if (locateEmbedClickCapture(objectElements))
			return true;
		by = By.tagName("iframe");
		List<WebElement> iFrameElements = webElement == null ? browserDriver
				.locateBy(by) : webElement.findElements(by);
		if (locateIFrame(browserDriver, iFrameElements))
			return true;
		return false;
	}

	private boolean locateIFrame(BrowserDriver<?> browserDriver,
			List<WebElement> iFrameElements) throws SearchLibException,
			IOException {
		try {
			if (CollectionUtils.isEmpty(iFrameElements))
				return false;
			for (WebElement frameWebElement : iFrameElements) {
				if (!frameWebElement.isDisplayed())
					continue;
				browserDriver.switchToFrame(frameWebElement);
				if (locateElement(browserDriver, null))
					return true;
			}
			return false;
		} finally {
			browserDriver.switchToMain();
		}
	}

	private void locate(BrowserDriver<?> browserDriver) {
		try {
			if (CollectionUtils.isEmpty(webElements))
				return;
			for (WebElement webElement : webElements) {
				if ("img".equalsIgnoreCase(webElement.getTagName())) {
					webElement = browserDriver.getParent("a", webElement);
					if (webElement != null)
						if (locateAimgClickCapture(webElement))
							return;
				}
				if (locateElement(browserDriver, webElement))
					return;
			}
		} catch (Exception e) {
			Logging.warn(e);
		}
	}

	/**
	 * Try to locate a/img object/object or object/embed items
	 * 
	 * @param browserDriver
	 * @param clickCaptures
	 */
	public static void locate(BrowserDriver<?> browserDriver,
			Collection<ClickCapture> clickCaptures) {
		for (ClickCapture clickCapture : clickCaptures)
			clickCapture.locate(browserDriver);
	}

	private void click(BrowserDriver<?> browserDriver,
			HtmlArchiver htmlArchiver, BufferedImage screenshot) {
		try {
			filename = null;
			if (isEmpty()) {
				if (CollectionUtils.isEmpty(webElements))
					return;
				if (firstElementBox == null || htmlArchiver == null)
					return;
				if (firstElementBox.width == 0 || firstElementBox.height == 0)
					throw new SearchLibException(
							"Box height or width is null: " + selector);
				BufferedImage image = ImageUtils.getSubImage(screenshot,
						firstElementBox);
				File imageFile = htmlArchiver.getAndRegisterDestFile(null,
						"clickCapture", "png");
				ImageIO.write(image, "png", imageFile);
				filename = imageFile.getName();
				imgSrc = imageFile.getName();
			} else {
				if (htmlArchiver != null) {
					if (imgSrc != null)
						filename = htmlArchiver.getUrlFileName(imgSrc);
					else if (embedSrc != null)
						filename = htmlArchiver.getUrlFileName(embedSrc);
				}
			}
			if (filename != null) {
				File file = htmlArchiver.getLocalFile(filename);
				if (file.exists()) {
					file_md5 = FilesUtils.computeMd5(file);
					if (imgSrc != null)
						file_phash = ImageUtils.computePHash(file);
				}
			}
			finalUrl = performClickGetUrl(browserDriver, anchorHref);
		} catch (Exception e) {
			Logging.warn(e);
		}
	}

	/**
	 * Collect the final URL
	 * 
	 * @param browserDriver
	 * @param results
	 * @param htmlArchiver
	 * @throws SearchLibException
	 * @throws IOException
	 */
	public static void click(BrowserDriver<?> browserDriver,
			Collection<ClickCapture> clickCaptures, HtmlArchiver htmlArchiver,
			BufferedImage screenshot) throws SearchLibException, IOException {
		String window = browserDriver.getWindow();
		try {
			for (ClickCapture clickCapture : clickCaptures)
				clickCapture.click(browserDriver, htmlArchiver, screenshot);
		} finally {
			if (window != null)
				browserDriver.switchToWindow(window);
		}
	}

	/**
	 * 
	 * @param context
	 * @param clickCaptureSql
	 * @param clickCaptures
	 */
	public static void sql(ScriptCommandContext context,
			String clickCaptureSql, Collection<ClickCapture> clickCaptures) {
		for (ClickCapture clickCapture : clickCaptures) {
			String sql = clickCapture.sql(clickCaptureSql);
			try {
				context.executeSqlUpdate(sql);
			} catch (Exception e) {
				Logging.warn(e);
			}
		}
	}
}