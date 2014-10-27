/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2014 Emmanuel Keller / Jaeksoft
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

import java.util.List;

import org.apache.lucene.analysis.TokenStream;

import dk.dren.hunspell.Hunspell;

public class HunspellSuggestFilter extends HunspellStemFilter {

	@Override
	protected TokenStream newTokenFilter(TokenStream input,
			Hunspell.Dictionary dict) {
		return new HunspellSuggestTokenFilter(input, dict);
	}

	public static class HunspellSuggestTokenFilter extends
			HunspellStemTokenFilter {

		public HunspellSuggestTokenFilter(TokenStream input,
				Hunspell.Dictionary dict) {
			super(input, dict);
		}

		@Override
		protected List<String> getWords(String currentTerm) {
			List<String> words = hunspell_dict.suggest(currentTerm);
			return words.contains(currentTerm) ? null : words;
		}

	}

}
