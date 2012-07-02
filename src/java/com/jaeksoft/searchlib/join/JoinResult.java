/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.join;

import java.io.IOException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.DocumentsRequest;
import com.jaeksoft.searchlib.result.AbstractResultSearch;
import com.jaeksoft.searchlib.result.ResultDocument;
import com.jaeksoft.searchlib.result.ResultScoreDoc;
import com.jaeksoft.searchlib.result.ResultSearchSingle;

public class JoinResult {

	public static final JoinResult[] EMPTY_ARRAY = new JoinResult[0];

	public final int pos;

	private final String paramPosition;

	private ResultDocument[] resultDocument;

	private transient ResultSearchSingle foreignResult;

	public JoinResult(int pos, String paramPosition) {
		this.pos = pos;
		this.paramPosition = paramPosition;
		resultDocument = ResultDocument.EMPTY_ARRAY;
	}

	public String getParamPosition() {
		return paramPosition;
	}

	public void setForeignResult(ResultSearchSingle foreignResult) {
		this.foreignResult = foreignResult;
	}

	public AbstractResultSearch getForeignResult() {
		return foreignResult;
	}

	private void getDocuments(int start, int rows, ResultScoreDoc[] docs)
			throws SearchLibException, ParseException, SyntaxError, IOException {
		if (rows <= 0)
			return;
		resultDocument = foreignResult.getReader().documents(
				new DocumentsRequest(this, start, rows, docs));
	}

	public ResultDocument[] getDocuments() {
		return resultDocument;
	}

	public final static void getDocuments(AbstractResultSearch result,
			JoinResult[] joinResults) throws SearchLibException,
			ParseException, SyntaxError, IOException {
		int start = result.getRequest().getStart();
		int rows = result.getDocumentCount();
		if (rows <= 0)
			return;
		ResultScoreDoc[] docs = result.getDocs();
		for (JoinResult joinResult : joinResults)
			joinResult.getDocuments(start, rows, docs);
	}

}
