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
package com.jaeksoft.searchlib.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import com.jaeksoft.searchlib.analysis.CompiledAnalyzer;
import com.jaeksoft.searchlib.query.parser.BooleanQueryBaseListener;
import com.jaeksoft.searchlib.query.parser.BooleanQueryLexer;
import com.jaeksoft.searchlib.query.parser.BooleanQueryParser;

public class QueryParser extends BooleanQueryBaseListener {

	public static enum Operator {

		AND(BooleanQueryLexer.AND), OR(BooleanQueryLexer.OR), NOT(
				BooleanQueryLexer.NOT);

		private final int lexerInt;

		private Operator(int v) {
			lexerInt = v;
		}
	}

	private final String field;
	private final Operator defaultOperator;
	private final CompiledAnalyzer analyzer;
	private final int phraseSlop;

	private int currentOperator;
	private BooleanQuery booleanQuery;

	private IOException ioError;

	public QueryParser(final String field, final Operator defaultOperator,
			final CompiledAnalyzer analyzer, final int phraseSlop) {
		this.field = field;
		this.defaultOperator = defaultOperator == null ? Operator.AND
				: defaultOperator;
		this.analyzer = analyzer;
		this.phraseSlop = phraseSlop;
	}

	private void addBooleanClause(Query query) {
		Occur occur = null;
		switch (currentOperator) {
		case BooleanQueryLexer.AND:
			occur = Occur.MUST;
			break;
		case BooleanQueryLexer.OR:
			occur = Occur.SHOULD;
			break;
		case BooleanQueryLexer.NOT:
			occur = Occur.MUST_NOT;
			break;
		}
		booleanQuery.add(new BooleanClause(query, occur));
		currentOperator = defaultOperator.lexerInt;
	}

	final private List<String> getWords(String text) throws IOException {
		List<String> words = new ArrayList<String>(1);
		if (analyzer != null)
			analyzer.extractTerms(text, words);
		else
			words.add(text);
		return words;
	}

	private void addTermQuery(String text) throws IOException {
		for (String word : getWords(text)) {
			Term term = new Term(field, word);
			TermQuery termQuery = new TermQuery(term);
			addBooleanClause(termQuery);
		}
	}

	private void addPhraseQuery(String text) throws IOException {
		text = text.substring(1, text.length() - 1);
		PhraseQuery phraseQuery = new PhraseQuery();
		phraseQuery.setSlop(phraseSlop);
		for (String word : getWords(text))
			phraseQuery.add(new Term(field, word));
		addBooleanClause(phraseQuery);
	}

	@Override
	final public void visitTerminal(final TerminalNode node) {
		try {
			int type = node.getSymbol().getType();
			switch (type) {
			case BooleanQueryLexer.AND:
			case BooleanQueryLexer.OR:
			case BooleanQueryLexer.NOT:
				currentOperator = type;
				break;
			case BooleanQueryLexer.QSTRING:
				addPhraseQuery(node.getText());
				break;
			case BooleanQueryLexer.STRING:
				addTermQuery(node.getText());
				break;
			default:
				System.out.println(type + " : " + node.getText());
				break;
			}
		} catch (IOException e) {
			ioError = e;
		}
	}

	final Query parse(String query) throws IOException {
		currentOperator = defaultOperator.lexerInt;
		booleanQuery = new BooleanQuery();
		ioError = null;
		ANTLRInputStream input = new ANTLRInputStream(query);
		BooleanQueryLexer lexer = new BooleanQueryLexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		BooleanQueryParser parser = new BooleanQueryParser(tokens);
		parser.addParseListener(this);
		parser.expression(defaultOperator.lexerInt);
		if (ioError != null)
			throw ioError;
		return booleanQuery;
	}

	public final static void main(String[] arvs) throws IOException {
		QueryParser queryParser = new QueryParser("defaultField", Operator.AND,
				null, 1);
		queryParser.parse("word");
		queryParser.parse("\"quoted_word\"");
		queryParser.parse("\"quoted_word\" word");
		queryParser.parse("word OR \"quoted_word\"");
		queryParser.parse("word1 word2 AND \"quoted_word\"");
		queryParser.parse("word1 OU word2 \"quoted_word\" NON unwanted");
		queryParser.parse("\"\"");
	}
}
