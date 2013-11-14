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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.pagefactory.ByAll;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.browser.BrowserDriver;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.script.CommandAbstract;
import com.jaeksoft.searchlib.script.CommandEnum;
import com.jaeksoft.searchlib.script.ScriptCommandContext;
import com.jaeksoft.searchlib.script.ScriptException;
import com.jaeksoft.searchlib.util.LinkUtils;
import com.jaeksoft.searchlib.util.RegExpUtils;
import com.jaeksoft.searchlib.util.ThreadUtils;
import com.jaeksoft.searchlib.utils.Variables;

public class Selectors {

	public enum Type {
		CSS_SELECTOR, XPATH_SELECTOR, ID_SELECTOR;
	}

	public static class Selector implements Comparable<Selector> {
		public final Type type;
		public final String query;
		public final boolean disableScript;
		public final boolean screenshotHighlight;
		public final boolean clickCapture;
		public final String custom;
		public final String flashVarsLink;

		private Selector(Type type, String query, boolean disableScript,
				boolean screenshotHighlight, boolean clickCapture,
				String custom, String flashVarsLink, String indexField) {
			this.type = type;
			// Avoid null value in query part (for comparison)
			this.query = query == null ? "" : query;
			this.disableScript = disableScript;
			this.screenshotHighlight = screenshotHighlight;
			this.clickCapture = clickCapture;
			this.custom = custom;
			this.flashVarsLink = flashVarsLink;
		}

		public Selector(Type type, String query) {
			this(type, query, false, false, false, null, null, null);
		}

		public final By getBy() {
			switch (type) {
			case CSS_SELECTOR:
				return new By.ByCssSelector(query);
			case XPATH_SELECTOR:
				return new By.ByXPath(query);
			case ID_SELECTOR:
				String[] ids = StringUtils.split(query);
				if (ids.length == 1)
					return new By.ById(query);
				By.ById[] byIds = new By.ById[ids.length];
				int i = 0;
				for (String id : ids)
					byIds[i++] = new By.ById(id);
				return new ByAll(byIds);
			}
			return null;
		}

		@Override
		public int compareTo(Selector o) {
			int c = type.compareTo(o.type);
			if (c != 0)
				return c;
			return query.compareTo(o.query);
		}

		public boolean isDisableScript() {
			return disableScript;
		}

	}

	public static abstract class SelectorCommandAbstract extends
			CommandAbstract {

		protected final Type selectorType;

		protected SelectorCommandAbstract(CommandEnum commandEnum,
				Type selectorType) {
			super(commandEnum);
			this.selectorType = selectorType;
		}

		public final static Pattern PARAM_DISABLE_SCRIPT = Pattern.compile(
				"disable_script", Pattern.CASE_INSENSITIVE);
		public final static Pattern PARAM_SCREENSHOT_HIGHLIGHT = Pattern
				.compile("screenshot_highlight", Pattern.CASE_INSENSITIVE);
		public final static Pattern PARAM_CLICK_CAPTURE = Pattern.compile(
				"click_capture\\(([^)]*)\\)", Pattern.CASE_INSENSITIVE);
		public final static Pattern PARAM_FLASHVARS_LINK = Pattern.compile(
				"flashvars_link\\(([^)]*)\\)", Pattern.CASE_INSENSITIVE);

		protected List<WebElement> runSelector(ScriptCommandContext context,
				int paramPosition) throws ScriptException {
			Selector selector = new Selector(selectorType,
					getParameterString(paramPosition));
			BrowserDriver<?> driver = context.getBrowserDriver();
			if (driver == null)
				throwError("No browser driver is available");
			try {
				return driver.locateBy(selector.getBy());
			} catch (SearchLibException e) {
				throw new ScriptException(e);
			}
		}

		protected Selector newSelector() throws ScriptException {
			boolean disableScript = false;
			boolean screenshotHighlight = false;
			boolean clickCapture = false;
			String custom = null;
			String flashVarsLink = null;
			String indexField = null;
			for (int i = 1; i < getParameterCount(); i++) {
				String param = getParameterString(i);
				if (param == null)
					continue;
				if (param.length() == 0)
					continue;
				if (RegExpUtils.find(PARAM_DISABLE_SCRIPT, param)) {
					disableScript = true;
					continue;
				}
				if (RegExpUtils.find(PARAM_SCREENSHOT_HIGHLIGHT, param)) {
					screenshotHighlight = true;
					continue;
				}
				if (RegExpUtils.find(PARAM_CLICK_CAPTURE, param)) {
					Matcher matcher = RegExpUtils.matcher(PARAM_CLICK_CAPTURE,
							param);
					if (matcher.matches())
						if (matcher.groupCount() > 0)
							custom = matcher.group(1);
					clickCapture = true;
					continue;
				}
				if (RegExpUtils.find(PARAM_FLASHVARS_LINK, param)) {
					Matcher matcher = RegExpUtils.matcher(PARAM_FLASHVARS_LINK,
							param);
					if (matcher.matches())
						if (matcher.groupCount() > 0)
							flashVarsLink = matcher.group(1);
					continue;
				}
				throw new ScriptException("Unknown parameter: " + param);
			}
			return new Selector(selectorType, getParameterString(0),
					disableScript, screenshotHighlight, clickCapture, custom,
					flashVarsLink, indexField);
		}

		protected List<URL> getUrlList(ScriptCommandContext context)
				throws ScriptException {
			List<WebElement> elements = runSelector(context, 1);
			if (CollectionUtils.isEmpty(elements))
				return null;
			URL currentURL = null;
			BrowserDriver<?> driver = context.getBrowserDriver();
			if (driver != null) {
				String u = driver.getCurrentUrl();
				if (u != null)
					try {
						currentURL = new URL(u);
					} catch (MalformedURLException e) {
						Logging.warn(e);

					}
			}
			List<URL> urls = new ArrayList<URL>(elements.size());
			for (WebElement element : elements) {
				if (currentURL != null) {
					String href = element.getAttribute("href");
					if (href == null)
						href = element.getAttribute("src");
					if (href != null)
						urls.add(LinkUtils.getLink(currentURL, href, null,
								false));
				}
			}
			return urls;
		}

	}

	public static class CSS_Add extends SelectorCommandAbstract {

		public CSS_Add() {
			super(CommandEnum.CSS_SELECTOR_ADD, Type.CSS_SELECTOR);
		}

		@Override
		public void run(ScriptCommandContext context, String id,
				String... parameters) throws ScriptException {
			checkParameters(1, parameters);
			context.addSelector(newSelector());
		}
	}

	public static class CSS_Reset extends CommandAbstract {

		public CSS_Reset() {
			super(CommandEnum.CSS_SELECTOR_RESET);
		}

		@Override
		public void run(ScriptCommandContext context, String id,
				String... parameters) throws ScriptException {
			context.resetSelector(Type.CSS_SELECTOR);
		}
	}

	public abstract static class IndexFieldCommandAbstract extends
			SelectorCommandAbstract {

		protected IndexFieldCommandAbstract(CommandEnum commandEnum,
				Type selectorType) {
			super(commandEnum, selectorType);
		}

		@Override
		public void run(ScriptCommandContext context, String id,
				String... parameters) throws ScriptException {
			checkParameters(2, parameters);
			IndexDocument indexDocument = context.getIndexDocument();
			if (indexDocument == null)
				throwError("No index document available. Call INDEX_DOCUMENT_NEW before");
			String field = getParameterString(0);
			List<WebElement> elements = runSelector(context, 1);
			if (CollectionUtils.isEmpty(elements))
				return;
			for (WebElement element : elements)
				indexDocument.add(field, element.getText(), null);
		}
	}

	public static class CSS_IndexField extends IndexFieldCommandAbstract {

		public CSS_IndexField() {
			super(CommandEnum.CSS_SELECTOR_INDEX_FIELD, Type.CSS_SELECTOR);
		}

	}

	public static class XPATH_Add extends SelectorCommandAbstract {

		public XPATH_Add() {
			super(CommandEnum.XPATH_SELECTOR_ADD, Type.XPATH_SELECTOR);
		}

		@Override
		public void run(ScriptCommandContext context, String id,
				String... parameters) throws ScriptException {
			checkParameters(1, parameters);
			context.addSelector(newSelector());
		}
	}

	public static class XPATH_Reset extends CommandAbstract {

		public XPATH_Reset() {
			super(CommandEnum.XPATH_SELECTOR_RESET);
		}

		@Override
		public void run(ScriptCommandContext context, String id,
				String... parameters) throws ScriptException {
			context.resetSelector(Type.XPATH_SELECTOR);
		}
	}

	public static class XPATH_IndexField extends IndexFieldCommandAbstract {

		public XPATH_IndexField() {
			super(CommandEnum.XPATH_SELECTOR_INDEX_FIELD, Type.XPATH_SELECTOR);
		}

	}

	public static class ID_Add extends SelectorCommandAbstract {

		public ID_Add() {
			super(CommandEnum.ID_SELECTOR_ADD, Type.ID_SELECTOR);
		}

		@Override
		public void run(ScriptCommandContext context, String id,
				String... parameters) throws ScriptException {
			checkParameters(1, parameters);
			context.addSelector(newSelector());
		}
	}

	public static class ID_Reset extends CommandAbstract {

		public ID_Reset() {
			super(CommandEnum.ID_SELECTOR_RESET);
		}

		@Override
		public void run(ScriptCommandContext context, String id,
				String... parameters) throws ScriptException {
			context.resetSelector(Type.ID_SELECTOR);
		}
	}

	public static class ID_IndexField extends IndexFieldCommandAbstract {

		public ID_IndexField() {
			super(CommandEnum.ID_SELECTOR_INDEX_FIELD, Type.ID_SELECTOR);
		}
	}

	public static class ALL_Reset extends CommandAbstract {

		public ALL_Reset() {
			super(CommandEnum.ALL_SELECTOR_RESET);
		}

		@Override
		public void run(ScriptCommandContext context, String id,
				String... parameters) throws ScriptException {
			context.resetSelector(null);
		}

	}

	public abstract static class SubScriptCommandAbstract extends
			SelectorCommandAbstract {

		protected SubScriptCommandAbstract(CommandEnum commandEnum,
				Type selectorType) {
			super(commandEnum, selectorType);
		}

		@Override
		public void run(ScriptCommandContext context, String id,
				String... parameters) throws ScriptException {
			checkParameters(2, parameters);
			String scriptName = getParameterString(0);
			List<URL> urls = getUrlList(context);
			if (CollectionUtils.isEmpty(urls))
				context.subscript(scriptName, null);
			else
				for (URL url : urls) {
					Variables variables = new Variables();
					variables.put("url", url.toExternalForm());
					context.subscript(scriptName, variables);
				}
		}
	}

	public static class CSS_SubScript extends SubScriptCommandAbstract {

		public CSS_SubScript() {
			super(CommandEnum.CSS_SELECTOR_SUBSCRIPT, Type.CSS_SELECTOR);
		}
	}

	public static class XPATH_SubScript extends SubScriptCommandAbstract {

		public XPATH_SubScript() {
			super(CommandEnum.XPATH_SELECTOR_SUBSCRIPT, Type.XPATH_SELECTOR);
		}
	}

	public static class ID_SubScript extends SubScriptCommandAbstract {

		public ID_SubScript() {
			super(CommandEnum.ID_SELECTOR_SUBSCRIPT, Type.ID_SELECTOR);
		}
	}

	public abstract static class DownloadCommandAbstract extends
			SelectorCommandAbstract {

		public DownloadCommandAbstract(CommandEnum commandEnum,
				Type selectorType) {
			super(commandEnum, selectorType);
		}

		@Override
		public void run(ScriptCommandContext context, String id,
				String... parameters) throws ScriptException {
			checkParameters(2, parameters);
			String filePath = getParameterString(0);
			List<URL> urls = getUrlList(context);
			if (CollectionUtils.isEmpty(urls))
				return;
			int i = 0;
			for (URL url : urls) {
				File file = new File(filePath, Integer.toString(i));
				context.download(url, file);
			}

		}
	}

	public static class CSS_Download extends DownloadCommandAbstract {

		public CSS_Download() {
			super(CommandEnum.CSS_SELECTOR_DOWNLOAD, Type.CSS_SELECTOR);
		}
	}

	public static class XPATH_Download extends DownloadCommandAbstract {

		public XPATH_Download() {
			super(CommandEnum.XPATH_SELECTOR_DOWNLOAD, Type.XPATH_SELECTOR);
		}
	}

	public static class ID_Download extends DownloadCommandAbstract {

		public ID_Download() {
			super(CommandEnum.ID_SELECTOR_DOWNLOAD, Type.ID_SELECTOR);
		}
	}

	public abstract static class ClickAndScriptCommandAbstract extends
			SelectorCommandAbstract {

		protected ClickAndScriptCommandAbstract(CommandEnum commandEnum,
				Type selectorType) {
			super(commandEnum, selectorType);
		}

		@Override
		public void run(ScriptCommandContext context, String id,
				String... parameters) throws ScriptException {
			checkParameters(2, parameters);
			String scriptName = getParameterString(1);
			Integer waitSec = getParameterInt(2);
			List<WebElement> elements = runSelector(context, 0);
			if (CollectionUtils.isEmpty(elements))
				return;
			elements.get(0).click();
			if (waitSec != null)
				ThreadUtils.sleepMs(waitSec * 1000);
			context.subscript(scriptName, null);
		}
	}

	public static class CSS_ClickAndScript extends
			ClickAndScriptCommandAbstract {

		public CSS_ClickAndScript() {
			super(CommandEnum.CSS_SELECTOR_CLICK_AND_SCRIPT, Type.CSS_SELECTOR);
		}
	}

	public static class XPATH_ClickAndScript extends
			ClickAndScriptCommandAbstract {

		public XPATH_ClickAndScript() {
			super(CommandEnum.XPATH_SELECTOR_CLICK_AND_SCRIPT,
					Type.XPATH_SELECTOR);
		}
	}

	public static class ID_ClickAndScript extends ClickAndScriptCommandAbstract {

		public ID_ClickAndScript() {
			super(CommandEnum.ID_SELECTOR_CLICK_AND_SCRIPT, Type.ID_SELECTOR);
		}
	}
}
