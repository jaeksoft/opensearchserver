/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012-2013 Emmanuel Keller / Jaeksoft
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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.TokenStream;

import com.jaeksoft.searchlib.util.RegExpUtils;

public class RegularExpressionTokenFilter extends AbstractTermFilter implements
		RegExpUtils.MatchGroupListener {

	private List<String> termQueue = null;

	private int currentPos = 0;

	private final Pattern pattern;

	protected RegularExpressionTokenFilter(Pattern pattern, TokenStream input) {
		super(input);
		termQueue = new ArrayList<String>(0);
		this.pattern = pattern;
	}

	private final boolean popToken() {
		if (termQueue.size() == 0)
			return false;
		if (currentPos == termQueue.size())
			return false;
		createToken(termQueue.get(currentPos++));
		return true;
	}

	private final void createTokens() {
		termQueue.clear();
		currentPos = 0;

		synchronized (pattern) {
			RegExpUtils.groupExtractor(pattern, termAtt.toString(), this);
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

	@Override
	public void match(int start, int end) {
	}

	@Override
	public void group(int start, int end, String content) {
		termQueue.add(content);
	}
}
