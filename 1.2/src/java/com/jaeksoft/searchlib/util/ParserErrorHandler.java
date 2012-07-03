/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.util;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.jaeksoft.searchlib.Logging;

public class ParserErrorHandler implements ErrorHandler {

	private boolean silent;

	public static final ParserErrorHandler SILENT_ERROR_HANDLER = new ParserErrorHandler(
			true);

	public static final ParserErrorHandler STANDARD_ERROR_HANDLER = new ParserErrorHandler(
			false);

	private ParserErrorHandler(boolean silent) {
		this.silent = silent;
	}

	@Override
	public void error(SAXParseException e) throws SAXException {
		if (silent)
			return;
		Logging.error(e.getMessage());
	}

	@Override
	public void fatalError(SAXParseException e) throws SAXException {
		if (silent)
			return;
		Logging.error(e.getMessage());

	}

	@Override
	public void warning(SAXParseException e) throws SAXException {
		if (silent)
			return;
		Logging.warn(e.getMessage());
	}

}
