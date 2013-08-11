/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.request;

import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PhraseQuery;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.analysis.TokenQueryFilter;
import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.XmlWriter;

public class SearchField implements Cloneable {

	public final static String SEARCHFIELD_NODE_NAME = "searchField";
	public final static String SEARCHFIELD_ATTRIBUTE_FIELD_NAME = "field";
	public final static String SEARCHFIELD_ATTRIBUTE_PHRASE = "phrase";
	public final static String SEARCHFIELD_ATTRIBUTE_BOOST = "boost";

	private String field;
	private boolean phrase;
	private float boost;

	private SearchField(SearchField searchField) {
		this.field = searchField.field;
		this.phrase = searchField.phrase;
		this.boost = searchField.boost;
	}

	public SearchField(String field, boolean phrase, float boost) {
		this.field = field;
		this.phrase = phrase;
		this.boost = boost;
	}

	public SearchField(Node fieldNode) {
		this.field = DomUtils.getAttributeText(fieldNode,
				SEARCHFIELD_ATTRIBUTE_FIELD_NAME);
		this.phrase = Boolean.parseBoolean(DomUtils.getAttributeText(fieldNode,
				SEARCHFIELD_ATTRIBUTE_PHRASE));
		this.boost = Float.parseFloat(DomUtils.getAttributeText(fieldNode,
				SEARCHFIELD_ATTRIBUTE_BOOST));
	}

	@Override
	public SearchField clone() {
		return new SearchField(this);
	}

	/**
	 * @return the field
	 */
	public String getField() {
		return field;
	}

	/**
	 * @param field
	 *            the field to set
	 */
	public void setField(String field) {
		this.field = field;
	}

	/**
	 * @return the phrase
	 */
	public boolean isPhrase() {
		return phrase;
	}

	/**
	 * @param phrase
	 *            the phrase to set
	 */
	public void setPhrase(boolean phrase) {
		this.phrase = phrase;
	}

	/**
	 * @return the boost
	 */
	public float getBoost() {
		return boost;
	}

	/**
	 * @param boost
	 *            the boost to set
	 */
	public void setBoost(float boost) {
		this.boost = boost;
	}

	public void writeXmlConfig(XmlWriter xmlWriter) throws SAXException {
		xmlWriter.startElement(SEARCHFIELD_NODE_NAME,
				SEARCHFIELD_ATTRIBUTE_FIELD_NAME, field,
				SEARCHFIELD_ATTRIBUTE_PHRASE, Boolean.toString(phrase),
				SEARCHFIELD_ATTRIBUTE_BOOST, Float.toString(boost));
		xmlWriter.endElement();

	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(field);
		sb.append(" - ");
		sb.append(phrase);
		sb.append(" - ");
		sb.append(boost);
		return sb.toString();
	}

	public void addQuery(PerFieldAnalyzerWrapper analyzer, String queryString,
			BooleanQuery complexQuery, int phraseSlop, Occur occur)
			throws IOException {
		TokenStream ts = analyzer.tokenStream(field, new StringReader(
				queryString));
		TokenQueryFilter.PhraseQueryFilter tfp = null;
		TokenQueryFilter.BooleanQueryFilter tft = new TokenQueryFilter.BooleanQueryFilter(
				new BooleanQuery(), occur, field, boost, ts);
		ts = tft;
		if (phrase) {
			tfp = new TokenQueryFilter.PhraseQueryFilter(new PhraseQuery(),
					field, boost, ts);
			tfp.query.setBoost(boost);
			tfp.query.setSlop(phraseSlop);
			ts = tfp;
		}
		while (ts.incrementToken())
			;
		ts.end();
		ts.close();
		complexQuery.add(tft.query, Occur.SHOULD);
		if (tfp != null) {
			BooleanQuery bpQuery = new BooleanQuery();
			bpQuery.add(tfp.query, Occur.MUST);
			complexQuery.add(bpQuery, Occur.SHOULD);
		}
	}
}
