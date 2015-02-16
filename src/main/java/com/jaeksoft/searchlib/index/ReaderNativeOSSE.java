/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.index;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.index.TermPositions;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.similar.MoreLikeThis;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.PerFieldAnalyzer;
import com.jaeksoft.searchlib.filter.FilterAbstract;
import com.jaeksoft.searchlib.filter.FilterHits;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.osse.api.OsseDocCursor;
import com.jaeksoft.searchlib.index.osse.api.OsseErrorHandler;
import com.jaeksoft.searchlib.index.osse.api.OsseIndex;
import com.jaeksoft.searchlib.index.osse.api.OsseIndex.FieldInfo;
import com.jaeksoft.searchlib.index.osse.query.OsseAbstractQuery;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.AbstractRequest;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.result.AbstractResult;
import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.util.IOUtils;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.Timer;

public class ReaderNativeOSSE extends ReaderAbstract {

	final private ReadWriteLock rwl = new ReadWriteLock();

	private OsseIndex index;

	protected ReaderNativeOSSE(IndexConfig indexConfig, OsseIndex index)
			throws SearchLibException {
		super(indexConfig);
		this.index = index;
	}

	@Override
	public void close() {
	}

	@Override
	public int getDocFreq(Term term) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public IndexStatistics getStatistics() throws IOException,
			SearchLibException {
		OsseErrorHandler error = null;
		try {
			error = new OsseErrorHandler();
			return index.getStatistics(error);
		} finally {
			IOUtils.close(error);
		}
	}

	@Override
	public TermFreqVector getTermFreqVector(int docId, String field) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void reload() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean sameIndex(ReaderInterface reader) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public TermEnum getTermEnum() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<?> getFieldNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MoreLikeThis getMoreLikeThis() throws SearchLibException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractResult<?> request(AbstractRequest request)
			throws SearchLibException {
		rwl.r.lock();
		try {
			return request.execute(this);
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public TermDocs getTermDocs(Term t) throws SearchLibException {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, List<String>> getDocumentFields(long docId,
			TreeSet<String> fieldSet, Timer timer) throws SearchLibException {
		OsseErrorHandler error = null;
		OsseDocCursor docCursor = null;
		Map<String, List<String>> fieldValueMap = new LinkedHashMap<String, List<String>>();
		rwl.r.lock();
		try {
			error = new OsseErrorHandler();
			Map<String, FieldInfo> fieldMap = index.getListOfFields(error);
			for (String fieldName : fieldMap.keySet()) {
				FieldInfo fieldInfo = fieldMap.get(fieldName);
				// TODO FIX
				if (fieldInfo != null) {
					docCursor = new OsseDocCursor(index, error);
					List<String> valueList = docCursor.getTerms(null, docId);
					docCursor.close();
					if (valueList != null)
						fieldValueMap.put(fieldName.intern(), valueList);
				}
			}
			return fieldValueMap;
		} finally {
			rwl.r.unlock();
			IOUtils.close(docCursor, error);
		}
	}

	@Override
	public DocSetHits searchDocSet(AbstractSearchRequest searchRequest,
			Timer timer) throws SearchLibException, IOException, SyntaxError,
			ParseException {
		rwl.r.lock();
		try {

			Schema schema = searchRequest.getConfig().getSchema();
			SchemaField defaultField = schema.getFieldList().getDefaultField();
			PerFieldAnalyzer analyzer = searchRequest.getAnalyzer();
			// FilterHits filterHits = searchRequest
			// .getFilterList()
			// .getFilterHits(defaultField, analyzer, searchRequest, timer);
			return new DocSetHits(this, searchRequest, null/* filterHits */);
		} catch (Exception e) {
			throw new SearchLibException(e);
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public TermEnum getTermEnum(Term term) throws SearchLibException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Query rewrite(Query query) throws SearchLibException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String explain(AbstractRequest request, int docId, boolean bHtml)
			throws SearchLibException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FilterHits getFilterHits(SchemaField defaultField,
			PerFieldAnalyzer analyzer, AbstractSearchRequest request,
			FilterAbstract<?> filter, Timer timer) throws ParseException,
			IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long numDocs() throws SearchLibException {
		OsseErrorHandler error = null;
		try {
			error = new OsseErrorHandler();
			return index.numDocs(error);
		} finally {
			IOUtils.close(error);
		}
	}

	@Override
	public void search(Query query, Filter filter, Collector collector)
			throws IOException {
		OsseErrorHandler error = null;
		try {
			error = new OsseErrorHandler();
			Map<String, FieldInfo> fieldMap = index.getListOfFields(error);
			OsseAbstractQuery osseQuery = OsseAbstractQuery.create(query);
			osseQuery.execute(index, fieldMap, error);
			// OsseQuery osseQuery = new OsseQuery(index, query);
			// osseQuery.collect(collector);
			// osseQuery.free();
			// TODO filter
		} catch (SearchLibException e) {
			throw new IOException(e);
		} finally {
			IOUtils.close(error);
		}

	}

	@Override
	public FieldCacheIndex getStringIndex(String name) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long maxDoc() throws IOException, SearchLibException {
		OsseErrorHandler error = null;
		try {
			error = new OsseErrorHandler();
			return index.maxDoc(error);
		} finally {
			IOUtils.close(error);
		}
	}

	@Override
	public Map<String, List<String>> getDocumentFields(int docId,
			Set<String> fieldNameSet, Timer timer) throws IOException,
			ParseException, SyntaxError, SearchLibException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void putTermVectors(int[] docIds, String field,
			Collection<String[]> termVectors) throws IOException,
			SearchLibException {
		// TODO Auto-generated method stub

	}

	@Override
	public Map<String, List<String>> getDocumentStoredField(int docId)
			throws IOException, SearchLibException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TermPositions getTermPositions() throws IOException,
			SearchLibException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getDocTerms(String field) throws IOException,
			SearchLibException {
		// TODO Auto-generated method stub
		return null;
	}

}
