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

package com.jaeksoft.searchlib.spellcheck;

import java.io.IOException;

import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;

import com.jaeksoft.searchlib.index.ReaderLocal;

public class SuggestionItem {

	private String term;

	private int freq;

	public SuggestionItem(String term) {
		this.term = term;
		this.freq = 0;
	}

	/**
	 * @return the term
	 */
	public String getTerm() {
		return term;
	}

	/**
	 * @return the freq
	 */
	public int getFreq() {
		return freq;
	}

	public void computeFrequency(ReaderLocal reader, String field)
			throws IOException {
		TermDocs termDocs = reader.getTermDocs(new Term(field, term));
		if (termDocs == null)
			return;
		while (termDocs.next())
			freq += termDocs.freq();
	}

}
