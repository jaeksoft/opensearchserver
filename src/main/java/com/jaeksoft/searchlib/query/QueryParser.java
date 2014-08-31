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
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
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
import com.jaeksoft.searchlib.util.StringUtils;

public class QueryParser extends BooleanQueryBaseListener {

	private final String field;
	private final int defaultOperator;
	private final CompiledAnalyzer analyzer;
	private final int phraseSlop;
	private final Double termBoost;
	private final Double phraseBoost;

	private int currentOperator;
	private Query holdQuery;
	private BooleanQuery booleanQuery;

	private IOException ioError;

	public QueryParser(final String field, final Occur occur,
			final CompiledAnalyzer analyzer, final int phraseSlop,
			final Double termBoost, final Double phraseBoost) {
		this.field = field;
		this.defaultOperator = getOperator(occur);
		this.analyzer = analyzer;
		this.phraseSlop = phraseSlop;
		this.termBoost = termBoost;
		this.phraseBoost = phraseBoost;
	}

	final private static int getOperator(final Occur occur) {
		if (occur == null)
			return BooleanQueryLexer.AND;
		switch (occur) {
		default:
		case MUST:
			return BooleanQueryLexer.AND;
		case MUST_NOT:
			return BooleanQueryLexer.NOT;
		case SHOULD:
			return BooleanQueryLexer.OR;
		}
	}

	final private void addBooleanClause(final Query query, final int operator) {
		Occur occur = null;
		switch (operator) {
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
	}

	final private void addBooleanClause(final Query query) {
		if (currentOperator == -1) {
			holdQuery = query;
		} else {
			if (holdQuery != null) {
				addBooleanClause(
						holdQuery,
						currentOperator == BooleanQueryLexer.NOT ? defaultOperator
								: currentOperator);
				holdQuery = null;
			}
			addBooleanClause(query, currentOperator);
		}
		currentOperator = defaultOperator;
	}

	final private List<String> getWords(String text) throws IOException {
		List<String> words = new ArrayList<String>(1);
		if (analyzer != null)
			analyzer.extractTerms(text, words);
		else
			words.add(text);
		return words;
	}

	final private void addTermQuery(String text) throws IOException {
		for (String word : getWords(text)) {
			Term term = new Term(field, word);
			TermQuery termQuery = new TermQuery(term);
			if (termBoost != null)
				termQuery.setBoost(termBoost.floatValue());
			addBooleanClause(termQuery);
		}
	}

	private void addPhraseQuery(String text) throws IOException {
		int s = 0;
		if (text.startsWith("\""))
			s = 1;
		int l = text.length() + 1 - s;
		if (text.endsWith("\""))
			l--;
		text = text.substring(s, l);
		PhraseQuery phraseQuery = new PhraseQuery();
		phraseQuery.setSlop(phraseSlop);
		if (phraseBoost != null)
			phraseQuery.setBoost(phraseBoost.floatValue());
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
				break;
			}
		} catch (IOException e) {
			ioError = e;
		}
	}

	private final class ErrorListener extends BaseErrorListener {

		@Override
		public void syntaxError(Recognizer<?, ?> recognizer,
				Object offendingSymbol, int line, int charPositionInLine,
				String msg, RecognitionException e) {
			ioError = new IOException(StringUtils.fastConcat("line: ",
					Integer.toString(line), " - pos: ",
					Integer.toString(charPositionInLine), " - ", msg));
		}
	}

	public final Query parse(String query) throws IOException {
		try {
			currentOperator = -1;
			holdQuery = null;
			booleanQuery = new BooleanQuery();
			ioError = null;
			ANTLRInputStream input = new ANTLRInputStream(query);
			BooleanQueryLexer lexer = new BooleanQueryLexer(input);
			ErrorListener errorListener = new ErrorListener();
			lexer.removeErrorListeners();
			lexer.addErrorListener(errorListener);
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			BooleanQueryParser parser = new BooleanQueryParser(tokens);
			BailErrorStrategy errorHandler = new BailErrorStrategy();
			parser.setErrorHandler(errorHandler);
			parser.addParseListener(this);
			parser.removeErrorListeners();
			parser.addErrorListener(errorListener);
			parser.expression();
			if (ioError != null)
				throw ioError;
			if (holdQuery != null)
				addBooleanClause(holdQuery, currentOperator);
			return booleanQuery;
		} catch (org.antlr.v4.runtime.RecognitionException e) {
			if (ioError != null)
				throw ioError;
			throw new IOException(e);
		} catch (org.antlr.v4.runtime.misc.ParseCancellationException e) {
			if (ioError != null)
				throw ioError;
			throw new IOException(e);
		}
	}

	public final static void main(String[] arvs) throws IOException {
		QueryParser queryParser = new QueryParser("field", Occur.MUST, null, 1,
				null, null);
		System.out.println(queryParser.parse("word"));
		System.out.println(queryParser.parse("\"quoted_word\""));
		System.out.println(queryParser.parse("\"quoted_word\" word"));
		System.out.println(queryParser.parse("word OR \"quoted_word\""));
		System.out
				.println(queryParser.parse("word1 word2 AND \"quoted_word\""));
		System.out.println(queryParser
				.parse("word1 OU word2 \"quoted_word\" NON unwanted"));
		System.out.println(queryParser.parse("\"\""));
		System.out.println(queryParser.parse("\"non ending quote"));
		System.out.println(queryParser.parse("22\""));
		System.out.println(queryParser.parse("\""));
		System.out.println(queryParser.parse("OU OU"));
	}
}
