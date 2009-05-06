/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
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

package com.jaeksoft.searchlib.function.expression;

import java.util.ArrayList;

import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.function.ValueSourceQuery;

public class RootExpression extends GroupExpression {

	protected ArrayList<FunctionValueSource> functionValueSources;

	protected RootExpression(char[] chars, int pos) throws SyntaxError {
		super(null, chars, pos);
	}

	public FunctionValueSource functionValueSource(String func, String field)
			throws SyntaxError {
		if (functionValueSources == null)
			functionValueSources = new ArrayList<FunctionValueSource>();
		FunctionValueSource fvs = new FunctionValueSource(func, field,
				functionValueSources.size());
		for (FunctionValueSource functionValueSource : functionValueSources)
			if (functionValueSource.equals(fvs))
				return functionValueSource;
		functionValueSources.add(fvs);
		return fvs;
	}

	private ValueSourceQuery getValueSourceQuery() {
		return new ValueSourceQuery(functionValueSources.get(0).valueSource);
	}

	private ValueSourceQuery[] getValueSourceQueries() {
		ValueSourceQuery[] vsq = new ValueSourceQuery[functionValueSources
				.size()];
		int i = 0;
		for (FunctionValueSource fvs : functionValueSources)
			vsq[i++] = new ValueSourceQuery(fvs.valueSource);
		return vsq;
	}

	protected ScoreFunctionQuery getQuery(Query subQuery) throws SyntaxError {
		if (functionValueSources == null)
			return new ScoreFunctionQuery(subQuery, this);
		switch (functionValueSources.size()) {
		case 0:
			return new ScoreFunctionQuery(subQuery, this);
		case 1:
			return new ScoreFunctionQuery(subQuery, getValueSourceQuery(), this);
		default:
			return new ScoreFunctionQuery(subQuery, getValueSourceQueries(),
					this);
		}

	}

	static public ScoreFunctionQuery getQuery(Query subQuery, String exp)
			throws SyntaxError {
		exp = exp.trim().replaceAll("\\s+", "");
		return new RootExpression(exp.toCharArray(), 0).getQuery(subQuery);
	}

	public static void main(String[] argv) {
		String exp = " 10000 / ( 1 * rord(creationDate) + 10000 ) ";
		try {
			System.out.println(getQuery(new BooleanQuery(), exp));
		} catch (SyntaxError e) {
			System.err.println(e.getMessage());
		}
	}

}
