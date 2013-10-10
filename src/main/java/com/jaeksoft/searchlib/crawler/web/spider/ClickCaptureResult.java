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

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.browser.BrowserDriver;
import com.jaeksoft.searchlib.script.commands.Selectors;
import com.jaeksoft.searchlib.util.FilesUtils;
import com.jaeksoft.searchlib.util.ImageUtils;
import com.jaeksoft.searchlib.util.StringUtils;

@JsonInclude(Include.NON_NULL)
public final class ClickCaptureResult implements Comparable<ClickCaptureResult> {

	private final Selectors.Selector selector;
	private final String iFrameXPath;

	public String anchorHref = null;
	public String finalUrl = null;
	public String embedSrc = null;
	public String imgSrc = null;
	public String filename = null;
	public String file_md5 = null;
	public String file_phash = null;

	private ClickCaptureResult(Selectors.Selector selector, String iFrameXPath) {
		this.selector = selector;
		this.iFrameXPath = iFrameXPath;
	}

	private boolean isEmpty() {
		return anchorHref == null && finalUrl == null && imgSrc == null
				&& embedSrc == null;
	}

	@Override
	public int compareTo(ClickCaptureResult o) {
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
	public String sql(String sql) {
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

	private static void locateAimgClickCapture(Selectors.Selector selector,
			List<WebElement> aElements, Collection<ClickCaptureResult> results)
			throws SearchLibException, IOException {
		if (aElements == null)
			return;
		for (WebElement aElement : aElements) {
			if (!aElement.isDisplayed())
				continue;
			ClickCaptureResult result = new ClickCaptureResult(selector, null);
			result.anchorHref = aElement.getAttribute("href");
			List<WebElement> imgElements = aElement.findElements(By
					.cssSelector("img"));
			if (imgElements != null) {
				for (WebElement imgElement : imgElements) {
					if (!imgElement.isDisplayed())
						continue;
					result.imgSrc = imgElement.getAttribute("src");
				}
			}
			results.add(result);
		}
	}

	private static void locateEmbedClickCapture(Selectors.Selector selector,
			List<WebElement> embedElements,
			Collection<ClickCaptureResult> clickCaptureResults)
			throws SearchLibException, IOException {
		if (embedElements == null)
			return;
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
			ClickCaptureResult result = new ClickCaptureResult(selector, null);
			result.embedSrc = embedElement.getAttribute("src");
			if (selector.flashVarsLink != null)
				result.anchorHref = paramMap.get(selector.flashVarsLink);
			clickCaptureResults.add(result);
		}

	}

	private static void locateIFrame(BrowserDriver<?> browserDriver,
			Selectors.Selector selector, WebElement parentElement,
			Collection<ClickCaptureResult> results) throws SearchLibException,
			IOException {
		// Load included iframe
		List<WebElement> webElements = parentElement.findElements(By
				.tagName("iframe"));
		if (CollectionUtils.isEmpty(webElements))
			return;
		for (WebElement webElement : webElements) {
			ClickCaptureResult result = new ClickCaptureResult(selector,
					browserDriver.getXPath(webElement, true));
			results.add(result);
		}
	}

	public static void locateClickCaptures(
			BrowserDriver<?> browserDriver,
			HashMap<Selectors.Selector, HashSet<WebElement>> selectorsClickCapture,
			Collection<ClickCaptureResult> results) throws SearchLibException,
			IOException {
		if (MapUtils.isEmpty(selectorsClickCapture))
			return;

		for (Map.Entry<Selectors.Selector, HashSet<WebElement>> entry : selectorsClickCapture
				.entrySet()) {
			HashSet<WebElement> webElements = entry.getValue();
			Selectors.Selector selector = entry.getKey();
			if (CollectionUtils.isEmpty(webElements))
				continue;
			for (WebElement webElement : webElements) {
				List<WebElement> aElements = webElement.findElements(By
						.cssSelector("a"));
				locateAimgClickCapture(selector, aElements, results);
				List<WebElement> embedElements = webElement.findElements(By
						.cssSelector("embed"));
				locateEmbedClickCapture(selector, embedElements, results);
				List<WebElement> objectElements = webElement.findElements(By
						.cssSelector("object > object"));
				locateEmbedClickCapture(selector, objectElements, results);
				locateIFrame(browserDriver, selector, webElement, results);
			}
		}
	}

	public static void locateIFrame(Collection<ClickCaptureResult> results,
			BrowserDriver<?> browserDriver, String iFrameXPath)
			throws SearchLibException, IOException {
		if (iFrameXPath == null)
			return;
		List<ClickCaptureResult> newResults = new ArrayList<ClickCaptureResult>(
				0);
		for (ClickCaptureResult result : results) {
			if (result.iFrameXPath == null)
				continue;
			if (!result.iFrameXPath.equals(iFrameXPath))
				continue;
			List<WebElement> aElements = browserDriver.locateBy(By
					.cssSelector("a"));
			locateAimgClickCapture(result.selector, aElements, newResults);
			List<WebElement> embedElements = browserDriver.locateBy(By
					.cssSelector("embed"));
			locateEmbedClickCapture(result.selector, embedElements, newResults);
			List<WebElement> objectElements = browserDriver.locateBy(By
					.cssSelector("object > object"));
			locateEmbedClickCapture(result.selector, objectElements, results);
		}
		results.addAll(newResults);
	}

	public static void clickClickCapture(BrowserDriver<?> browserDriver,
			Collection<ClickCaptureResult> results, HtmlArchiver htmlArchiver)
			throws SearchLibException, IOException {

		String window = browserDriver.getWindow();
		Set<ClickCaptureResult> resultSet = new TreeSet<ClickCaptureResult>();

		// Collect the final URL
		for (ClickCaptureResult result : results) {
			if (result.isEmpty() || resultSet.contains(result))
				continue;
			result.filename = htmlArchiver == null || result.imgSrc == null ? null
					: htmlArchiver.getUrlFileName(result.imgSrc);
			if (result.filename != null) {
				File file = htmlArchiver.getLocalFile(result.filename);
				if (file.exists()) {
					result.file_md5 = FilesUtils.computeMd5(file);
					result.file_phash = ImageUtils.computePHash(file);
				}
			}
			result.finalUrl = performClickGetUrl(browserDriver,
					result.anchorHref);
			resultSet.add(result);
		}
		results.clear();
		results.addAll(resultSet);
		browserDriver.switchToWindow(window);
	}
}