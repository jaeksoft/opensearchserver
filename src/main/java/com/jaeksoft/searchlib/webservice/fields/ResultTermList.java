/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2015 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.webservice.fields;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;

@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@XmlRootElement(name = "result")
public class ResultTermList {

	@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
	@XmlRootElement(name = "term")
	public static class ResultTermFreq {

		final public String term;

		final public Integer freq;

		public ResultTermFreq() {
			term = null;
			freq = null;
		}

		public ResultTermFreq(String term, Integer freq) {
			this.term = term;
			this.freq = freq;
		}
	}

	public final List<ResultTermFreq> terms;

	public ResultTermList() {
		terms = null;
	}

	public ResultTermList(Client client, String field, String term, Integer start, Integer rows)
			throws IOException, SearchLibException {
		terms = buildTermList(client, field, term, start, rows);
	}

	private static List<ResultTermFreq> buildTermList(Client client, String field, String prefix, Integer start,
			Integer rows) throws SearchLibException, IOException {
		List<ResultTermFreq> terms = new ArrayList<ResultTermFreq>();
		final TermEnum termEnum = client.getTermEnum(new Term(field, prefix));
		try {
			if (start == null)
				start = 0;
			if (rows == null)
				rows = 10;
			while (start-- != 0)
				if (!termEnum.next())
					return terms;
			while (rows-- != 0) {
				final Term term = termEnum.term();
				if (!term.field().equals(field))
					break;
				final String word = term.text();
				if (!word.startsWith(prefix))
					break;
				terms.add(new ResultTermFreq(word, termEnum.docFreq()));
				if (!termEnum.next())
					break;
			}
			return terms;
		} finally {
			termEnum.close();
		}
	}

}
