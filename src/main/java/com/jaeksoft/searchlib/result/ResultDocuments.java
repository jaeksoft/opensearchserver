/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012-2013 Emmanuel Keller / Jaeksoft
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
import java.util.TreeSet;

import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.ReaderInterface;
import com.jaeksoft.searchlib.index.ReaderLocal;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.render.Render;
import com.jaeksoft.searchlib.render.RenderDocumentsJson;
import com.jaeksoft.searchlib.render.RenderDocumentsXml;
import com.jaeksoft.searchlib.request.AbstractRequest;
import com.jaeksoft.searchlib.request.DocumentsRequest;
import com.jaeksoft.searchlib.request.RequestInterfaces;
import com.jaeksoft.searchlib.result.collector.DocIdInterface;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.util.Timer;

public class ResultDocuments extends AbstractResult<AbstractRequest> implements
		ResultDocumentsInterface<AbstractRequest> {

	transient private ReaderInterface reader = null;
	final private TreeSet<String> fieldNameSet;
	final private List<Integer> docList;

	public ResultDocuments(ReaderInterface reader, AbstractRequest request,
			TreeSet<String> fieldNameSet, List<Integer> docList) {
		super(request);
		this.reader = reader;
		this.fieldNameSet = fieldNameSet == null ? new TreeSet<String>()
				: fieldNameSet;
		if (fieldNameSet.size() == 0
				&& request instanceof RequestInterfaces.ReturnedFieldInterface)
			((RequestInterfaces.ReturnedFieldInterface) request)
					.getReturnFieldList().populate(fieldNameSet);
		this.docList = new ArrayList<Integer>(docList);
	}

	public ResultDocuments(ReaderLocal reader, DocumentsRequest request)
			throws IOException {
		this(reader, request, null, request.getDocList());
		SchemaField uniqueField = request.getConfig().getSchema()
				.getFieldList().getUniqueField();
		if (uniqueField != null) {
			String uniqueFieldName = uniqueField.getName();
			for (String uniqueKey : request.getUniqueKeyList()) {
				TermDocs termDocs = reader.getTermDocs(new Term(
						uniqueFieldName, uniqueKey));
				if (termDocs != null)
					while (termDocs.next())
						docList.add(termDocs.doc());
				termDocs.close();
			}
		}
	}

	@Override
	public ResultDocument getDocument(int pos, Timer timer)
			throws SearchLibException {
		if (docList == null || pos < 0 || pos > docList.size())
			return null;
		try {
			return new ResultDocument(fieldNameSet, docList.get(pos), reader,
					timer);
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
		return 0;
	}

	@Override
	public int getCollapseCount(int pos) {
		return 0;
	}

	@Override
	public int getNumFound() {
		if (docList == null)
			return 0;
		return docList.size();
	}

	@Override
	protected Render getRenderXml() {
		return new RenderDocumentsXml(this);
	}

	@Override
	protected Render getRenderCsv() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Render getRenderJson(boolean indent) {
		return new RenderDocumentsJson(this, indent);
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
	public int getRequestStart() {
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
	public int getCollapsedDocCount() {
		return 0;
	}

}
