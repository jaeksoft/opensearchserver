/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2013 Emmanuel Keller / Jaeksoft
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
import java.util.Map;
import java.util.Set;

import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.similar.MoreLikeThis;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.PerFieldAnalyzer;
import com.jaeksoft.searchlib.filter.FilterAbstract;
import com.jaeksoft.searchlib.filter.FilterHits;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.AbstractRequest;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.result.AbstractResult;
import com.jaeksoft.searchlib.schema.FieldValue;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.util.Timer;

public interface ReaderInterface {

	public abstract boolean sameIndex(ReaderInterface reader);

	public void close();

	public Collection<?> getFieldNames() throws SearchLibException;

	public int getDocFreq(Term term) throws SearchLibException;

	public TermEnum getTermEnum() throws SearchLibException;

	public TermEnum getTermEnum(Term term) throws SearchLibException;

	public TermDocs getTermDocs(Term term) throws IOException,
			SearchLibException;

	public Map<String, FieldValue> getDocumentFields(final int docId,
			final Set<String> fieldNameSet, final Timer timer)
			throws IOException, ParseException, SyntaxError, SearchLibException;

	public TermFreqVector getTermFreqVector(final int docId, final String field)
			throws IOException, SearchLibException;

	public FieldCacheIndex getStringIndex(final String fieldName)
			throws IOException, SearchLibException;

	public FilterHits getFilterHits(SchemaField defaultField,
			PerFieldAnalyzer analyzer, AbstractSearchRequest request,
			FilterAbstract<?> filter, Timer timer) throws ParseException,
			IOException, SearchLibException;

	public void putTermVectors(final int[] docIds, final String field,
			final Collection<String[]> termVectors) throws IOException,
			SearchLibException;

	public abstract Query rewrite(Query query) throws SearchLibException;

	public abstract MoreLikeThis getMoreLikeThis() throws SearchLibException;

	public AbstractResult<?> request(AbstractRequest request)
			throws SearchLibException;

	public String explain(AbstractRequest request, int docId, boolean bHtml)
			throws SearchLibException;

	public IndexStatistics getStatistics() throws IOException,
			SearchLibException;

	public long getVersion() throws SearchLibException;

	public void reload() throws SearchLibException;

}
