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

package com.jaeksoft.searchlib.script;

public class ScriptException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3095402923384217353L;

	public ScriptException(String message) {
		super(message);
	}

	public ScriptException(String message, Exception cause) {
		super(message, cause);
	}

	public ScriptException(Exception e) {
		super(e);
	}

	public static class ExitException extends ScriptException {

		private static final long serialVersionUID = -2964160037418663922L;

		public ExitException() {
			super("Exit");
		}

	}
}
