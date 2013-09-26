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

import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.support.pagefactory.ByAll;

import com.jaeksoft.searchlib.script.CommandAbstract;
import com.jaeksoft.searchlib.script.CommandEnum;
import com.jaeksoft.searchlib.script.ScriptCommandContext;
import com.jaeksoft.searchlib.script.ScriptException;

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

		public Selector(Type type, String query, boolean disableScript,
				boolean screenshotHighlight, boolean clickCapture) {
			this.type = type;
			// Avoid null value in query part (for comparison)
			this.query = query == null ? "" : query;
			this.disableScript = disableScript;
			this.screenshotHighlight = screenshotHighlight;
			this.clickCapture = clickCapture;
		}

		public Selector(Type type, String query) {
			this(type, query, false, false, false);
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

		protected SelectorCommandAbstract(CommandEnum commandEnum) {
			super(commandEnum);
		}

		public final static String PARAM_DISABLE_SCRIPT = "disable_script";
		public final static String PARAM_SCREENSHOT_HIGHLIGHT = "screenshot_highlight";
		public final static String PARAM_CLICK_CAPTURE = "click_capture";

		protected Selector newSelector(Type type) throws ScriptException {
			boolean disableScript = false;
			boolean screenshotHighlight = false;
			boolean clickCapture = false;
			for (int i = 1; i < getParameterCount(); i++) {
				String param = getParameterString(i);
				if (param == null)
					continue;
				if (param.length() == 0)
					continue;
				if (PARAM_DISABLE_SCRIPT.equalsIgnoreCase(param))
					disableScript = true;
				else if (PARAM_SCREENSHOT_HIGHLIGHT.equalsIgnoreCase(param))
					screenshotHighlight = true;
				else if (PARAM_CLICK_CAPTURE.equalsIgnoreCase(param))
					clickCapture = true;
				else
					throw new ScriptException("Unknown parameter: " + param);
			}
			return new Selector(type, getParameterString(0), disableScript,
					screenshotHighlight, clickCapture);
		}
	}

	public static class CSS_Add extends SelectorCommandAbstract {

		public CSS_Add() {
			super(CommandEnum.CSS_SELECTOR_ADD);
		}

		@Override
		public void run(ScriptCommandContext context, String id,
				String... parameters) throws ScriptException {
			checkParameters(1, parameters);
			context.addSelector(newSelector(Type.CSS_SELECTOR));
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

	public static class XPATH_Add extends SelectorCommandAbstract {

		public XPATH_Add() {
			super(CommandEnum.XPATH_SELECTOR_ADD);
		}

		@Override
		public void run(ScriptCommandContext context, String id,
				String... parameters) throws ScriptException {
			checkParameters(1, parameters);
			context.addSelector(newSelector(Type.XPATH_SELECTOR));
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

	public static class ID_Add extends SelectorCommandAbstract {

		public ID_Add() {
			super(CommandEnum.ID_SELECTOR_ADD);
		}

		@Override
		public void run(ScriptCommandContext context, String id,
				String... parameters) throws ScriptException {
			checkParameters(1, parameters);
			context.addSelector(newSelector(Type.ID_SELECTOR));
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

}
