// Generated from com/jaeksoft/searchlib/query/parser/BooleanQuery.g4 by ANTLR 4.1
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
	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		AND=1, OR=2, NOT=3, STRING=4, QSTRING=5, WS=6;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] tokenNames = {
		"<INVALID>",
		"AND", "OR", "NOT", "STRING", "QSTRING", "WS"
	};
	public static final String[] ruleNames = {
		"AND", "OR", "NOT", "STRING", "QSTRING", "WS"
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
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	@Override
	public void action(RuleContext _localctx, int ruleIndex, int actionIndex) {
		switch (ruleIndex) {
		case 5: WS_action((RuleContext)_localctx, actionIndex); break;
		}
	}
	private void WS_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 0: skip();  break;
		}
	}

	public static final String _serializedATN =
		"\3\uacf5\uee8c\u4f5d\u8b0d\u4a45\u78bd\u1b2f\u3378\2\bH\b\1\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2"+
		"\5\2\30\n\2\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\5\3\"\n\3\3\4\3\4\3\4\3\4"+
		"\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\5\4\60\n\4\3\5\6\5\63\n\5\r\5\16\5\64"+
		"\3\6\3\6\3\6\3\6\7\6;\n\6\f\6\16\6>\13\6\3\6\3\6\3\7\6\7C\n\7\r\7\16\7"+
		"D\3\7\3\7\2\b\3\3\1\5\4\1\7\5\1\t\6\1\13\7\1\r\b\2\3\2\5\6\2\13\f\17\17"+
		"\"\"$$\3\2$$\5\2\13\f\17\17\"\"Q\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2"+
		"\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\3\27\3\2\2\2\5!\3\2\2\2\7/\3\2\2\2"+
		"\t\62\3\2\2\2\13\66\3\2\2\2\rB\3\2\2\2\17\20\7C\2\2\20\21\7P\2\2\21\30"+
		"\7F\2\2\22\23\7G\2\2\23\30\7V\2\2\24\25\7W\2\2\25\26\7P\2\2\26\30\7F\2"+
		"\2\27\17\3\2\2\2\27\22\3\2\2\2\27\24\3\2\2\2\30\4\3\2\2\2\31\32\7Q\2\2"+
		"\32\"\7T\2\2\33\34\7Q\2\2\34\"\7W\2\2\35\36\7Q\2\2\36\37\7F\2\2\37 \7"+
		"G\2\2 \"\7T\2\2!\31\3\2\2\2!\33\3\2\2\2!\35\3\2\2\2\"\6\3\2\2\2#$\7P\2"+
		"\2$%\7Q\2\2%\60\7V\2\2&\'\7P\2\2\'(\7Q\2\2(\60\7P\2\2)*\7P\2\2*+\7K\2"+
		"\2+,\7E\2\2,-\7J\2\2-.\7V\2\2.\60\7U\2\2/#\3\2\2\2/&\3\2\2\2/)\3\2\2\2"+
		"\60\b\3\2\2\2\61\63\n\2\2\2\62\61\3\2\2\2\63\64\3\2\2\2\64\62\3\2\2\2"+
		"\64\65\3\2\2\2\65\n\3\2\2\2\66<\7$\2\2\678\7$\2\28;\7$\2\29;\n\3\2\2:"+
		"\67\3\2\2\2:9\3\2\2\2;>\3\2\2\2<:\3\2\2\2<=\3\2\2\2=?\3\2\2\2><\3\2\2"+
		"\2?@\7$\2\2@\f\3\2\2\2AC\t\4\2\2BA\3\2\2\2CD\3\2\2\2DB\3\2\2\2DE\3\2\2"+
		"\2EF\3\2\2\2FG\b\7\2\2G\16\3\2\2\2\n\2\27!/\64:<D";
	public static final ATN _ATN =
		ATNSimulator.deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}