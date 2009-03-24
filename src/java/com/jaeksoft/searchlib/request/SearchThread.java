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
import com.jaeksoft.searchlib.util.Debug;

public class SearchThread extends AbstractGroupRequestThread {

	private ResultGroup resultGroup;
	private ReaderInterface reader;
	private SearchRequest searchRequest;
	private int newDocumentCount;
	private int step;

	public SearchThread(Debug debug, IndexAbstract index,
			ResultGroup resultGroup, int step) {
		reader = index;
		this.resultGroup = resultGroup;
		this.newDocumentCount = 0;
		this.step = step;

		searchRequest = new SearchRequest(resultGroup.getSearchRequest());
		searchRequest.setCollapseActive(false);
		searchRequest.setIndexName(index.getName());
		searchRequest.setWithDocument(false);
		searchRequest.setWithSortValues(true);
		searchRequest.setStart(0);
		searchRequest.setRows(0);

	}

	@Override
	public void runner() throws IOException, URISyntaxException,
			ParseException, SyntaxError, ClassNotFoundException,
			InterruptedException {
		searchRequest.setStart(searchRequest.getEnd());
		searchRequest.setRows(step);
		Result result = reader.search(searchRequest);

		Debug dbg = resultGroup.getDebug();
		if (dbg != null)
			dbg.addChildren(result.getDebug());

		if (searchRequest.getStart() == 0)
			resultGroup.addResult(result);

		newDocumentCount = 0;
		int docCount = result.getDocumentCount();
		if (docCount <= 0)
			return;

		if (resultGroup.isThresholdReach(result.getDocs()[searchRequest
				.getStart()]))
			return;

		newDocumentCount = docCount;
		resultGroup.populate(result);
	}

	public void setStep(int step) {
		this.step = step;
	}

	public int getNewDocumentCount() {
		return newDocumentCount;
	}

}
