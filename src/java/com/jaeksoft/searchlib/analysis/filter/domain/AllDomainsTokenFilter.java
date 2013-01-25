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
import java.util.List;

import org.apache.lucene.analysis.TokenStream;

import com.jaeksoft.searchlib.crawler.web.database.UrlItem;

public class AllDomainsTokenFilter extends CommonDomainTokenFilter {

	private List<String> subDomainQueue = null;

	private int currentPos = 0;

	public AllDomainsTokenFilter(TokenStream input, boolean silent) {
		super(input, silent);
	}

	private final boolean popToken() {
		if (subDomainQueue == null)
			return false;
		if (currentPos == subDomainQueue.size())
			return false;
		createToken(subDomainQueue.get(currentPos++));
		return true;
	}

	private final void createTokens() throws MalformedURLException {
		try {
			URL url = new URL(termAtt.toString());
			subDomainQueue = UrlItem.buildSubHost(url.getHost());
			currentPos = 0;
		} catch (MalformedURLException e) {
			if (!silent)
				throw e;
		}
	}

	@Override
	public final boolean incrementToken() throws IOException {
		current = captureState();
		for (;;) {
			if (popToken())
				return true;
			if (!input.incrementToken())
				return false;
			createTokens();
		}
	}
}
