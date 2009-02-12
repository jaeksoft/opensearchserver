/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.request;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.lucene.queryParser.ParseException;

import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.IndexAbstract;
import com.jaeksoft.searchlib.index.IndexGroup;
import com.jaeksoft.searchlib.result.ResultGroup;

public class SearchGroup extends AbstractGroupRequest<SearchThread> {

	private ResultGroup resultGroup;

	private int totalFetchCount;

	private int fetchGoal;

	private int step;

	private int nextStep;

	public SearchGroup(IndexGroup indexGroup, SearchRequest searchRequest)
			throws IOException, URISyntaxException, ParseException, SyntaxError {
		super(indexGroup);
		resultGroup = new ResultGroup(searchRequest);
		totalFetchCount = 0;
		fetchGoal = searchRequest.getEnd();
		step = fetchGoal / indexGroup.size() + 1;
		nextStep = searchRequest.getRows() / indexGroup.size() + 1;
		loop();
	}

	@Override
	protected SearchThread getNewThread(IndexAbstract index) {
		return new SearchThread(index, resultGroup, step);
	}

	@Override
	protected boolean done() throws IOException, URISyntaxException,
			ParseException, SyntaxError {
		resultGroup.setFinalDocs();
		if (totalFetchCount == 0)
			return true;
		if (resultGroup.getDocLength() >= fetchGoal)
			return true;
		totalFetchCount = 0;
		step = nextStep;
		// Help working with large collapsed set
		nextStep *= 2;
		return false;
	}

	@Override
	protected void complete(SearchThread thread) {
		totalFetchCount += thread.getFetchCount();
		thread.setStep(step);
	}

	public ResultGroup getResult() {
		return resultGroup;
	}
}
