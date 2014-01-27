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
import java.io.StringReader;
import java.util.List;

import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.apache.cxf.helpers.DOMUtils;
import org.apache.lucene.analysis.TokenStream;
import org.w3c.dom.Document;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.analysis.FilterFactory;
import com.jaeksoft.searchlib.util.XPathParser;

public class XPathFilter extends FilterFactory {

	public class XPathTokenFilter extends AbstractTermFilter {

		private XPathExpression xPathExpression = null;

		private List<?> tokenList = null;

		private int currentPos = 0;

		protected XPathTokenFilter(TokenStream input,
				XPathExpression xPathExpression) {
			super(input);
			this.xPathExpression = xPathExpression;
		}

		private final boolean popToken() {
			if (tokenList == null)
				return false;
			if (currentPos == tokenList.size())
				return false;
			createToken(tokenList.get(currentPos++).toString());
			return true;
		}

		private final boolean isDefaultValue() {
			if (defaultValue == null)
				return false;
			return defaultValue.length() != 0;
		}

		private final boolean defaultValueToken() {
			createToken(defaultValue);
			return true;
		}

		@Override
		public final boolean incrementToken() throws IOException {
			try {
				for (;;) {
					if (popToken())
						return true;
					if (!input.incrementToken())
						return false;
					Document document = DOMUtils.readXml(new StringReader(
							termAtt.toString()));
					Object object = xPathExpression.evaluate(document);
					if (object == null) {
						if (isDefaultValue())
							return defaultValueToken();
						continue;
					}
					if (object instanceof String) {
						String s = (String) object;
						if (s.length() > 0)
							return createToken(s);
						if (isDefaultValue())
							return defaultValueToken();
						continue;
					}
					if (object instanceof List) {
						List<?> list = (List<?>) object;
						if (list.size() == 0)
							if (isDefaultValue())
								return defaultValueToken();
						tokenList = (List<?>) object;
						currentPos = 0;
					}
				}
			} catch (Exception e) {
				if (faultTolerant) {
					Logging.warn(e);
					if (isDefaultValue())
						return defaultValueToken();
					return false;
				}
				if (e instanceof IOException)
					throw (IOException) e;
				else
					throw new IOException(e);
			}
		}
	}

	private XPathExpression xPathExpression = null;
	public boolean faultTolerant = true;
	public String defaultValue = null;

	@Override
	public void initProperties() throws SearchLibException {
		super.initProperties();
		addProperty(ClassPropertyEnum.XPATH, "", null, 30, 1);
		addProperty(ClassPropertyEnum.DEFAULT_VALUE, "", null, 20, 1);
		addProperty(ClassPropertyEnum.FAULT_TOLERANT,
				ClassPropertyEnum.BOOLEAN_LIST[0],
				ClassPropertyEnum.BOOLEAN_LIST, 0, 0);
	}

	@Override
	public void checkValue(ClassPropertyEnum prop, String value)
			throws SearchLibException {
		if (value == null || value.length() == 0)
			return;
		if (prop == ClassPropertyEnum.XPATH) {
			if (value != null && value.length() > 0)
				try {
					xPathExpression = XPathParser.getXPath().compile(value);
				} catch (XPathExpressionException e) {
					throw new SearchLibException(e);
				}
			else
				xPathExpression = null;
		} else if (prop == ClassPropertyEnum.FAULT_TOLERANT)
			faultTolerant = Boolean.parseBoolean(value);
		else if (prop == ClassPropertyEnum.DEFAULT_VALUE)
			defaultValue = value;
	}

	@Override
	public TokenStream create(TokenStream tokenStream) {
		return new XPathTokenFilter(tokenStream, xPathExpression);
	}
}
