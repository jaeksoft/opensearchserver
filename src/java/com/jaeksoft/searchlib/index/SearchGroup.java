/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft SearchLib Community.
 *
 * Jaeksoft SearchLib Community is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft SearchLib Community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft SearchLib Community. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.index;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.queryParser.ParseException;

import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.request.Request;
import com.jaeksoft.searchlib.result.ResultGroup;

public class SearchGroup {

	private IndexGroup indexGroup;

	protected SearchGroup(IndexGroup indexGroup) {
		this.indexGroup = indexGroup;
	}

	private int search(ResultGroup resultGroup,
			ArrayList<SearchThread> searchsThread, int step, int end)
			throws IOException, ParseException, SyntaxError {
		for (SearchThread searchThread : searchsThread)
			searchThread.search(step, resultGroup.getScoreGoal());
		int fetchCount = 0;
		for (SearchThread searchThread : searchsThread) {
			searchThread.waitForCompletion();
			searchThread.exception();
			fetchCount += searchThread.getFetchCount();
		}
		resultGroup.collapse();
		return fetchCount;
	}

	protected ResultGroup search(Request request) throws IOException,
			ParseException, SyntaxError {
		ResultGroup resultGroup = new ResultGroup(request);
		ArrayList<SearchThread> searchsThread = new ArrayList<SearchThread>(
				indexGroup.size());

		int end = request.getEnd();

		Request singleRequest = request.clone();
		singleRequest.setCollapseActive(false);

		for (IndexAbstract index : indexGroup.getIndices())
			searchsThread.add(new SearchThread(index, singleRequest,
					resultGroup));

		int step = end / searchsThread.size() + 1;
		int nextStep = request.getRows() / searchsThread.size() + 1;

		while (search(resultGroup, searchsThread, step, end) > 0)
			step = nextStep;

		return resultGroup;
	}

}
