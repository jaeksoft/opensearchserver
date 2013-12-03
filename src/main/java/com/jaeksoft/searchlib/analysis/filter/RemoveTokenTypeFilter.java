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

public class RemoveTokenTypeFilter extends FilterFactory {

	private String type = null;

	@Override
	public void initProperties() throws SearchLibException {
		super.initProperties();
		addProperty(ClassPropertyEnum.TOKEN_TYPE, "shingle", null, 20, 0);
	}

	@Override
	public void checkValue(ClassPropertyEnum prop, String value)
			throws SearchLibException {
		if (prop == ClassPropertyEnum.TOKEN_TYPE)
			type = value;
	}

	@Override
	public TokenStream create(TokenStream tokenStream) {
		return new RemoveTokenTypeTokenFilter(tokenStream, type);
	}

	public class RemoveTokenTypeTokenFilter extends AbstractTermFilter {

		private final String type;

		public RemoveTokenTypeTokenFilter(TokenStream input, String type) {
			super(input);
			this.type = type;
		}

		@Override
		public final boolean incrementToken() throws IOException {
			int skippedPositions = 0;
			for (;;) {
				if (!input.incrementToken())
					return false;
				if (!type.equals(typeAtt.type())) {
					posIncrAtt.setPositionIncrement(posIncrAtt
							.getPositionIncrement() + skippedPositions);
					return true;
				}
				skippedPositions += posIncrAtt.getPositionIncrement();
			}
		}
	}
}
