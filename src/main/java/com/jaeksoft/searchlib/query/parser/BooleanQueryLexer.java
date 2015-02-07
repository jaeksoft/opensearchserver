// Generated from com/jaeksoft/searchlib/query/parser/BooleanQuery.g4 by ANTLR 4.5
package com.jaeksoft.searchlib.query.parser;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class BooleanQueryLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.5", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		AND=1, OR=2, NOT=3, STRING=4, QSTRING=5, FIELD=6, WS=7;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"AND", "OR", "NOT", "STRING", "QSTRING", "FIELD", "WS", "KeywordElement", 
		"StringElement", "QStringElement", "WhiteSpaces", "CharEscapeSeq"
	};

	private static final String[] _LITERAL_NAMES = {
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, "AND", "OR", "NOT", "STRING", "QSTRING", "FIELD", "WS"
	};
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public BooleanQueryLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "BooleanQuery.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2\tj\b\1\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\5\2$\n\2\3\3\3\3\3"+
		"\3\3\3\3\3\3\3\3\3\3\3\5\3.\n\3\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3"+
		"\4\3\4\3\4\5\4<\n\4\3\5\6\5?\n\5\r\5\16\5@\3\5\5\5D\n\5\3\6\3\6\3\6\7"+
		"\6I\n\6\f\6\16\6L\13\6\3\6\5\6O\n\6\3\7\6\7R\n\7\r\7\16\7S\3\7\3\7\3\b"+
		"\3\b\3\b\3\b\3\t\3\t\3\n\3\n\5\n`\n\n\3\13\3\13\5\13d\n\13\3\f\3\f\3\r"+
		"\3\r\3\r\2\2\16\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\2\23\2\25\2\27\2\31\2"+
		"\3\2\b\3\3$$\6\2\62;C\\aac|\5\2##%;=\1\4\2##%\1\5\2\13\f\17\17\"\"\13"+
		"\2$$))<<^^ddhhppttvvq\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2"+
		"\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\3#\3\2\2\2\5-\3\2\2\2\7;\3\2\2"+
		"\2\t>\3\2\2\2\13E\3\2\2\2\rQ\3\2\2\2\17W\3\2\2\2\21[\3\2\2\2\23_\3\2\2"+
		"\2\25c\3\2\2\2\27e\3\2\2\2\31g\3\2\2\2\33\34\7C\2\2\34\35\7P\2\2\35$\7"+
		"F\2\2\36\37\7G\2\2\37$\7V\2\2 !\7W\2\2!\"\7P\2\2\"$\7F\2\2#\33\3\2\2\2"+
		"#\36\3\2\2\2# \3\2\2\2$\4\3\2\2\2%&\7Q\2\2&.\7T\2\2\'(\7Q\2\2(.\7W\2\2"+
		")*\7Q\2\2*+\7F\2\2+,\7G\2\2,.\7T\2\2-%\3\2\2\2-\'\3\2\2\2-)\3\2\2\2.\6"+
		"\3\2\2\2/\60\7P\2\2\60\61\7Q\2\2\61<\7V\2\2\62\63\7P\2\2\63\64\7Q\2\2"+
		"\64<\7P\2\2\65\66\7P\2\2\66\67\7K\2\2\678\7E\2\289\7J\2\29:\7V\2\2:<\7"+
		"U\2\2;/\3\2\2\2;\62\3\2\2\2;\65\3\2\2\2<\b\3\2\2\2=?\5\23\n\2>=\3\2\2"+
		"\2?@\3\2\2\2@>\3\2\2\2@A\3\2\2\2AC\3\2\2\2BD\7\2\2\3CB\3\2\2\2CD\3\2\2"+
		"\2D\n\3\2\2\2EJ\7$\2\2FI\5\25\13\2GI\5\27\f\2HF\3\2\2\2HG\3\2\2\2IL\3"+
		"\2\2\2JH\3\2\2\2JK\3\2\2\2KN\3\2\2\2LJ\3\2\2\2MO\t\2\2\2NM\3\2\2\2O\f"+
		"\3\2\2\2PR\5\21\t\2QP\3\2\2\2RS\3\2\2\2SQ\3\2\2\2ST\3\2\2\2TU\3\2\2\2"+
		"UV\7<\2\2V\16\3\2\2\2WX\5\27\f\2XY\3\2\2\2YZ\b\b\2\2Z\20\3\2\2\2[\\\t"+
		"\3\2\2\\\22\3\2\2\2]`\t\4\2\2^`\5\31\r\2_]\3\2\2\2_^\3\2\2\2`\24\3\2\2"+
		"\2ad\t\5\2\2bd\5\31\r\2ca\3\2\2\2cb\3\2\2\2d\26\3\2\2\2ef\t\6\2\2f\30"+
		"\3\2\2\2gh\7^\2\2hi\t\7\2\2i\32\3\2\2\2\16\2#-;@CHJNS_c\3\2\3\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}