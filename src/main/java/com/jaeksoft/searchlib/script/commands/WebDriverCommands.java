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
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.htmlcleaner.XPatherException;
import org.openqa.selenium.WebElement;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.browser.BrowserDriver;
import com.jaeksoft.searchlib.crawler.web.browser.BrowserDriverEnum;
import com.jaeksoft.searchlib.crawler.web.spider.ClickCapture;
import com.jaeksoft.searchlib.crawler.web.spider.HtmlArchiver;
import com.jaeksoft.searchlib.crawler.web.spider.HttpDownloader;
import com.jaeksoft.searchlib.script.CommandAbstract;
import com.jaeksoft.searchlib.script.CommandEnum;
import com.jaeksoft.searchlib.script.ScriptCommandContext;
import com.jaeksoft.searchlib.script.ScriptException;
import com.jaeksoft.searchlib.util.IOUtils;
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
			BrowserDriver<?> driver = context.getBrowserDriver();
			if (driver != null && driver.getType() == browserDriverEnum)
				return;
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
			if (StringUtils.isEmpty(url))
				throwError("No URL given");
			url = context.replaceVariables(url);
			browserDriver.get(url);
		}

	}

	public static class Download extends CommandAbstract {

		public Download() {
			super(CommandEnum.WEBDRIVER_DOWNLOAD);
		}

		@Override
		public void run(ScriptCommandContext context, String id,
				String... parameters) throws ScriptException {
			checkParameters(1, parameters);
			String filePath = context.replaceVariables(getParameterString(0));
			if (StringUtils.isEmpty(filePath))
				throwError("No PATH given");
			BrowserDriver<?> browserDriver = context.getBrowserDriver();
			if (browserDriver == null)
				throwError("No browser open");
			context.download(browserDriver.getCurrentUrl(), new File(filePath));
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
				throwError("The destination " + dest + " is not a directory");
			destFile.mkdirs();
			if (!destFile.exists())
				throwError("Unable to create the directory " + destFile);
			return destFile;
		}

		protected BrowserDriver<?> checkBrowserDriver(
				ScriptCommandContext context) throws ScriptException {
			BrowserDriver<?> browserDriver = context.getBrowserDriver();
			if (browserDriver == null)
				throwError("No browser open");
			return browserDriver;
		}

		public final static Pattern PARAM_CLICK_CAPTURE_SQL = Pattern.compile(
				"click_capture_sql\\(\\[([^\\]]*)\\]\\)",
				Pattern.CASE_INSENSITIVE);

		@Override
		public void run(ScriptCommandContext context, String id,
				String... parameters) throws ScriptException {
			HttpDownloader httpDownloader = null;
			checkParameters(1, parameters);
			String clickCaptureSql = findPatternFunction(1,
					PARAM_CLICK_CAPTURE_SQL);
			BrowserDriver<?> browserDriver = checkBrowserDriver(context);
			File destFile = checkDestFile();
			try {

				Collection<Selectors.Selector> selectors = context
						.getSelectors();
				Collection<ClickCapture> clickCaptures = null;
				BufferedImage screenshot = null;
				if (selectors != null) {
					clickCaptures = new ArrayList<ClickCapture>(0);
					for (Selectors.Selector selector : selectors) {
						List<WebElement> elements = new ArrayList<WebElement>(0);
						browserDriver
								.locateBy(selector.getBy(), elements, true);
						if (selector.clickCapture)
							clickCaptures.add(new ClickCapture(browserDriver,
									selector, elements));
					}
					if (!CollectionUtils.isEmpty(clickCaptures))
						screenshot = browserDriver.getScreenshot();
				}
				httpDownloader = context.getConfig().getWebCrawlMaster()
						.getNewHttpDownloader(true, null);

				HtmlArchiver htmlArchiver = browserDriver.saveArchive(
						httpDownloader, destFile, context.getSelectors());

				if (!CollectionUtils.isEmpty(clickCaptures)) {
					ClickCapture.locate(browserDriver, clickCaptures);
					ClickCapture.click(browserDriver, clickCaptures,
							htmlArchiver, screenshot);
					JsonUtils.jsonToFile(clickCaptures, new File(destFile,
							"clickCapture.json"));
					ClickCapture.sql(context, clickCaptureSql, clickCaptures);
				}

			} catch (IOException e) {
				throw new ScriptException(e);
			} catch (IllegalStateException e) {
				throw new ScriptException(e);
			} catch (SearchLibException e) {
				throw new ScriptException(e);
			} catch (ClassCastException e) {
				throw new ScriptException(e);
			} catch (URISyntaxException e) {
				throw new ScriptException(e);
			} catch (SAXException e) {
				throw new ScriptException(e);
			} catch (ParserConfigurationException e) {
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

		private final static String NO_HIGHLIGHT_PARAM = "NO_HIGHLIGHT";

		@Override
		public void run(ScriptCommandContext context, String id,
				String... parameters) throws ScriptException {
			checkParameters(1, parameters);
			BrowserDriver<?> browserDriver = checkBrowserDriver(context);
			File destFile = checkDestFile();
			FileWriter writer = null;
			Integer count = getParameterInt(1);
			boolean bNoHighLight = findParameter(2, NO_HIGHLIGHT_PARAM);
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
				StringBuilder sbHtml = new StringBuilder(html);
				Collection<Selectors.Selector> selectors = context
						.getSelectors();
				if (selectors != null && !bNoHighLight) {
					HashSet<WebElement> elementSet = new HashSet<WebElement>();
					for (Selectors.Selector selector : selectors)
						if (selector.screenshotHighlight)
							browserDriver.locateBy(selector.getBy(),
									elementSet, true);
					List<Rectangle> boxes = new ArrayList<Rectangle>(
							elementSet.size());
					int i = 1;
					for (WebElement element : elementSet) {
						Rectangle box = browserDriver.getRectangle(element);
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
				IOUtils.close(writer);
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
			String js = getParameterString(0);
			js = context.replaceVariables(js);
			try {
				browserDriver.javascript(js, faultTolerant);
			} catch (IOException e) {
				throw new ScriptException(e);
			} catch (SearchLibException e) {
				throw new ScriptException(e);
			}
		}
	}

	public static class NewWindow extends CommandAbstract {

		public NewWindow() {
			super(CommandEnum.WEBDRIVER_NEW_WINDOW);
		}

		@Override
		public void run(ScriptCommandContext context, String id,
				String... parameters) throws ScriptException {
			BrowserDriver<?> driver = context.getBrowserDriver();
			if (driver == null)
				throwError("No WebDriver open");
			try {
				driver.openNewWindow();
			} catch (IOException e) {
				throw new ScriptException(e);
			} catch (SearchLibException e) {
				throw new ScriptException(e);
			}
		}
	}

	public static class CloseWindow extends CommandAbstract {

		public CloseWindow() {
			super(CommandEnum.WEBDRIVER_CLOSE_WINDOW);
		}

		@Override
		public void run(ScriptCommandContext context, String id,
				String... parameters) throws ScriptException {
			BrowserDriver<?> driver = context.getBrowserDriver();
			if (driver == null)
				throwError("Nothing to close. No WebDriver open");
			driver.closeWindow();
			driver.switchToLastWindow();
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
