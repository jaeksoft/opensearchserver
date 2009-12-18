/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2009 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.analysis;

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.AttributeSource;

public class SynonymTokenFilter extends TokenFilter {

	public static final String SYNONYM_TOKEN_TYPE = "SYNONYM";

	private String[] synonyms = null;
	private int index = 0;
	private SynonymMap synonymMap = null;

	private TermAttribute termAtt;
	private TypeAttribute typeAtt;
	private PositionIncrementAttribute posIncrAtt;

	private AttributeSource.State current = null;

	protected SynonymTokenFilter(TokenStream input, SynonymMap synonymMap) {
		super(input);
		this.synonymMap = synonymMap;
		this.termAtt = (TermAttribute) addAttribute(TermAttribute.class);
		this.typeAtt = (TypeAttribute) addAttribute(TypeAttribute.class);
		this.posIncrAtt = (PositionIncrementAttribute) addAttribute(PositionIncrementAttribute.class);
	}

	private final void createToken(String synonym, State current) {
		restoreState(current);
		termAtt.setTermBuffer(synonym);
		typeAtt.setType(SYNONYM_TOKEN_TYPE);
		posIncrAtt.setPositionIncrement(0);
		System.out.println("Create token: " + synonym);
	}

	@Override
	public final boolean incrementToken() throws IOException {
		if (synonyms != null)
			while (index < synonyms.length)
				createToken(synonyms[index++], current);

		if (!input.incrementToken())
			return false;

		synonyms = synonymMap.getSynonyms(termAtt.term());
		index = 0;
		current = captureState();
		return true;
	}
}
