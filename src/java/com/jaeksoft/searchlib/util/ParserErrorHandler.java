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

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.jaeksoft.searchlib.Logging;

public class ParserErrorHandler implements ErrorHandler, ErrorListener {

	private boolean silent;

	private boolean logOnly;

	public static final ParserErrorHandler SILENT_ERROR_HANDLER = new ParserErrorHandler(
			true, true);

	public static final ParserErrorHandler LOGONLY_ERROR_HANDLER = new ParserErrorHandler(
			false, true);

	public static final ParserErrorHandler STANDARD_ERROR_HANDLER = new ParserErrorHandler(
			false, false);

	private ParserErrorHandler(boolean silent, boolean logOnly) {
		this.silent = silent;
		this.logOnly = logOnly;
	}

	private final void handleError(SAXParseException e) throws SAXException {
		if (silent)
			return;
		if (logOnly)
			Logging.error(e.getMessage());
		else
			throw e;
	}

	private final void handleError(TransformerException e)
			throws TransformerException {
		if (silent)
			return;
		if (logOnly)
			Logging.error(e.getMessage());
		else
			throw e;
	}

	@Override
	public void error(SAXParseException e) throws SAXException {
		handleError(e);
	}

	@Override
	public void fatalError(SAXParseException e) throws SAXException {
		handleError(e);
	}

	@Override
	public void warning(SAXParseException e) throws SAXException {
		handleError(e);
	}

	@Override
	public void error(TransformerException e) throws TransformerException {
		handleError(e);
	}

	@Override
	public void fatalError(TransformerException e) throws TransformerException {
		handleError(e);
	}

	@Override
	public void warning(TransformerException e) throws TransformerException {
		handleError(e);
	}

}
