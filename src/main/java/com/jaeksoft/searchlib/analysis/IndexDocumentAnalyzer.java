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

package com.jaeksoft.searchlib.analysis;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

import com.jaeksoft.searchlib.webservice.query.document.IndexDocumentResult;
import com.jaeksoft.searchlib.webservice.query.document.IndexDocumentResult.IndexField;
import com.jaeksoft.searchlib.webservice.query.document.IndexDocumentResult.IndexTerm;

public class IndexDocumentAnalyzer extends AbstractAnalyzer {

	private final Map<String, IndexField> fieldMap;

	public IndexDocumentAnalyzer(IndexDocumentResult document) {
		fieldMap = new HashMap<String, IndexField>();
		if (document.fields != null)
			for (IndexField indexField : document.fields)
				if (indexField.terms != null)
					fieldMap.put(indexField.field, indexField);
	}

	@Override
	public TokenStream tokenStream(String fieldName, Reader reader) {
		return new IndexDocumentTokenStream(fieldMap.get(fieldName));
	}

	final public class IndexDocumentTokenStream extends TokenStream {

		private final List<IndexTerm> indexTerms;

		private final int size;

		private int posTerm;

		private int posVector;

		private IndexTerm indexTerm;

		private CharTermAttribute termAtt = (CharTermAttribute) addAttribute(CharTermAttribute.class);
		private PositionIncrementAttribute posIncrAtt = (PositionIncrementAttribute) addAttribute(PositionIncrementAttribute.class);
		private OffsetAttribute offsetAtt = (OffsetAttribute) addAttribute(OffsetAttribute.class);

		private IndexDocumentTokenStream(IndexField indexField) {
			indexTerms = indexField == null ? null : indexField.terms;
			size = indexTerms == null ? 0 : indexTerms.size();
			posTerm = 0;
			posVector = -1;
		}

		@Override
		final public boolean incrementToken() throws IOException {
			if (posTerm == size)
				return false;
			if (posVector == -1) {
				indexTerm = indexTerms.get(posTerm);
				posVector = 0;
			}
			termAtt.setEmpty();
			termAtt.append(indexTerm.t);
			if (indexTerm.p != null)
				posIncrAtt.setPositionIncrement(indexTerm.p[posVector]);
			if (indexTerm.s != null)
				offsetAtt.setOffset(indexTerm.s[posVector],
						indexTerm.e[posVector]);
			if (++posVector == indexTerm.f) {
				posVector = -1;
				posTerm++;
			}
			return true;
		}
	}
}
