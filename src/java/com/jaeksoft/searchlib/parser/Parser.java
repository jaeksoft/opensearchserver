/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft SearchLib Community.
 *
 * Jaeksoft SearchLib Community is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft SearchLib Community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft SearchLib Community. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import com.jaeksoft.searchlib.basket.BasketDocument;

public abstract class Parser {

	private long sizeLimit;

	protected BasketDocument basketDocument;

	protected Parser() {
		sizeLimit = 0;
		basketDocument = new BasketDocument();
	}

	public void setSizeLimit(long l) {
		sizeLimit = l;
	}

	public BasketDocument getBasketDocument() {
		return basketDocument;
	}

	protected abstract void parseContent(LimitInputStream inputStream)
			throws IOException;

	protected abstract void parseContent(LimitReader reader) throws IOException;

	public void parseContent(InputStream inputStream) throws IOException {
		parseContent(new LimitInputStream(inputStream, sizeLimit));
	}

	public void parseContent(Reader reader) throws IOException {
		parseContent(new LimitReader(reader, sizeLimit));
	}

	public void parseContent(byte[] byteData) throws IOException {
		ByteArrayInputStream inputStream = null;
		try {
			inputStream = new ByteArrayInputStream(byteData);
			parseContent(inputStream);
		} finally {
			if (inputStream != null)
				inputStream.close();
		}
	}

	public void parseContent(String stringData) throws IOException {
		StringReader stringReader = null;
		try {
			new StringReader(stringData);
			parseContent(stringReader);
		} finally {
			if (stringReader != null)
				stringReader.close();
		}
	}

}
