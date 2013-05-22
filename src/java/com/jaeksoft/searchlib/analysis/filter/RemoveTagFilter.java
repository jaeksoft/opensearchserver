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

package com.jaeksoft.searchlib.analysis.filter;

import java.io.IOException;

import org.apache.lucene.analysis.TokenStream;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.analysis.FilterFactory;
import com.jaeksoft.searchlib.util.StringUtils;

public class RemoveTagFilter extends FilterFactory {

	private static final String[] EMTPY_TAG_ARRAY = {};

	private String[] allowedTagArray = EMTPY_TAG_ARRAY;

	public class RemoveTagTokenFilter extends AbstractTermFilter {

		protected RemoveTagTokenFilter(TokenStream input) {
			super(input);

		}

		@Override
		public final boolean incrementToken() throws IOException {
			current = captureState();
			for (;;) {
				if (!input.incrementToken())
					return false;
				for (String sep : allowedTagArray)
					System.out.println(sep);
				createToken(StringUtils.removeTag(termAtt.toString(),
						allowedTagArray));
				return true;
			}
		}
	}

	@Override
	protected void checkValue(ClassPropertyEnum prop, String value)
			throws SearchLibException {
		if (prop == ClassPropertyEnum.ALLOWED_TAGS) {
			if (value == null)
				allowedTagArray = EMTPY_TAG_ARRAY;
			else
				allowedTagArray = StringUtils.split(value);
		}
	}

	@Override
	protected void initProperties() throws SearchLibException {
		super.initProperties();
		addProperty(ClassPropertyEnum.ALLOWED_TAGS, "", null);
	}

	@Override
	public TokenStream create(TokenStream tokenStream) {
		return new RemoveTagTokenFilter(tokenStream);
	}
}
