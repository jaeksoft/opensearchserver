/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012-2015 Emmanuel Keller / Jaeksoft
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.render.Render;
import com.jaeksoft.searchlib.request.AbstractRequest;
import com.jaeksoft.searchlib.request.NamedEntityExtractionRequest;
import com.jaeksoft.searchlib.result.collector.DocIdInterface;
import com.jaeksoft.searchlib.util.Timer;
import com.jaeksoft.searchlib.webservice.query.document.DocumentsResult;
import com.jaeksoft.searchlib.webservice.query.document.IndexDocumentResult;
import com.jaeksoft.searchlib.webservice.query.namedEntity.NamedEntityResult;
import com.opensearchserver.client.common.search.result.VectorPosition;
import com.opensearchserver.client.v2.search.DocumentResult2;

public class ResultNamedEntityExtraction extends
		AbstractResult<AbstractRequest> implements
		ResultDocumentsInterface<AbstractRequest> {

	final private Map<Integer, ResultDocument> docMap;
	final private List<ResultDocument> docList;
	final private StringBuilder enrichedText;

	public ResultNamedEntityExtraction(
			final NamedEntityExtractionRequest request) {
		super(request);
		this.docMap = new TreeMap<Integer, ResultDocument>();
		this.docList = new ArrayList<ResultDocument>(0);
		this.enrichedText = new StringBuilder();
	}

	public void addFieldValue(Integer docId, String field, String value) {
		ResultDocument doc = docMap.get(docId);
		if (doc == null) {
			doc = new ResultDocument(docId);
			docMap.put(docId, doc);
			docList.add(doc);
		}
		doc.addReturnedField(field, value);
	}

	@Override
	public ResultDocument getDocument(long pos, Timer timer)
			throws SearchLibException {
		if (docList == null || pos < 0 || pos > docList.size())
			return null;
		return docList.get((int) pos);
	}

	@Override
	public void populate(List<IndexDocumentResult> indexDocuments)
			throws IOException, SearchLibException {
		throw new SearchLibException("Method not available");
	}

	public void resolvePositions(String namedEntityField,
			Map<String, List<VectorPosition>> tokenMap, String text) {
		Map<Integer, VectorPosition> mapPositions = new TreeMap<Integer, VectorPosition>();
		for (ResultDocument document : docList) {
			String entity = document.getValueContent(namedEntityField, 0);
			if (entity == null)
				continue;
			List<VectorPosition> positions = tokenMap.get(entity);
			document.addPositions(positions);
			if (positions != null)
				for (VectorPosition position : positions)
					mapPositions.put(position.start, position);
		}
		int lastPos = 0;
		for (VectorPosition posDoc : mapPositions.values()) {
			if (posDoc.start > lastPos)
				enrichedText.append(text.substring(lastPos, posDoc.start));
			enrichedText.append("<strong>");
			enrichedText.append(text.substring(posDoc.start, posDoc.end));
			enrichedText.append("</strong>");
			lastPos = posDoc.end;
		}
		if (lastPos < text.length())
			enrichedText.append(text.substring(lastPos));
	}

	public String getEnrichedText() {
		return enrichedText.toString();
	}

	@Override
	public float getScore(long pos) {
		return 0;
	}

	@Override
	public Float getDistance(long pos) {
		return null;
	}

	@Override
	public int getCollapseCount(long pos) {
		return 0;
	}

	@Override
	public long getNumFound() {
		if (docList == null)
			return 0;
		return docList.size();
	}

	@Override
	protected Render getRenderXml() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Render getRenderCsv() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Render getRenderJson(boolean indent) {
		return null;
	}

	@Override
	public Iterator<ResultDocument> iterator() {
		return new ResultDocumentIterator(this, null);
	}

	@Override
	public int getDocumentCount() {
		return docList.size();
	}

	@Override
	public long getRequestStart() {
		return 0;
	}

	@Override
	public int getRequestRows() {
		return docList.size();
	}

	@Override
	public DocIdInterface getDocs() {
		return null;
	}

	@Override
	public float getMaxScore() {
		return 0;
	}

	@Override
	public long getCollapsedDocCount() {
		return 0;
	}

	public NamedEntityResult getNamedEntityResult() throws SearchLibException {
		List<DocumentResult2> documents = new ArrayList<DocumentResult2>();
		DocumentsResult.populateDocumentList(this, documents);
		return new NamedEntityResult(getEnrichedText(), documents);
	}

}
