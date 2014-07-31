/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013-2014 Emmanuel Keller / Jaeksoft
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
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.analysis.CompiledAnalyzer;
import com.jaeksoft.searchlib.analysis.PerFieldAnalyzer;
import com.jaeksoft.searchlib.analysis.TokenQueryFilter;
import com.jaeksoft.searchlib.analysis.TokenQueryFilter.TermQueryFilter;
import com.jaeksoft.searchlib.analysis.TokenQueryFilter.TermQueryItem;
import com.jaeksoft.searchlib.query.QueryParser;
import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.IOUtils;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.jaeksoft.searchlib.webservice.query.search.SearchFieldQuery.SearchField.Mode;

public class SearchField implements Cloneable {

	public final static String SEARCHFIELD_NODE_NAME = "searchField";
	public final static String SEARCHFIELD_ATTRIBUTE_FIELD_NAME = "field";
	public final static String SEARCHFIELD_ATTRIBUTE_MODE = "mode";
	public final static String SEARCHFIELD_ATTRIBUTE_PHRASE = "phrase";
	public final static String SEARCHFIELD_ATTRIBUTE_TERM_BOOST = "boost";
	public final static String SEARCHFIELD_ATTRIBUTE_PHRASE_BOOST = "phraseBoost";
	public final static String SEARCHFIELD_ATTRIBUTE_PHRASE_SLOP = "phraseSlop";

	private String field;
	private Mode mode;
	private double termBoost;
	private double phraseBoost;
	private Integer phraseSlop;

	private SearchField(SearchField searchField) {
		this.field = searchField.field;
		this.mode = searchField.mode;
		this.termBoost = searchField.termBoost;
		this.phraseBoost = searchField.phraseBoost;
		this.phraseSlop = searchField.phraseSlop;
	}

	public SearchField(String field, Mode mode, Double termBoost,
			Double phraseBoost, Integer phraseSlop) {
		this.field = field;
		this.mode = mode == null ? Mode.TERM : mode;
		this.termBoost = termBoost == null ? 1.0F : termBoost;
		this.phraseBoost = phraseBoost == null ? this.termBoost : phraseBoost;
		this.phraseSlop = phraseSlop;
	}

	public SearchField(Node fieldNode) {
		this.field = DomUtils.getAttributeText(fieldNode,
				SEARCHFIELD_ATTRIBUTE_FIELD_NAME);
		String modeAttr = DomUtils.getAttributeText(fieldNode,
				SEARCHFIELD_ATTRIBUTE_MODE);
		if (modeAttr == null) {
			boolean phrase = Boolean.parseBoolean(DomUtils.getAttributeText(
					fieldNode, SEARCHFIELD_ATTRIBUTE_PHRASE));
			mode = phrase ? Mode.TERM_AND_PHRASE : Mode.TERM;
		} else
			mode = Mode.find(modeAttr);
		this.termBoost = DomUtils.getAttributeDouble(fieldNode,
				SEARCHFIELD_ATTRIBUTE_TERM_BOOST, 1.0);
		this.phraseBoost = DomUtils.getAttributeDouble(fieldNode,
				SEARCHFIELD_ATTRIBUTE_PHRASE_BOOST, this.termBoost);
		this.phraseSlop = DomUtils.getAttributeInteger(fieldNode,
				SEARCHFIELD_ATTRIBUTE_PHRASE_SLOP, this.phraseSlop);
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
	 * @return the mode
	 */
	public Mode getMode() {
		return mode;
	}

	/**
	 * @param mode
	 *            the mode to set
	 */
	public void setMode(Mode mode) {
		this.mode = mode;
	}

	/**
	 * @return the termBoost
	 */
	public double getTermBoost() {
		return termBoost;
	}

	/**
	 * @param termBoost
	 *            the termBoost to set
	 */
	public void setTermBoost(double termBoost) {
		this.termBoost = termBoost;
	}

	/**
	 * @return the phrase boost
	 */
	public double getPhraseBoost() {
		return phraseBoost;
	}

	/**
	 * @param phrase
	 *            boost the phrase boost to set
	 */
	public void setPhraseBoost(double phraseBoost) {
		this.phraseBoost = phraseBoost;
	}

	/**
	 * @return the phrase slop
	 */
	public Integer getPhraseSlop() {
		return phraseSlop;
	}

	/**
	 * @param phrase
	 *            slop the phrase slop to set
	 */
	public void setPhraseSlop(Integer phraseSlop) {
		this.phraseSlop = phraseSlop;
	}

	public void writeXmlConfig(XmlWriter xmlWriter) throws SAXException {
		xmlWriter.startElement(SEARCHFIELD_NODE_NAME,
				SEARCHFIELD_ATTRIBUTE_FIELD_NAME, field,
				SEARCHFIELD_ATTRIBUTE_MODE, mode == null ? null : mode.name(),
				SEARCHFIELD_ATTRIBUTE_TERM_BOOST, Double.toString(termBoost),
				SEARCHFIELD_ATTRIBUTE_PHRASE_BOOST, Double
						.toString(phraseBoost),
				SEARCHFIELD_ATTRIBUTE_PHRASE_SLOP, phraseSlop == null ? null
						: Integer.toString(phraseSlop));
		xmlWriter.endElement();

	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(field);
		sb.append(" - ");
		sb.append(mode.getLabel());
		sb.append(" - ");
		sb.append(termBoost);
		sb.append("/");
		sb.append(phraseBoost);
		if (phraseSlop != null) {
			sb.append("/");
			sb.append(phraseSlop);
		}
		return sb.toString();
	}

	final private Query getPatternQuery(final CompiledAnalyzer analyzer,
			final Occur occur, final int phraseSlop, final String queryString)
			throws IOException {
		QueryParser queryParser = new QueryParser(field, occur, analyzer,
				this.phraseSlop != null ? this.phraseSlop : phraseSlop,
				termBoost, phraseBoost);
		return queryParser.parse(queryString);
	}

	final private List<TermQueryItem> getTermQueryFilter(
			final PerFieldAnalyzer perFieldAnalyzer,
			CompiledAnalyzer compiledAnalyzer, final String queryString)
			throws IOException {
		TokenStream ts = null;
		TokenQueryFilter.TermQueryFilter tqf = null;
		Analyzer analyzer = compiledAnalyzer != null ? compiledAnalyzer
				: perFieldAnalyzer.getKeywordAnalyzer();
		try {
			ts = analyzer.tokenStream(field, new StringReader(queryString));
			tqf = new TermQueryFilter(compiledAnalyzer, field,
					(float) termBoost, ts);
			while (tqf.incrementToken())
				;
			ts.end();
			ts.close();

			tqf.sortByOffset();

			TermQueryFilter.includeChildrenBrothers(tqf.termQueryItems);
			for (TermQueryItem termQueryItem : tqf.termQueryItems)
				termQueryItem.includeChildrenBrothers();
			return tqf.termQueryItems;
		} finally {
			IOUtils.close(tqf, ts, analyzer);
		}
	}

	final private Query getTermQuery(final List<TermQueryItem> termQueryItems,
			final Occur occur) throws IOException {
		BooleanQuery booleanQuery = new BooleanQuery();
		for (TermQueryItem termQueryItem : termQueryItems)
			if (termQueryItem.parent == null)
				booleanQuery.add(termQueryItem.getQuery(null, occur), occur);
		return booleanQuery;
	}

	final private Query getPhraseQuery(
			final List<TermQueryItem> termQueryItems, final int phraseSlop,
			final Occur occur) throws IOException {
		PhraseQuery phraseQuery = new PhraseQuery();
		for (TermQueryItem termQueryItem : termQueryItems)
			if (termQueryItem.children == null)
				phraseQuery.add(new Term(field, termQueryItem.term));
		phraseQuery.setBoost((float) phraseBoost);
		phraseQuery.setSlop(this.phraseSlop != null ? this.phraseSlop
				: phraseSlop);
		return phraseQuery;
	}

	final public void addQuery(PerFieldAnalyzer perFieldAnalyzer,
			String queryString, BooleanQuery complexQuery, int phraseSlop,
			Occur occur) throws IOException {
		CompiledAnalyzer compiledAnalyzer = null;
		try {
			if (StringUtils.isEmpty(queryString))
				return;
			compiledAnalyzer = perFieldAnalyzer.getCompiledAnalyzer(field);

			if (mode == Mode.PATTERN) {
				complexQuery.add(
						getPatternQuery(compiledAnalyzer, occur, phraseSlop,
								queryString), Occur.SHOULD);
				return;
			}
			List<TermQueryItem> termQueryItems = getTermQueryFilter(
					perFieldAnalyzer, compiledAnalyzer, queryString);
			switch (mode) {
			case TERM:
				complexQuery.add(getTermQuery(termQueryItems, occur),
						Occur.SHOULD);
				break;
			case PHRASE:
				complexQuery.add(
						getPhraseQuery(termQueryItems, phraseSlop, occur),
						Occur.SHOULD);
				break;
			case TERM_AND_PHRASE:
				complexQuery.add(getTermQuery(termQueryItems, occur),
						Occur.SHOULD);
				complexQuery.add(
						getPhraseQuery(termQueryItems, phraseSlop, occur),
						Occur.SHOULD);
				break;
			default:
				break;
			}
		} finally {
			IOUtils.close(compiledAnalyzer);
		}
	}
}
