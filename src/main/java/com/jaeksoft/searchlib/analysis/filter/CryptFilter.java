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

import org.apache.commons.codec.digest.Crypt;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.TokenStream;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.analysis.FilterFactory;

public class CryptFilter extends FilterFactory {

	private class CryptTokenFilter extends AbstractTermFilter {

		private final String salt;

		protected CryptTokenFilter(TokenStream input, String salt) {
			super(input);
			this.salt = StringUtils.isEmpty(salt) ? null : salt;
		}

		@Override
		public boolean incrementToken() throws IOException {
			for (;;) {
				if (!input.incrementToken())
					return false;
				String term = salt == null ? Crypt.crypt(termAtt.toString())
						: Crypt.crypt(termAtt.toString(), salt);
				createToken(term);
				return true;
			}
		}
	}

	private String salt = null;

	@Override
	protected void initProperties() throws SearchLibException {
		super.initProperties();
		addProperty(ClassPropertyEnum.SALT, "oss2pepper", null, 20, 0);
	}

	@Override
	protected void checkValue(ClassPropertyEnum prop, String value)
			throws SearchLibException {
		if (prop == ClassPropertyEnum.SALT)
			salt = value;
	}

	@Override
	public TokenStream create(TokenStream tokenStream) {
		return new CryptTokenFilter(tokenStream, salt);
	}

}
