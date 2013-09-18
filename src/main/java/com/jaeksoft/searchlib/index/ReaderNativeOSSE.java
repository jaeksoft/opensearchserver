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
import java.util.BitSet;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.similar.MoreLikeThis;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.PerFieldAnalyzer;
import com.jaeksoft.searchlib.filter.FilterAbstract;
import com.jaeksoft.searchlib.filter.FilterHits;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.osse.OsseDocCursor;
import com.jaeksoft.searchlib.index.osse.OsseErrorHandler;
import com.jaeksoft.searchlib.index.osse.OsseFieldList;
import com.jaeksoft.searchlib.index.osse.OsseFieldList.FieldInfo;
import com.jaeksoft.searchlib.index.osse.OsseIndex;
import com.jaeksoft.searchlib.index.osse.OsseQuery;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.AbstractRequest;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.result.AbstractResult;
import com.jaeksoft.searchlib.result.collector.AbstractCollector;
import com.jaeksoft.searchlib.schema.FieldValue;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.Timer;

public class ReaderNativeOSSE extends ReaderAbstract {

	final private ReadWriteLock rwl = new ReadWriteLock();

	private OsseIndex index;

	private OsseErrorHandler err;

	protected ReaderNativeOSSE(IndexConfig indexConfig, OsseIndex index)
			throws SearchLibException {
		super(indexConfig);
		this.index = index;
	}

	@Override
	public void close() {
		err.release();
	}

	@Override
	public int getDocFreq(Term term) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public IndexStatistics getStatistics() throws IOException {
		return new IndexStatistics();
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

	public void search(Query query, BitSet filter, AbstractCollector collector)
			throws SearchLibException, IOException {
		OsseQuery osseQuery = new OsseQuery(index, query);
		osseQuery.collect(collector);
		osseQuery.free();
		// TODO filter
	}

	@Override
	public TermDocs getTermDocs(Term t) throws SearchLibException {
		// TODO Auto-generated method stub
		return null;
	}

	public DocSetHits newDocSetHits(AbstractSearchRequest searchRequest,
			Schema schema, SchemaField defaultField, PerFieldAnalyzer analyzer,
			Timer timer) throws SearchLibException, ParseException,
			IOException, SyntaxError {

		FilterHits filterHits = searchRequest.getFilterList().getFilterHits(
				this, defaultField, searchRequest.getAnalyzer(), searchRequest,
				timer);

		DocSetHits dsh = new DocSetHits(this, searchRequest.getQuery(),
				filterHits, searchRequest.getSortFieldList(), timer);
		return dsh;
	}

	public Map<String, FieldValue> getDocumentFields(long docId,
			TreeSet<String> fieldSet, Timer timer) throws SearchLibException {
		OsseErrorHandler error = null;
		OsseDocCursor docCursor = null;
		Map<String, FieldValue> fieldValueMap = new TreeMap<String, FieldValue>();
		rwl.r.lock();
		try {
			error = new OsseErrorHandler();
			OsseFieldList fieldList = new OsseFieldList(index, error);
			for (String fieldName : fieldSet) {
				FieldInfo fieldInfo = fieldList.getFieldInfo(fieldName);
				if (fieldInfo != null) {
					docCursor = new OsseDocCursor(index, error);
					List<FieldValueItem> valueList = docCursor.getTerms(
							fieldInfo.pointer, docId);
					docCursor.release();
					if (valueList != null)
						fieldValueMap.put(fieldName, new FieldValue(fieldName,
								valueList));
				}
			}
			return fieldValueMap;
		} finally {
			rwl.r.unlock();
			if (docCursor != null)
				docCursor.release();
			if (error != null)
				error.release();
		}
	}

	public DocSetHits searchDocSet(AbstractSearchRequest searchRequest,
			Timer timer) throws SearchLibException, IOException, SyntaxError,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException, ParseException {
		rwl.r.lock();
		try {

			Schema schema = searchRequest.getConfig().getSchema();
			SchemaField defaultField = schema.getFieldList().getDefaultField();

			return newDocSetHits(searchRequest, schema, defaultField,
					searchRequest.getAnalyzer(), timer);

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
	public int numDocs() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void search(Query query, Filter filter, Collector collector)
			throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public FieldCacheIndex getStringIndex(String name) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int maxDoc() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

}
