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

package com.jaeksoft.searchlib.filter;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.index.ReaderLocal;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.schema.Field;
import com.jaeksoft.searchlib.util.Timer;
import com.jaeksoft.searchlib.util.XmlWriter;

public class QueryFilter extends FilterAbstract {

	private transient Query query;

	private String queryString;

	public QueryFilter(String req, boolean negative, Source src) {
		super(src, negative);
		this.queryString = req;
		this.query = null;
	}

	public Query getQuery(Field defaultField, Analyzer analyzer)
			throws ParseException {
		if (query != null)
			return query;
		QueryParser queryParser = new QueryParser(Version.LUCENE_29,
				defaultField.getName(), analyzer);
		queryParser.setLowercaseExpandedTerms(false);
		try {
			query = queryParser.parse(queryString);
		} catch (org.apache.lucene.queryParser.ParseException e) {
			throw new ParseException(e);
		}
		return query;
	}

	public String getQueryString() {
		return this.queryString;
	}

	public void setQueryString(String queryString) {
		this.queryString = queryString;
		this.query = null;
	}

	@Override
	public String getCacheKey(Field defaultField, Analyzer analyzer)
			throws ParseException {
		return "QueryFilter - " + getQuery(defaultField, analyzer).toString();
	}

	@Override
	public void writeXmlConfig(XmlWriter xmlWriter) throws SAXException {
		xmlWriter.startElement("filter", "negative", isNegative() ? "yes"
				: "no");
		xmlWriter.textNode(queryString);
		xmlWriter.endElement();
	}

	@Override
	public FilterHits getFilterHits(ReaderLocal reader, Field defaultField,
			Analyzer analyzer, Timer timer) throws ParseException, IOException {
		Query query = getQuery(defaultField, analyzer);
		FilterHits filterHits = new FilterHits(query, isNegative(), reader,
				timer);
		return filterHits;
	}

}
