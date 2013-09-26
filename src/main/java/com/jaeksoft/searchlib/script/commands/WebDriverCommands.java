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

package com.jaeksoft.searchlib.script.commands;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.IOUtils;
import org.htmlcleaner.XPatherException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.browser.BrowserDriver;
import com.jaeksoft.searchlib.crawler.web.browser.BrowserDriverEnum;
import com.jaeksoft.searchlib.crawler.web.spider.HttpDownloader;
import com.jaeksoft.searchlib.script.CommandAbstract;
import com.jaeksoft.searchlib.script.CommandEnum;
import com.jaeksoft.searchlib.script.ScriptCommandContext;
import com.jaeksoft.searchlib.script.ScriptException;
import com.jaeksoft.searchlib.util.ImageUtils;
import com.jaeksoft.searchlib.util.JsonUtils;

public class WebDriverCommands {

	public static class Open extends CommandAbstract {

		public Open() {
			super(CommandEnum.WEBDRIVER_OPEN);
		}

		@Override
		public void run(ScriptCommandContext context, String id,
				String... parameters) throws ScriptException {
			checkParameters(1, parameters);
			BrowserDriverEnum browserDriverEnum = BrowserDriverEnum.find(
					getParameterString(0), null);
			if (browserDriverEnum == null)
				throw new ScriptException("Web driver not found: "
						+ parameters[0]);
			context.setBrowserDriver(browserDriverEnum);
		}
	}

	public static class Close extends CommandAbstract {

		public Close() {
			super(CommandEnum.WEBDRIVER_CLOSE);
		}

		@Override
		public void run(ScriptCommandContext context, String id,
				String... parameters) throws ScriptException {
			context.setBrowserDriver(null);
		}
	}

	public static class Resize extends CommandAbstract {

		public Resize() {
			super(CommandEnum.WEBDRIVER_RESIZE);
		}

		@Override
		public void run(ScriptCommandContext context, String id,
				String... parameters) throws ScriptException {
			checkParameters(2, parameters);
			try {
				Integer width = getParameterInt(0);
				if (width == null)
					throwError("No width given");
				Integer height = getParameterInt(1);
				if (height == null)
					throwError("No height given");
				BrowserDriver<?> browserDriver = context.getBrowserDriver();
				if (browserDriver == null)
					throwError("No browser open");
				browserDriver.setSize(width, height);
			} catch (NumberFormatException e) {
				throw new ScriptException(e);
			} catch (SearchLibException e) {
				throw new ScriptException(e);
			}
		}
	}

	public static class Get extends CommandAbstract {

		public Get() {
			super(CommandEnum.WEBDRIVER_GET);
		}

		@Override
		public void run(ScriptCommandContext context, String id,
				String... parameters) throws ScriptException {
			checkParameters(1, parameters);
			BrowserDriver<?> browserDriver = context.getBrowserDriver();
			if (browserDriver == null)
				throwError("No browser open");
			String url = getParameterString(0);
			if (url == null)
				throwError("No URL given");
			browserDriver.get(url);
		}

	}

	public static class Capture extends CommandAbstract {

		public Capture() {
			super(CommandEnum.WEBDRIVER_CAPTURE);
		}

		protected Capture(CommandEnum command) {
			super(command);
		}

		protected File checkDestFile() throws ScriptException {
			String dest = getParameterString(0);
			if (dest == null)
				throwError("No destination path given");
			File destFile = new File(dest);
			if (destFile.exists() && !destFile.isDirectory())
				throw new ScriptException("The destination " + dest
						+ " is not a directory");
			destFile.mkdirs();
			return destFile;
		}

		protected BrowserDriver<?> checkBrowserDriver(
				ScriptCommandContext context) throws ScriptException {
			BrowserDriver<?> browserDriver = context.getBrowserDriver();
			if (browserDriver == null)
				throwError("No browser open");
			return browserDriver;
		}

		@JsonInclude(Include.NON_NULL)
		public final static class ClickCaptureResult {

			public String anchorHref = null;
			public String finalUrl = null;
			public String objectData = null;
			public Set<String> imageSources = null;

			private void addImageSource(String src) {
				if (imageSources == null)
					imageSources = new TreeSet<String>();
				imageSources.add(src);
			}
		}

		private void doClickCapture(BrowserDriver<?> browserDriver,
				WebElement webElement, Collection<ClickCaptureResult> results)
				throws SearchLibException {
			List<WebElement> aElements = browserDriver.locateBy(webElement,
					By.cssSelector("a"), true);
			if (aElements != null) {
				for (WebElement aElement : aElements) {
					if (!aElement.isDisplayed())
						continue;
					ClickCaptureResult result = new ClickCaptureResult();
					result.anchorHref = aElement.getAttribute("href");
					results.add(result);
					List<WebElement> imgElements = browserDriver.locateBy(
							aElement, By.cssSelector("img"), true);
					if (imgElements != null) {
						for (WebElement imgElement : imgElements) {
							if (!imgElement.isDisplayed())
								continue;
							result.addImageSource(imgElement
									.getAttribute("src"));
						}
					}
					result.finalUrl = browserDriver.click(aElement);
				}
			}
			List<WebElement> objectElements = browserDriver.locateBy(
					webElement, By.cssSelector("object"), true);
			if (objectElements != null) {
				for (WebElement objectElement : objectElements) {
					if (!objectElement.isDisplayed())
						continue;
					ClickCaptureResult result = new ClickCaptureResult();
					result.objectData = objectElement.getAttribute("data");
					result.finalUrl = browserDriver.click(objectElement);
					results.add(result);
				}
			}
		}

		private void doClickCaptures(
				BrowserDriver<?> browserDriver,
				HashMap<Selectors.Selector, HashSet<WebElement>> selectorsClickCapture,
				Collection<ClickCaptureResult> results)
				throws SearchLibException {
			if (MapUtils.isEmpty(selectorsClickCapture))
				return;
			for (Map.Entry<Selectors.Selector, HashSet<WebElement>> entry : selectorsClickCapture
					.entrySet()) {
				if (CollectionUtils.isEmpty(entry.getValue()))
					continue;
				for (WebElement webElement : entry.getValue())
					doClickCapture(browserDriver, webElement, results);
			}
		}

		@Override
		public void run(ScriptCommandContext context, String id,
				String... parameters) throws ScriptException {
			HttpDownloader httpDownloader = null;
			checkParameters(1, parameters);
			BrowserDriver<?> browserDriver = checkBrowserDriver(context);
			File destFile = checkDestFile();
			try {

				Collection<Selectors.Selector> selectors = context
						.getSelectors();
				HashMap<Selectors.Selector, HashSet<WebElement>> selectorsClickCapture = null;
				if (selectors != null) {
					selectorsClickCapture = new HashMap<Selectors.Selector, HashSet<WebElement>>();
					HashSet<WebElement> elementSet = new HashSet<WebElement>();
					for (Selectors.Selector selector : selectors) {
						HashSet<WebElement> elements = new HashSet<WebElement>();
						browserDriver.locateBy(selector, elements, true);
						elementSet.addAll(elements);
						if (selector.clickCapture)
							selectorsClickCapture.put(selector, elements);
					}
					int i = 1;
					File captureFile = new File(destFile, "capture");
					for (WebElement element : elementSet) {
						if ("iframe".equals(element.getTagName())) {
							if (!captureFile.exists())
								captureFile.mkdir();
							browserDriver.getFrameSource(element, new File(
									captureFile, Integer.toString(i)));
						}

					}
				}
				httpDownloader = context.getConfig().getWebCrawlMaster()
						.getNewHttpDownloader(true, null);
				browserDriver.saveArchive(httpDownloader, destFile,
						context.getSelectors());

				List<ClickCaptureResult> clickCaptures = new ArrayList<WebDriverCommands.Capture.ClickCaptureResult>(
						0);
				doClickCaptures(browserDriver, selectorsClickCapture,
						clickCaptures);
				if (clickCaptures.size() > 0)
					JsonUtils.jsonToFile(clickCaptures, new File(destFile,
							"clickCapture.json"));

			} catch (IOException e) {
				throw new ScriptException(e);
			} catch (IllegalStateException e) {
				throw new ScriptException(e);
			} catch (SearchLibException e) {
				throw new ScriptException(e);
			} catch (URISyntaxException e) {
				throw new ScriptException(e);
			} catch (SAXException e) {
				throw new ScriptException(e);
			} catch (ParserConfigurationException e) {
				throw new ScriptException(e);
			} catch (ClassCastException e) {
				throw new ScriptException(e);
			} catch (ClassNotFoundException e) {
				throw new ScriptException(e);
			} catch (InstantiationException e) {
				throw new ScriptException(e);
			} catch (IllegalAccessException e) {
				throw new ScriptException(e);
			} catch (XPatherException e) {
				throw new ScriptException(e);
			} finally {
				if (httpDownloader != null)
					httpDownloader.release();
			}
		}
	}

	public static class Screenshot extends Capture {

		private final static String SUBST_FILE = "{screenshot}";
		private final static String SUBST_WIDTH = "{width}";
		private final static String SUBST_HEIGHT = "{height}";
		private final static String SUBST_COORD = "{coord}";
		private final static String SUBST_ALT = "{alt}";

		private final static String HTML_START = "<html><body>" + "<img src=\""
				+ SUBST_FILE + "\" width=\"" + SUBST_WIDTH + "\" height=\""
				+ SUBST_HEIGHT
				+ "\" usemap=\"#capturemap\"/><map name=\"capturemap\"/>";

		private final static String HTML_AREA = "<area shape=\"rect\" coords=\""
				+ SUBST_COORD + "\" href=\"#\" alt=\"" + SUBST_ALT + "\"/>";

		private final static String HTML_END = "</map></body></html>";

		public Screenshot() {
			super(CommandEnum.WEBDRIVER_SCREENSHOT);
		}

		@Override
		public void run(ScriptCommandContext context, String id,
				String... parameters) throws ScriptException {
			checkParameters(1, parameters);
			BrowserDriver<?> browserDriver = checkBrowserDriver(context);
			File destFile = checkDestFile();
			FileWriter writer = null;
			Integer count = getParameterInt(1);
			String destname = count == null ? "screenshot" : "screenshot"
					+ count;
			try {
				BufferedImage image = browserDriver.getScreenshot();
				File pngFile = new File(destFile, destname + ".png");
				File htmlFile = new File(destFile, destname + ".html");
				String html = HTML_START;
				html = html.replace(SUBST_FILE, pngFile.getName());
				html = html.replace(SUBST_WIDTH,
						Integer.toString(image.getWidth()));
				html = html.replace(SUBST_HEIGHT,
						Integer.toString(image.getHeight()));
				StringBuffer sbHtml = new StringBuffer(html);
				Collection<Selectors.Selector> selectors = context
						.getSelectors();
				if (selectors != null) {
					HashSet<WebElement> elementSet = new HashSet<WebElement>();
					for (Selectors.Selector selector : selectors)
						if (selector.screenshotHighlight)
							browserDriver.locateBy(selector, elementSet, true);
					List<Rectangle> boxes = new ArrayList<Rectangle>(
							elementSet.size());
					int i = 1;
					for (WebElement element : elementSet) {
						Rectangle box = new Rectangle(element.getLocation().x,
								element.getLocation().y,
								element.getSize().width,
								element.getSize().height);
						boxes.add(box);
						ImageUtils.yellowHighlight(image, boxes);
						String area = HTML_AREA.replace(SUBST_COORD,
								ImageUtils.rectToCoordString(box, ','))
								.replace(SUBST_ALT, "#" + (i++));
						sbHtml.append(area);
					}
				}
				sbHtml.append(HTML_END);
				if (!pngFile.getParentFile().exists())
					pngFile.getParentFile().mkdirs();
				ImageIO.write(image, "png", pngFile);
				writer = new FileWriter(htmlFile);
				IOUtils.write(sbHtml.toString(), writer);
				writer.close();
				writer = null;
			} catch (IOException e) {
				throw new ScriptException(e);
			} catch (SearchLibException e) {
				throw new ScriptException(e);
			} finally {
				if (writer != null)
					IOUtils.closeQuietly(writer);
			}
		}
	}

	public static class SetTimeOuts extends CommandAbstract {

		public SetTimeOuts() {
			super(CommandEnum.WEBDRIVER_SET_TIMEOUTS);
		}

		@Override
		public void run(ScriptCommandContext context, String id,
				String... parameters) throws ScriptException {
			checkParameters(2, parameters);
			try {
				Integer pageLoad = getParameterInt(0);
				if (pageLoad == null)
					throwError("No time-out for page load (seconds) given");
				Integer script = getParameterInt(1);
				if (script == null)
					throwError("No time-out for script (seconds) given");
				BrowserDriver<?> browserDriver = context.getBrowserDriver();
				if (browserDriver == null)
					throwError("No browser open");
				browserDriver.setTimeouts(pageLoad, script);
			} catch (NumberFormatException e) {
				throw new ScriptException(e);
			}
		}
	}

	public static class Javascript extends CommandAbstract {

		public Javascript() {
			super(CommandEnum.WEBDRIVER_JAVASCRIPT);
		}

		private boolean isStrict() throws ScriptException {
			boolean isStrict = false;
			for (int i = 1; i < getParameterCount(); i++) {
				String param = getParameterString(i);
				if (param == null)
					continue;
				if (param.length() == 0)
					continue;
				if ("strict".equalsIgnoreCase(param)) {
					isStrict = true;
					continue;
				}
				throw new ScriptException("Unknown parameter: " + param);
			}
			return isStrict;
		}

		@Override
		public void run(ScriptCommandContext context, String id,
				String... parameters) throws ScriptException {
			checkParameters(1, parameters);
			boolean faultTolerant = !isStrict();
			BrowserDriver<?> browserDriver = context.getBrowserDriver();
			if (browserDriver == null)
				throwError("No browser open");
			try {
				browserDriver.javascript(getParameterString(0), faultTolerant);
			} catch (IOException e) {
				throw new ScriptException(e);
			} catch (SearchLibException e) {
				throw new ScriptException(e);
			}
		}
	}

	// <link rel="stylesheet" id="responsive-style-css"
	// href="./OpenSearchServer   Search Engine API_files/style.css"
	// type="text/css" media="all">
	// <link rel="canonical"
	// href="./OpenSearchServer   Search Engine API_files/OpenSearchServer   Search Engine API.html">
	// <script id="LR1" type="text/javascript" async=""
	// src="./OpenSearchServer   Search Engine API_files/client.js"></script>
	// <iframe id="twttrHubFrameSecure" allowtransparency="true" frameborder="0"
	// scrolling="no" tabindex="0" name="twttrHubFrameSecure"
	// style="position: absolute; top: -9999em; width: 10px; height: 10px;"
	// src="./OpenSearchServer   Search Engine API_files/hub(1).html"></iframe>
	// <img
	// src="./OpenSearchServer   Search Engine API_files/copy-new-oss-header.png"
	// width="343" height="95" alt="OpenSearchServer">
}
