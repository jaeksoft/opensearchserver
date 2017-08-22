/*
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2012-2017 Emmanuel Keller / Jaeksoft
 * <p>
 * http://www.open-search-server.com
 * <p>
 * This file is part of OpenSearchServer.
 * <p>
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.jaeksoft.searchlib.spellcheck;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.index.ReaderLocal;
import com.qwazr.utils.FunctionUtils;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;

import java.io.IOException;

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

	public void computeFrequency(ReaderLocal reader, String field) throws IOException, SearchLibException {
		final TermDocsConsumer freqCumulator = new TermDocsConsumer();
		reader.termDocs(new Term(field, term), freqCumulator);
		freq += freqCumulator.freq;
	}

	private static class TermDocsConsumer implements FunctionUtils.ConsumerEx<TermDocs, IOException> {

		int freq = 0;

		@Override
		public void accept(TermDocs termDocs) throws IOException {
			if (termDocs == null)
				return;
			while (termDocs.next())
				freq += termDocs.freq();
		}
	}

}
