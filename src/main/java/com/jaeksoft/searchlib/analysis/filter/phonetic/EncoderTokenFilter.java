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
import org.apache.commons.codec.StringEncoder;
import org.apache.lucene.analysis.TokenStream;

import com.jaeksoft.searchlib.analysis.filter.AbstractTermFilter;

public class EncoderTokenFilter extends AbstractTermFilter {

	private StringEncoder encoder;

	public EncoderTokenFilter(TokenStream input, StringEncoder encoder) {
		super(input);
		this.encoder = encoder;
	}

	@Override
	public final boolean incrementToken() throws IOException {
		if (!input.incrementToken())
			return false;
		try {
			createToken(encoder.encode(termAtt.toString()));
		} catch (EncoderException e) {
			throw new IOException(e);
		}
		return true;
	}
}
