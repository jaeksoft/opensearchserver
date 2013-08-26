/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2013 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.analysis.filter.domain;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.lucene.analysis.TokenStream;

import com.jaeksoft.searchlib.util.LinkUtils;

public class HostnameTokenFilter extends CommonDomainTokenFilter {

	public HostnameTokenFilter(TokenStream input, boolean silent) {
		super(input, silent);
	}

	@Override
	public final boolean incrementToken() throws IOException {
		if (!input.incrementToken())
			return false;
		try {
			URL url = LinkUtils.newEncodedURL(termAtt.toString());
			termAtt.setEmpty();
			termAtt.append(url.getHost());
		} catch (MalformedURLException e) {
			if (silent)
				return false;
			throw e;
		} catch (IllegalArgumentException e) {
			if (silent)
				return false;
			throw e;
		} catch (URISyntaxException e) {
			if (silent)
				return false;
			throw new IOException(e);
		}
		return true;
	}
}
