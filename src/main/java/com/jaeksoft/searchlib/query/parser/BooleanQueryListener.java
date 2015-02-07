// Generated from com/jaeksoft/searchlib/query/parser/BooleanQuery.g4 by ANTLR 4.5
package com.jaeksoft.searchlib.query.parser;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link BooleanQueryParser}.
 */
public interface BooleanQueryListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link BooleanQueryParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(BooleanQueryParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BooleanQueryParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(BooleanQueryParser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BooleanQueryParser#term}.
	 * @param ctx the parse tree
	 */
	void enterTerm(BooleanQueryParser.TermContext ctx);
	/**
	 * Exit a parse tree produced by {@link BooleanQueryParser#term}.
	 * @param ctx the parse tree
	 */
	void exitTerm(BooleanQueryParser.TermContext ctx);
}