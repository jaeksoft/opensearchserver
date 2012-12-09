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
package com.jaeksoft.searchlib.analysis.filter;

import java.io.IOException;
import java.text.DecimalFormat;

import org.apache.lucene.analysis.TokenStream;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.analysis.FilterFactory;

public class NumberFormatFilter extends FilterFactory {

	private final String DEFAULT_FORMAT = "0000000000";

	private String format = DEFAULT_FORMAT;

	private String defaultValue = "0";

	@Override
	public void initProperties() throws SearchLibException {
		super.initProperties();
		addProperty(ClassPropertyEnum.NUMBER_FORMAT, DEFAULT_FORMAT, null);
		addProperty(ClassPropertyEnum.DEFAULT_VALUE, "", null);
	}

	@Override
	public void checkValue(ClassPropertyEnum prop, String value)
			throws SearchLibException {
		if (value == null || value.length() == 0)
			return;
		if (prop == ClassPropertyEnum.NUMBER_FORMAT) {
			new DecimalFormat(value);
			format = value;
		} else if (prop == ClassPropertyEnum.DEFAULT_VALUE) {
			defaultValue = value;
			if (defaultValue != null && defaultValue.length() == 0)
				defaultValue = null;
		}
	}

	@Override
	public TokenStream create(TokenStream tokenStream) {
		return new NumberFormatTermFilter(tokenStream, format);
	}

	public class NumberFormatTermFilter extends AbstractTermFilter {

		private final DecimalFormat numberFormat;

		public NumberFormatTermFilter(TokenStream input, String format) {
			super(input);
			numberFormat = new DecimalFormat(format);
		}

		@Override
		public final boolean incrementToken() throws IOException {
			current = captureState();
			if (!input.incrementToken())
				return false;
			try {
				String term = numberFormat.format(new Double(termAtt.term()));
				if (term != null)
					createToken(term);
			} catch (NumberFormatException e) {
				if (defaultValue == null)
					return false;
				createToken(defaultValue);
			}
			return true;
		}
	}
}
