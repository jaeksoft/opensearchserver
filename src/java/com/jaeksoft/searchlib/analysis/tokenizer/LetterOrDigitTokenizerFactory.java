/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2010 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.analysis.tokenizer;

import java.io.Reader;

import org.apache.lucene.analysis.CharTokenizer;
import org.apache.lucene.analysis.Tokenizer;

public class LetterOrDigitTokenizerFactory extends TokenizerFactory {

	public class LetterOrDigitTokenizer extends CharTokenizer {

		public LetterOrDigitTokenizer(Reader input) {
			super(input);
		}

		@Override
		protected boolean isTokenChar(char c) {
			return Character.isLetterOrDigit(c);
		}
	}

	@Override
	public Tokenizer create(Reader reader) {
		return new LetterOrDigitTokenizer(reader);
	}

}
