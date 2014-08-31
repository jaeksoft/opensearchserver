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
		"AND", "OR", "NOT", "STRING", "QSTRING", "WS", "StringCharacter", "EscapeSequence"
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
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2\bT\b\1\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\3\2\3\2\3\2\3\2"+
		"\3\2\3\2\3\2\3\2\5\2\34\n\2\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\5\3&\n\3\3"+
		"\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\5\4\64\n\4\3\5\6\5\67\n"+
		"\5\r\5\16\58\3\5\5\5<\n\5\3\6\3\6\6\6@\n\6\r\6\16\6A\3\6\5\6E\n\6\3\7"+
		"\6\7H\n\7\r\7\16\7I\3\7\3\7\3\b\3\b\5\bP\n\b\3\t\3\t\3\t\2\2\n\3\3\5\4"+
		"\7\5\t\6\13\7\r\b\17\2\21\2\3\2\6\3\3$$\5\2\13\f\17\17\"\"\6\2\13\f\17"+
		"\17\"\"^^\n\2$$))^^ddhhppttvv\\\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2"+
		"\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\3\33\3\2\2\2\5%\3\2\2\2\7\63\3\2\2"+
		"\2\t\66\3\2\2\2\13=\3\2\2\2\rG\3\2\2\2\17O\3\2\2\2\21Q\3\2\2\2\23\24\7"+
		"C\2\2\24\25\7P\2\2\25\34\7F\2\2\26\27\7G\2\2\27\34\7V\2\2\30\31\7W\2\2"+
		"\31\32\7P\2\2\32\34\7F\2\2\33\23\3\2\2\2\33\26\3\2\2\2\33\30\3\2\2\2\34"+
		"\4\3\2\2\2\35\36\7Q\2\2\36&\7T\2\2\37 \7Q\2\2 &\7W\2\2!\"\7Q\2\2\"#\7"+
		"F\2\2#$\7G\2\2$&\7T\2\2%\35\3\2\2\2%\37\3\2\2\2%!\3\2\2\2&\6\3\2\2\2\'"+
		"(\7P\2\2()\7Q\2\2)\64\7V\2\2*+\7P\2\2+,\7Q\2\2,\64\7P\2\2-.\7P\2\2./\7"+
		"K\2\2/\60\7E\2\2\60\61\7J\2\2\61\62\7V\2\2\62\64\7U\2\2\63\'\3\2\2\2\63"+
		"*\3\2\2\2\63-\3\2\2\2\64\b\3\2\2\2\65\67\5\17\b\2\66\65\3\2\2\2\678\3"+
		"\2\2\28\66\3\2\2\289\3\2\2\29;\3\2\2\2:<\7\2\2\3;:\3\2\2\2;<\3\2\2\2<"+
		"\n\3\2\2\2=?\7$\2\2>@\5\17\b\2?>\3\2\2\2@A\3\2\2\2A?\3\2\2\2AB\3\2\2\2"+
		"BD\3\2\2\2CE\t\2\2\2DC\3\2\2\2E\f\3\2\2\2FH\t\3\2\2GF\3\2\2\2HI\3\2\2"+
		"\2IG\3\2\2\2IJ\3\2\2\2JK\3\2\2\2KL\b\7\2\2L\16\3\2\2\2MP\n\4\2\2NP\5\21"+
		"\t\2OM\3\2\2\2ON\3\2\2\2P\20\3\2\2\2QR\7^\2\2RS\t\5\2\2S\22\3\2\2\2\f"+
		"\2\33%\638;ADIO\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}