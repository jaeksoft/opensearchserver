/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011 Emmanuel Keller / Jaeksoft
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
import java.net.URL;

import org.apache.lucene.analysis.TokenStream;

import com.google.common.net.InternetDomainName;

public class TldTokenFilter extends CommonDomainTokenFilter {

	public TldTokenFilter(TokenStream input, boolean silent) {
		super(input, silent);
	}

	@Override
	public final boolean incrementToken() throws IOException {
		current = captureState();
		if (!input.incrementToken())
			return false;
		try {
			URL url = new URL(termAtt.term());
			InternetDomainName domainName = InternetDomainName.fromLenient(url
					.getHost());
			termAtt.setTermBuffer(domainName.publicSuffix().name());
		} catch (MalformedURLException e) {
			if (silent)
				return false;
			throw e;
		} catch (IllegalArgumentException e) {
			if (silent)
				return false;
			throw e;
		}
		return true;
	}
}
