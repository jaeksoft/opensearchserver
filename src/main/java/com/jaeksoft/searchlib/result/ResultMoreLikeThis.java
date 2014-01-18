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

package com.jaeksoft.searchlib.result;

import java.io.IOException;
import java.util.Iterator;
import java.util.TreeSet;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.filter.FilterAbstract;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.DocSetHits;
import com.jaeksoft.searchlib.index.ReaderAbstract;
import com.jaeksoft.searchlib.index.ReaderInterface;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.render.Render;
import com.jaeksoft.searchlib.render.RenderMoreLikeThisJson;
import com.jaeksoft.searchlib.render.RenderMoreLikeThisXml;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.request.MoreLikeThisRequest;
import com.jaeksoft.searchlib.request.SearchPatternRequest;
import com.jaeksoft.searchlib.result.collector.DocIdInterface;
import com.jaeksoft.searchlib.result.collector.ScoreInterface;
import com.jaeksoft.searchlib.util.Timer;

public class ResultMoreLikeThis extends AbstractResult<MoreLikeThisRequest>
		implements ResultDocumentsInterface<MoreLikeThisRequest> {

	final private ReaderInterface reader;

	final private DocIdInterface docs;

	final private ScoreInterface scores;

	final private TreeSet<String> fieldNameSet;

	public ResultMoreLikeThis(ReaderAbstract reader, MoreLikeThisRequest request)
			throws SearchLibException, IOException, ParseException,
			SyntaxError, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		super(request);
		this.reader = reader;
		AbstractSearchRequest searchRequest = new SearchPatternRequest(
				request.getConfig());
		for (FilterAbstract<?> filter : request.getFilterList())
			searchRequest.getFilterList().add(filter);
		searchRequest.setBoostedComplexQuery(request.getQuery());
		DocSetHits dsh = reader.searchDocSet(searchRequest, timer);
		if (dsh == null) {
			fieldNameSet = null;
			docs = null;
			scores = null;
			return;
		}
		docs = dsh.getCollector(DocIdInterface.class);
		scores = dsh.getCollector(ScoreInterface.class);
		fieldNameSet = new TreeSet<String>();
		request.getReturnFieldList().populate(fieldNameSet);
	}

	@Override
	public ResultDocument getDocument(int pos, Timer timer)
			throws SearchLibException {
		if (docs == null)
			return null;
		if (pos < 0 || pos >= docs.getSize())
			return null;
		try {
			return new ResultDocument(fieldNameSet, docs.getIds()[pos], reader,
					null, timer);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (ParseException e) {
			throw new SearchLibException(e);
		} catch (SyntaxError e) {
			throw new SearchLibException(e);
		}
	}

	@Override
	public float getScore(int pos) {
		if (scores == null)
			return 0;
		return scores.getScores()[pos];
	}

	@Override
	public Float getDistance(int pos) {
		return null;
	}

	@Override
	public int getCollapseCount(int pos) {
		return ResultDocument.getCollapseCount(docs, pos);
	}

	@Override
	public int getNumFound() {
		if (docs == null)
			return 0;
		return docs.getSize();
	}

	@Override
	protected Render getRenderXml() {
		return new RenderMoreLikeThisXml(this);
	}

	@Override
	protected Render getRenderCsv() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Render getRenderJson(boolean indent) {
		return new RenderMoreLikeThisJson(this, indent);
	}

	@Override
	public Iterator<ResultDocument> iterator() {
		return new ResultDocumentIterator(this, null);
	}

	@Override
	public int getDocumentCount() {
		int end = request.getEnd();
		int len = getNumFound();
		if (end > len)
			end = len;
		return end - request.getStart();
	}

	@Override
	public int getRequestStart() {
		return request.getStart();
	}

	@Override
	public int getRequestRows() {
		return request.getRows();
	}

	@Override
	public DocIdInterface getDocs() {
		return docs;
	}

	@Override
	public float getMaxScore() {
		if (scores == null)
			return 0;
		return scores.getMaxScore();
	}

	@Override
	public int getCollapsedDocCount() {
		return 0;
	}

}
