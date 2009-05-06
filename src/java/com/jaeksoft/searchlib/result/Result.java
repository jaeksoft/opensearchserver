/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.result;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Iterator;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;

import com.jaeksoft.searchlib.facet.FacetList;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.util.Debug;
import com.jaeksoft.searchlib.util.External;

public abstract class Result implements Externalizable,
		Iterable<ResultDocument> {

	transient protected SearchRequest searchRequest;
	transient protected Collapse collapse;
	protected FacetList facetList;
	private ResultScoreDoc[] docs;
	protected int numFound;
	protected float maxScore;
	protected int collapsedDocCount;
	private ResultDocuments resultDocuments;
	transient protected Debug debug;

	protected Result() {
		searchRequest = null;
		resultDocuments = null;
	}

	protected Result(SearchRequest searchRequest) {
		this();
		this.numFound = 0;
		this.maxScore = 0;
		this.collapsedDocCount = 0;
		this.docs = new ResultScoreDoc[0];
		this.searchRequest = searchRequest;
		if (searchRequest.isDebug())
			debug = new Debug();
		if (searchRequest.getFacetFieldList().size() > 0)
			this.facetList = new FacetList();
		collapse = new Collapse(searchRequest);
	}

	public SearchRequest getSearchRequest() {
		return this.searchRequest;
	}

	public Debug getDebug() {
		return debug;
	}

	public void setSearchRequest(SearchRequest searchRequest) {
		this.searchRequest = searchRequest;
	}

	public FacetList getFacetList() {
		return this.facetList;
	}

	protected void setDocuments(ResultDocuments resultDocuments) {
		this.resultDocuments = resultDocuments;
	}

	public ResultDocument getDocument(int pos) throws CorruptIndexException,
			IOException, ParseException, SyntaxError {
		if (pos < searchRequest.getStart())
			return null;
		if (pos >= searchRequest.getEnd())
			return null;
		if (pos >= getDocLength())
			return null;
		return resultDocuments.get(pos - searchRequest.getStart());
	}

	public Iterator<ResultDocument> iterator() {
		if (resultDocuments == null)
			return new ResultDocuments(0).iterator();
		return resultDocuments.iterator();
	}

	public float getMaxScore() {
		return maxScore;
	}

	public int getNumFound() {
		return numFound;
	}

	protected void setDocs(ResultScoreDoc[] docs) {
		this.docs = docs;
	}

	public int getDocLength() {
		if (docs == null)
			return 0;
		return docs.length;
	}

	public int getDocumentCount() {
		int end = searchRequest.getEnd();
		int len = getDocLength();
		if (end > len)
			end = len;
		return end - searchRequest.getStart();
	}

	public ResultScoreDoc[] getDocs() {
		return docs;
	}

	public Collapse getCollapse() {
		return collapse;
	}

	public int getCollapseDocCount() {
		return collapsedDocCount;
	}

	public float getScore(int pos) {
		if (docs == null)
			return 0;
		return docs[pos].score;
	}

	public int getCollapseCount(int pos) {
		if (docs == null)
			return 0;
		return docs[pos].collapseCount;
	}

	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {

		// Reading FacetList if any
		facetList = (FacetList) External.readObject(in);

		// Reading docs (from request.start to last document)
		int length = in.readInt();
		if (length > 0) {
			docs = new ResultScoreDoc[length];
			int start = in.readInt();
			for (int i = start; i < length; i++)
				docs[i] = (ResultScoreDoc) in.readObject();
		} else
			docs = null;

		// Reading numFound, maxScore and collapsedDocCount
		numFound = in.readInt();
		maxScore = in.readFloat();
		collapsedDocCount = in.readInt();

		// Reading ResultDocument if any
		resultDocuments = External.readObject(in);
	}

	public void writeExternal(ObjectOutput out) throws IOException {

		// Writing FacetList if any
		External.writeObject(facetList, out);

		// Writing docs (from request.start to last document)
		int length = searchRequest.getStart() + getDocumentCount();
		out.writeInt(length);
		if (length > 0) {
			int start = searchRequest.getStart();
			out.writeInt(start);
			for (int i = start; i < length; i++)
				out.writeObject(docs[+i]);
		}

		// Writing numFound, maxScore, collapsedDocCount
		out.writeInt(numFound);
		out.writeFloat(maxScore);
		out.writeInt(collapsedDocCount);

		// Writing ResultDocument if any
		External.writeObject(resultDocuments, out);
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(numFound);
		sb.append(" founds.");
		if (docs != null) {
			sb.append(' ');
			sb.append(docs.length);
			sb.append(" docs.");
		}
		if (resultDocuments != null) {
			sb.append(' ');
			sb.append(resultDocuments.size());
			sb.append("resultDocuments.");
		}
		sb.append(" MaxScore: ");
		sb.append(maxScore);
		if (searchRequest != null) {
			sb.append(" - ");
			sb.append(searchRequest);
		}
		return sb.toString();
	}
}
