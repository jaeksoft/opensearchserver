/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.TokenStream;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.ClientCatalogItem;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.analysis.FilterFactory;
import com.jaeksoft.searchlib.join.JoinResult;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.AbstractResultSearch;
import com.jaeksoft.searchlib.result.ResultDocument;
import com.jaeksoft.searchlib.result.collector.JoinDocInterface;
import com.jaeksoft.searchlib.schema.FieldValueItem;

public class IndexLookupFilter extends FilterFactory {

	private String indexName = null;
	private String requestName = null;
	private String returnField = null;

	@Override
	public void initProperties() throws SearchLibException {
		super.initProperties();
		List<String> values = new ArrayList<String>(0);
		for (ClientCatalogItem item : ClientCatalog.getClientCatalog(null))
			values.add(item.getIndexName());
		addProperty(ClassPropertyEnum.INDEX_LIST, "", values.toArray());
		addProperty(ClassPropertyEnum.SEARCH_REQUEST, "", null);
		addProperty(ClassPropertyEnum.RETURN_FIELD, "", null);
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
		SearchRequest searchRequest = (SearchRequest) indexClient
				.getNewRequest(requestName);
		return new IndexLookupTokenFilter(tokenStream, indexClient,
				searchRequest, returnField);
	}

	public static class IndexLookupTokenFilter extends AbstractTermFilter {

		private Client indexClient;
		private SearchRequest searchRequest;
		private String[] returnFields;

		private List<String> tokenQueue;
		private int currentQueuePos;

		public IndexLookupTokenFilter(TokenStream input, Client indexClient,
				SearchRequest searchRequest, String returnField) {
			super(input);
			tokenQueue = null;
			this.indexClient = indexClient;
			this.searchRequest = searchRequest;
			this.returnFields = StringUtils.split(returnField, '|');
		}

		private final boolean popToken() {
			if (tokenQueue == null)
				return false;
			if (currentQueuePos == tokenQueue.size())
				return false;
			createToken(tokenQueue.get(currentQueuePos++));
			return true;
		}

		private final void extractTokens(ResultDocument resultDoc) {
			for (String returnField : returnFields) {
				FieldValueItem[] fieldValueItems = resultDoc
						.getValueArray(returnField);
				if (fieldValueItems == null)
					continue;
				for (FieldValueItem fieldValueItem : fieldValueItems)
					tokenQueue.add(fieldValueItem.getValue());
			}
		}

		@Override
		public final boolean incrementToken() throws IOException {
			current = captureState();
			try {
				for (;;) {
					if (popToken())
						return true;
					if (!input.incrementToken())
						return false;
					searchRequest.reset();
					searchRequest.setQueryString(termAtt.toString());
					AbstractResultSearch result = (AbstractResultSearch) indexClient
							.request(searchRequest);
					if (result.getNumFound() == 0)
						continue;
					int max = searchRequest.getEnd();
					if (max > result.getNumFound())
						max = result.getNumFound();
					tokenQueue = new ArrayList<String>(0);
					currentQueuePos = 0;
					for (int i = 0; i < max; i++) {
						ResultDocument resultDoc = result.getDocument(i);
						extractTokens(resultDoc);
						JoinResult[] joinResults = result.getJoinResult();
						if (joinResults != null)
							for (JoinResult joinResult : joinResults) {
								extractTokens(joinResult.getDocument(
										(JoinDocInterface) result.getDocs(), i,
										null));

							}
					}
				}
			} catch (SearchLibException e) {
				throw new IOException(e);
			}
		}
	}

}
