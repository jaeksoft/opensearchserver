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
import java.util.List;

import org.apache.lucene.analysis.TokenStream;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.analysis.FilterFactory;
import com.jayway.jsonpath.JsonPath;

public class JsonPathFilter extends FilterFactory {

	public class JsonPathTokenFilter extends AbstractTermFilter {

		private JsonPath jsonPath = null;

		private List<?> tokenList = null;

		private int currentPos = 0;

		protected JsonPathTokenFilter(TokenStream input, JsonPath jPath) {
			super(input);
			this.jsonPath = jPath;
		}

		private final boolean popToken() {
			if (tokenList == null)
				return false;
			if (currentPos == tokenList.size())
				return false;
			createToken(tokenList.get(currentPos++).toString());
			return true;
		}

		@Override
		public final boolean incrementToken() throws IOException {
			current = captureState();
			for (;;) {
				if (popToken())
					return true;
				if (!input.incrementToken())
					return false;
				try {
					Object object = jsonPath.read(termAtt.term());
					if (object instanceof String) {
						createToken(object.toString());
						return true;
					} else if (object instanceof List) {
						tokenList = (List<?>) object;
						currentPos = 0;
					}
				} catch (Exception e) {
					if (faultTolerant) {
						Logging.warn(e);
						return false;
					}
					if (e instanceof IOException)
						throw (IOException) e;
					else
						throw new IOException(e);
				}
			}
		}
	}

	private JsonPath jsonPath = null;
	public boolean faultTolerant = true;

	@Override
	public void initProperties() throws SearchLibException {
		super.initProperties();
		addProperty(ClassPropertyEnum.JSON_PATH, "", null);
		addProperty(ClassPropertyEnum.FAULT_TOLERANT,
				ClassPropertyEnum.BOOLEAN_LIST[0],
				ClassPropertyEnum.BOOLEAN_LIST);
	}

	@Override
	public void checkValue(ClassPropertyEnum prop, String value)
			throws SearchLibException {
		if (value == null || value.length() == 0)
			return;
		if (prop == ClassPropertyEnum.JSON_PATH) {
			if (value != null && value.length() > 0)
				jsonPath = JsonPath.compile(value);
			else
				jsonPath = null;
		} else if (prop == ClassPropertyEnum.FAULT_TOLERANT)
			faultTolerant = Boolean.parseBoolean(value);
	}

	@Override
	public TokenStream create(TokenStream tokenStream) {
		return new JsonPathTokenFilter(tokenStream, jsonPath);
	}
}
