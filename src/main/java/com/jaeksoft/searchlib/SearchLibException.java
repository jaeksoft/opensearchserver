/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib;

import com.jaeksoft.searchlib.parser.htmlParser.HtmlDocumentProvider;

public class SearchLibException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1720638403781547142L;

	public SearchLibException(Exception cause) {
		super(cause);
	}

	public SearchLibException(String message, Exception cause) {
		super(message, cause);
	}

	public SearchLibException(String message) {
		super(message);
	}

	public static class AbortException extends SearchLibException {
		/**
		 * 
		 */
		private static final long serialVersionUID = -1855323556480709778L;

		public AbortException() {
			super("Aborted");
		}

		public AbortException(String message) {
			super(message);
		}
	}

	public static class UniqueKeyMissing extends SearchLibException {

		/**
		 * 
		 */
		private static final long serialVersionUID = -8504346848728230027L;

		public UniqueKeyMissing(String uniqueField) {
			super("The unique key is missing: " + uniqueField);
		}

	}

	public static class XPathNotSupported extends SearchLibException {

		/**
		 * 
		 */
		private static final long serialVersionUID = -75701917881887018L;

		public XPathNotSupported(HtmlDocumentProvider hdp) {
			super("This HTML provider (" + hdp.getName()
					+ ") does not support XPATH request");
		}

	}

	public static class ExternalParserException extends SearchLibException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 748700519607025552L;

		public ExternalParserException(String message) {
			super(message);
		}
	}

}
