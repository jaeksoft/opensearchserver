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

import com.jaeksoft.searchlib.analysis.PerFieldAnalyzer;
import com.jaeksoft.searchlib.filter.FilterAbstract;
import com.jaeksoft.searchlib.filter.FilterHits;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.util.Timer;

public abstract class ReaderAbstract implements ReaderInterface {

	final protected IndexConfig indexConfig;

	public ReaderAbstract(IndexConfig indexConfig) {
		this.indexConfig = indexConfig;
	}

	public abstract FilterHits getFilterHits(SchemaField defaultField,
			PerFieldAnalyzer analyzer, FilterAbstract<?> filter, Timer timer)
			throws ParseException, IOException;

	public abstract int numDocs();

	public abstract void search(Query query, Filter filter, Collector collector)
			throws IOException;

	public abstract FieldCacheIndex getStringIndex(String name)
			throws IOException;

	public abstract int maxDoc() throws IOException;

}
