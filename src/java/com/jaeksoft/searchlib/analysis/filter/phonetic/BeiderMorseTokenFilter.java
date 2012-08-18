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
package com.jaeksoft.searchlib.analysis.filter.phonetic;

import java.io.IOException;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.language.bm.NameType;
import org.apache.commons.codec.language.bm.PhoneticEngine;
import org.apache.commons.codec.language.bm.RuleType;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.TokenStream;

import com.jaeksoft.searchlib.analysis.filter.AbstractTermFilter;

public class BeiderMorseTokenFilter extends AbstractTermFilter {

	private String[] wordQueue = null;
	private int currentPos = 0;
	private PhoneticEngine encoder;

	public BeiderMorseTokenFilter(TokenStream input) {
		super(input);
		encoder = new PhoneticEngine(NameType.GENERIC, RuleType.APPROX, true,
				10);
	}

	private final boolean popToken() {
		if (wordQueue == null)
			return false;
		if (currentPos == wordQueue.length)
			return false;
		createToken(wordQueue[currentPos++]);
		return true;
	}

	private final void createTokens() throws EncoderException {
		String encoded = encoder.encode(termAtt.term());
		wordQueue = StringUtils.split(encoded, '|');
		currentPos = 0;
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
				createTokens();
			} catch (EncoderException e) {
				throw new IOException(e);
			}
		}
	}

	public static void main(String[] args) {
		PhoneticEngine encoder = new PhoneticEngine(NameType.GENERIC,
				RuleType.APPROX, true, 10);
		for (int i = 0; i < 10; i++)
			System.out.println(encoder.encode("test"));
	}
}
