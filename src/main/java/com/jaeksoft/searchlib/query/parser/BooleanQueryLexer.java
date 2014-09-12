// Generated from com/jaeksoft/searchlib/query/parser/BooleanQuery.g4 by ANTLR 4.3
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
	static { RuntimeMetaData.checkVersion("4.3", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		AND=1, OR=2, NOT=3, STRING=4, QSTRING=5, WS=6;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] tokenNames = {
		"'\\u0000'", "'\\u0001'", "'\\u0002'", "'\\u0003'", "'\\u0004'", "'\\u0005'", 
		"'\\u0006'"
	};
	public static final String[] ruleNames = {
		"AND", "OR", "NOT", "STRING", "QSTRING", "WS", "StringElement", "WhiteSpaces", 
		"CharEscapeSeq"
	};


	public BooleanQueryLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "BooleanQuery.g4"; }

	@Override
	public String[] getTokenNames() { return tokenNames; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2\bW\b\1\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\3\2\3\2"+
		"\3\2\3\2\3\2\3\2\3\2\3\2\5\2\36\n\2\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\5"+
		"\3(\n\3\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\5\4\66\n\4\3\5"+
		"\6\59\n\5\r\5\16\5:\3\5\5\5>\n\5\3\6\3\6\3\6\7\6C\n\6\f\6\16\6F\13\6\3"+
		"\6\5\6I\n\6\3\7\3\7\3\7\3\7\3\b\3\b\5\bQ\n\b\3\t\3\t\3\n\3\n\3\n\2\2\13"+
		"\3\3\5\4\7\5\t\6\13\7\r\b\17\2\21\2\23\2\3\2\6\3\3$$\4\2##%\1\5\2\13\f"+
		"\17\17\"\"\n\2$$))^^ddhhppttvv^\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2"+
		"\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\3\35\3\2\2\2\5\'\3\2\2\2\7\65\3\2"+
		"\2\2\t8\3\2\2\2\13?\3\2\2\2\rJ\3\2\2\2\17P\3\2\2\2\21R\3\2\2\2\23T\3\2"+
		"\2\2\25\26\7C\2\2\26\27\7P\2\2\27\36\7F\2\2\30\31\7G\2\2\31\36\7V\2\2"+
		"\32\33\7W\2\2\33\34\7P\2\2\34\36\7F\2\2\35\25\3\2\2\2\35\30\3\2\2\2\35"+
		"\32\3\2\2\2\36\4\3\2\2\2\37 \7Q\2\2 (\7T\2\2!\"\7Q\2\2\"(\7W\2\2#$\7Q"+
		"\2\2$%\7F\2\2%&\7G\2\2&(\7T\2\2\'\37\3\2\2\2\'!\3\2\2\2\'#\3\2\2\2(\6"+
		"\3\2\2\2)*\7P\2\2*+\7Q\2\2+\66\7V\2\2,-\7P\2\2-.\7Q\2\2.\66\7P\2\2/\60"+
		"\7P\2\2\60\61\7K\2\2\61\62\7E\2\2\62\63\7J\2\2\63\64\7V\2\2\64\66\7U\2"+
		"\2\65)\3\2\2\2\65,\3\2\2\2\65/\3\2\2\2\66\b\3\2\2\2\679\5\17\b\28\67\3"+
		"\2\2\29:\3\2\2\2:8\3\2\2\2:;\3\2\2\2;=\3\2\2\2<>\7\2\2\3=<\3\2\2\2=>\3"+
		"\2\2\2>\n\3\2\2\2?D\7$\2\2@C\5\17\b\2AC\5\21\t\2B@\3\2\2\2BA\3\2\2\2C"+
		"F\3\2\2\2DB\3\2\2\2DE\3\2\2\2EH\3\2\2\2FD\3\2\2\2GI\t\2\2\2HG\3\2\2\2"+
		"I\f\3\2\2\2JK\5\21\t\2KL\3\2\2\2LM\b\7\2\2M\16\3\2\2\2NQ\t\3\2\2OQ\5\23"+
		"\n\2PN\3\2\2\2PO\3\2\2\2Q\20\3\2\2\2RS\t\4\2\2S\22\3\2\2\2TU\7^\2\2UV"+
		"\t\5\2\2V\24\3\2\2\2\f\2\35\'\65:=BDHP\3\2\3\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}