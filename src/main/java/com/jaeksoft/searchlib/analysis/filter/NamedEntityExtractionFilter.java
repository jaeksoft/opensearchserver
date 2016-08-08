/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.analysis.filter;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.ClientCatalogItem;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.analysis.FilterFactory;
import com.jaeksoft.searchlib.request.NamedEntityExtractionRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.TokenStream;

import java.util.ArrayList;
import java.util.List;

public class NamedEntityExtractionFilter extends FilterFactory {

	private String indexName = null;
	private String requestName = null;
	private String returnField = null;

	@Override
	public void initProperties() throws SearchLibException {
		super.initProperties();
		List<String> values = new ArrayList<String>(0);
		for (ClientCatalogItem item : ClientCatalog.getClientCatalog(null))
			values.add(item.getIndexName());
		addProperty(ClassPropertyEnum.INDEX_LIST, "", values.toArray(), 0, 0);
		addProperty(ClassPropertyEnum.SEARCH_REQUEST, "", null, 20, 1);
		addProperty(ClassPropertyEnum.RETURN_FIELD, "", null, 30, 1);
	}

	@Override
	public void checkValue(ClassPropertyEnum prop, String value)
			throws SearchLibException {
		if (prop == ClassPropertyEnum.INDEX_LIST)
			indexName = value;
		else if (prop == ClassPropertyEnum.SEARCH_REQUEST)
			requestName = value;
		else if (prop == ClassPropertyEnum.RETURN_FIELD)
			returnField = value;
	}

	@Override
	public TokenStream create(TokenStream tokenStream)
			throws SearchLibException {
		Client indexClient = ClientCatalog.getClient(indexName);
		NamedEntityExtractionRequest request = (NamedEntityExtractionRequest) indexClient
				.getNewRequest(requestName);
		if (!StringUtils.isEmpty(returnField))
			request.setReturnedFields(StringUtils.split(returnField, '|'));
		for (FilterFactory filter : request.getFilterList(null))
			tokenStream = filter.create(tokenStream);
		return tokenStream;
	}

}
