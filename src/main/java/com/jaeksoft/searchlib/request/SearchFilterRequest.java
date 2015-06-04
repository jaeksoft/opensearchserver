/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.request;

import java.io.IOException;

import org.apache.lucene.search.Query;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.filter.FilterAbstract;
import com.jaeksoft.searchlib.filter.FilterList;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.util.XmlWriter;

public class SearchFilterRequest extends AbstractLocalSearchRequest implements
		RequestInterfaces.ReturnedFieldInterface,
		RequestInterfaces.FilterListInterface {

	private final FilterAbstract<?> filter;

	public SearchFilterRequest(Config config, Query query,
			FilterAbstract<?> filter) {
		super(config, null);
		setBoostedComplexQuery(query);
		this.filter = filter;
		setStart(0);
		setRows(0);
	}

	@Override
	protected Query newSnippetQuery(String queryString) throws IOException,
			ParseException, SyntaxError, SearchLibException {
		return null;
	}

	@Override
	public Query newComplexQuery(String queryString) throws ParseException,
			SyntaxError, SearchLibException, IOException {
		return null;
	}

	@Override
	protected void writeSubXmlConfig(XmlWriter xmlWriter) throws SAXException {
	}

	@Override
	public FilterList getFilterList() {
		return null;
	}

	@Override
	public boolean isScoreRequired() {
		return false;
	}

	@Override
	public boolean isDistanceRequired() {
		return filter.isDistance();
	}

	@Override
	public boolean isDocIdRequired() {
		return true;
	}

	@Override
	public boolean isForFilter() {
		return true;
	}

	@Override
	public String getInfo() {
		return null;
	}

}
