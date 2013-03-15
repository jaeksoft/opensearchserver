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
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.TokenStream;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.analysis.FilterFactory;

public class URLNormalizerFilter extends FilterFactory {

	public class URLNormalizerTokenFilter extends AbstractTermFilter {

		protected URLNormalizerTokenFilter(TokenStream input) {
			super(input);
		}

		@Override
		public final boolean incrementToken() throws IOException {
			current = captureState();
			for (;;) {
				if (!input.incrementToken())
					return false;
				String term = termAtt.toString();
				String[] part = StringUtils.split(term, '|');
				if (part != null) {
					try {
						URL url = new URL(part[0]);
						if (part.length > 1)
							url = new URL(url, part[1]);
						createToken(url.toExternalForm());
					} catch (MalformedURLException e) {
						Logging.info(e.getMessage());
					}
				}
				return true;
			}
		}
	}

	@Override
	public TokenStream create(TokenStream tokenStream) {
		return new URLNormalizerTokenFilter(tokenStream);
	}
}
