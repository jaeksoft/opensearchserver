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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.lucene.queryParser.ParseException;

import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.request.Request;
import com.jaeksoft.searchlib.result.ResultGroup;
import com.jaeksoft.searchlib.result.ResultScoreDoc;

public class SearchGroup {

	final private static Logger logger = Logger.getLogger(SearchGroup.class
			.getCanonicalName());

	private IndexGroup indexGroup;

	protected SearchGroup(IndexGroup indexGroup) {
		this.indexGroup = indexGroup;
	}

	private int searchNextStep(ResultGroup resultGroup,
			List<SearchThread> searchsThread, int step) throws IOException,
			URISyntaxException, ParseException, SyntaxError {
		for (SearchThread searchThread : searchsThread)
			searchThread.searchNextStep(step);
		int fetchCount = 0;
		for (SearchThread searchThread : searchsThread) {
			searchThread.waitForCompletion();
			searchThread.exception();
			fetchCount += searchThread.getFetchCount();
		}
		return fetchCount;
	}

	protected ResultGroup search(Request request) throws IOException,
			URISyntaxException, ParseException, SyntaxError {
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
		int iterationCounter = 0;
		while (searchNextStep(resultGroup, searchsThread, step) > 0) {
			iterationCounter++;
			ResultScoreDoc[] tempsDocs = resultGroup.getCollapsedDocs();
			resultGroup.setDocs(tempsDocs);
			if (tempsDocs.length >= end)
				break;
			step = nextStep;
			// Help working with large collapsed set
			nextStep *= 2;
		}
		if (logger.isLoggable(Level.INFO))
			logger
					.info("SearchGroup needs " + iterationCounter
							+ " iterations");
		return resultGroup;
	}
}
