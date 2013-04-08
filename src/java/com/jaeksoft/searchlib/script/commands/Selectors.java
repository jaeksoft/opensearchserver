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

import org.openqa.selenium.By;

import com.jaeksoft.searchlib.script.CommandAbstract;
import com.jaeksoft.searchlib.script.CommandEnum;
import com.jaeksoft.searchlib.script.ScriptCommandContext;
import com.jaeksoft.searchlib.script.ScriptException;

public class Selectors {

	public enum Type {
		CSS_SELECTOR, XPATH_SELECTOR;
	}

	public static class Selector implements Comparable<Selector> {
		public final Type type;
		public final String query;

		public Selector(Type type, String query) {
			this.type = type;
			// Avoid null value in query part (for comparison)
			this.query = query == null ? "" : query;
		}

		public final By getBy() {
			switch (type) {
			case CSS_SELECTOR:
				return new By.ByCssSelector(query);
			case XPATH_SELECTOR:
				return new By.ByXPath(query);
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
	}

	public static class CSS_Add extends CommandAbstract {

		public CSS_Add() {
			super(CommandEnum.CSS_SELECTOR_ADD);
		}

		@Override
		public void run(ScriptCommandContext context, String id,
				Object... parameters) throws ScriptException {
			checkParameters(1, parameters);
			context.addSelector(new Selector(Type.CSS_SELECTOR,
					getParameterString(0)));
		}

	}

	public static class CSS_Reset extends CommandAbstract {

		public CSS_Reset() {
			super(CommandEnum.CSS_SELECTOR_RESET);
		}

		@Override
		public void run(ScriptCommandContext context, String id,
				Object... parameters) throws ScriptException {
			context.resetSelector(Type.CSS_SELECTOR);
		}

	}

	public static class XPATH_Add extends CommandAbstract {

		public XPATH_Add() {
			super(CommandEnum.XPATH_SELECTOR_ADD);
		}

		@Override
		public void run(ScriptCommandContext context, String id,
				Object... parameters) throws ScriptException {
			checkParameters(1, parameters);
			context.addSelector(new Selector(Type.XPATH_SELECTOR,
					getParameterString(0)));
		}
	}

	public static class XPATH_Reset extends CommandAbstract {

		public XPATH_Reset() {
			super(CommandEnum.XPATH_SELECTOR_RESET);
		}

		@Override
		public void run(ScriptCommandContext context, String id,
				Object... parameters) throws ScriptException {
			context.resetSelector(Type.XPATH_SELECTOR);
		}

	}

	public static class ALL_Reset extends CommandAbstract {

		public ALL_Reset() {
			super(CommandEnum.ALL_SELECTOR_RESET);
		}

		@Override
		public void run(ScriptCommandContext context, String id,
				Object... parameters) throws ScriptException {
			context.resetSelector(null);
		}

	}
}
