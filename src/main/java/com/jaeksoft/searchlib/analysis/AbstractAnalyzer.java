/*
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2013-2017 Emmanuel Keller / Jaeksoft
 * <p>
 * http://www.open-search-server.com
 * <p>
 * This file is part of OpenSearchServer.
 * <p>
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.jaeksoft.searchlib.analysis;

import org.apache.lucene.analysis.TokenStream;

import java.io.IOException;
import java.io.Reader;

public abstract class AbstractAnalyzer extends org.apache.lucene.analysis.Analyzer {

	private boolean superClosed;

	public AbstractAnalyzer() {
		superClosed = false;
	}

	@Override
	public final TokenStream reusableTokenStream(final String fieldName, final Reader reader) throws IOException {
		return tokenStream(fieldName, reader);
	}

	final public int getPositionIncrementGap(String fieldName) {
		return 100;
	}

	@Override
	public void close() {
		if (superClosed)
			return;
		super.close();
		superClosed = true;
	}
}
