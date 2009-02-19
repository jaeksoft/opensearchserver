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
import com.jaeksoft.searchlib.index.ReaderInterface;
import com.jaeksoft.searchlib.result.Result;
import com.jaeksoft.searchlib.result.ResultGroup;

public class SearchThread extends AbstractGroupRequestThread {

	private ResultGroup resultGroup;
	private ReaderInterface reader;
	private SearchRequest searchRequest;
	private boolean done;
	private int step;

	public SearchThread(IndexAbstract index, ResultGroup resultGroup, int step) {
		reader = index;
		this.resultGroup = resultGroup;
		this.done = false;
		this.step = step;

		searchRequest = new SearchRequest(resultGroup.getSearchRequest());
		searchRequest.setCollapseActive(false);
		searchRequest.setIndexName(index.getName());
		searchRequest.setWithDocument(false);
		searchRequest.setWithSortValues(true);
		searchRequest.setStart(0);
		searchRequest.setRows(0);

	}

	public void runner() throws IOException, URISyntaxException,
			ParseException, SyntaxError, ClassNotFoundException {
		searchRequest.setStart(searchRequest.getEnd());
		searchRequest.setRows(step);
		Result result = reader.search(searchRequest);

		done = true;

		if (searchRequest.getStart() == 0)
			resultGroup.addResult(result);

		if (result.getDocumentCount() <= 0)
			return;

		if (resultGroup.isThresholdReach(result.getDocs()[searchRequest
				.getStart()]))
			return;
		resultGroup.populate(reader.search(searchRequest));
		done = false;
	}

	public void setStep(int step) {
		this.step = step;
	}

	public boolean done() {
		return done;
	}

}
