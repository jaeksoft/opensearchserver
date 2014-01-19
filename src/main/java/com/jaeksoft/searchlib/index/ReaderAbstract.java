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

import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.PerFieldAnalyzer;
import com.jaeksoft.searchlib.filter.FilterAbstract;
import com.jaeksoft.searchlib.filter.FilterHits;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.docvalue.DocValueInterface;
import com.jaeksoft.searchlib.index.docvalue.DocValueType;
import com.jaeksoft.searchlib.index.docvalue.OrderDocValue;
import com.jaeksoft.searchlib.index.docvalue.RadiansDocValue;
import com.jaeksoft.searchlib.index.docvalue.ReverseOrderDocValue;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.util.Timer;

public abstract class ReaderAbstract implements ReaderInterface {

	final protected IndexConfig indexConfig;

	public ReaderAbstract(IndexConfig indexConfig) {
		this.indexConfig = indexConfig;
	}

	public abstract FilterHits getFilterHits(SchemaField defaultField,
			PerFieldAnalyzer analyzer, AbstractSearchRequest request,
			FilterAbstract<?> filter, Timer timer) throws ParseException,
			IOException;

	public abstract int numDocs();

	public abstract void search(Query query, Filter filter, Collector collector)
			throws IOException;

	@Override
	public abstract FieldCacheIndex getStringIndex(String name)
			throws IOException;

	public abstract DocSetHits newDocSetHits(
			AbstractSearchRequest searchRequest, Schema schema,
			SchemaField defaultField, PerFieldAnalyzer analyzer, Timer timer)
			throws IOException, ParseException, SyntaxError,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException, SearchLibException;

	public abstract DocSetHits searchDocSet(
			AbstractSearchRequest searchRequest, Timer timer)
			throws IOException, ParseException, SyntaxError,
			SearchLibException, InstantiationException, IllegalAccessException,
			ClassNotFoundException;

	public abstract int maxDoc() throws IOException;

	final public DocValueInterface getDocValueInterface(final String field,
			final DocValueType type) throws IOException {
		FieldCacheIndex stringIndex = getStringIndex(field);
		if (stringIndex == null)
			throw new IOException("Not string index for field: " + field);
		switch (type) {
		case ORD:
			return new OrderDocValue(stringIndex);
		case RORD:
			return new ReverseOrderDocValue(stringIndex);
		case RADIANS:
			return new RadiansDocValue(stringIndex);
		default:
			throw new IOException("Unknown doc value type: " + type);
		}
	}
}
