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
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.ClientCatalogItem;
import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.analysis.FilterFactory;
import com.jaeksoft.searchlib.analysis.TokenTerm;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.join.JoinResult;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.AbstractResultSearch;
import com.jaeksoft.searchlib.result.ResultDocument;
import com.jaeksoft.searchlib.result.collector.JoinDocInterface;
import com.jaeksoft.searchlib.schema.FieldValueItem;

public class IndexLookupFilter extends FilterFactory {

	private String indexName = null;
	private String requestName = null;
	private String returnField = null;
	private String requestedField = null;

	private final int maxTokenSearch = ClientFactory.INSTANCE
			.getBooleanQueryMaxClauseCount().getValue();

	@Override
	public void initProperties() throws SearchLibException {
		super.initProperties();
		List<String> values = new ArrayList<String>(0);
		for (ClientCatalogItem item : ClientCatalog.getClientCatalog(null))
			values.add(item.getIndexName());
		addProperty(ClassPropertyEnum.INDEX_LIST, "", values.toArray());
		addProperty(ClassPropertyEnum.SEARCH_REQUEST, "", null);
		addProperty(ClassPropertyEnum.REQUESTED_FIELD, "", null);
		addProperty(ClassPropertyEnum.RETURN_FIELD, "", null);
	}

	@Override
	public void checkValue(ClassPropertyEnum prop, String value)
			throws SearchLibException {
		if (prop == ClassPropertyEnum.INDEX_LIST)
			indexName = value;
		else if (prop == ClassPropertyEnum.SEARCH_REQUEST)
			requestName = value;
		else if (prop == ClassPropertyEnum.REQUESTED_FIELD)
			requestedField = value;
		else if (prop == ClassPropertyEnum.RETURN_FIELD)
			returnField = value;
	}

	@Override
	public TokenStream create(TokenStream tokenStream)
			throws SearchLibException {
		Client indexClient = ClientCatalog.getClient(indexName);
		SearchRequest searchRequest = (SearchRequest) indexClient
				.getNewRequest(requestName);
		searchRequest.setDefaultOperator("OR");
		return new IndexLookupTokenFilter(tokenStream, indexClient,
				searchRequest, requestedField, returnField, maxTokenSearch);
	}

	public static class IndexLookupTokenFilter extends AbstractTermFilter {

		private final Client indexClient;
		private final SearchRequest searchRequest;
		private final String[] returnFields;
		private final String requestedField;

		private final int batchBuffer;
		private final List<TokenTerm> collectedTokenBuffer;
		private final List<TokenTerm> tokenQueue;
		private int currentQueuePos;

		public IndexLookupTokenFilter(TokenStream input, Client indexClient,
				SearchRequest searchRequest, String requestedField,
				String returnField, int batchBuffer) {
			super(input);
			tokenQueue = new ArrayList<TokenTerm>(0);
			this.batchBuffer = batchBuffer;
			collectedTokenBuffer = new ArrayList<TokenTerm>(batchBuffer);
			this.indexClient = indexClient;
			this.searchRequest = searchRequest;
			this.returnFields = StringUtils.split(returnField, '|');
			this.requestedField = requestedField != null
					&& requestedField.length() > 0 ? requestedField
					: returnFields[0];
		}

		private final boolean popToken() {
			if (tokenQueue.size() == 0)
				return false;
			if (currentQueuePos == tokenQueue.size())
				return false;
			createToken(tokenQueue.get(currentQueuePos++));
			return true;
		}

		private final void extractTokens(TokenTerm tokenTerm,
				ResultDocument resultDoc) {
			for (String returnField : returnFields) {
				FieldValueItem[] fieldValueItems = resultDoc
						.getValueArray(returnField);
				if (fieldValueItems == null)
					continue;
				for (FieldValueItem fieldValueItem : fieldValueItems)
					tokenQueue.add(new TokenTerm(fieldValueItem.getValue(),
							tokenTerm, returnField));
			}
		}

		private final void searchTokens() throws SearchLibException,
				ParseException, SyntaxError, IOException {
			TokenTerm mergedTokenTerm = new TokenTerm(collectedTokenBuffer);
			searchRequest.reset();
			BooleanQuery bq = new BooleanQuery();
			for (TokenTerm tokenTerm : collectedTokenBuffer)
				bq.add(new TermQuery(new Term(requestedField, tokenTerm.term)),
						Occur.SHOULD);
			searchRequest.setBoostedComplexQuery(bq);
			searchRequest.setRows(collectedTokenBuffer.size());
			AbstractResultSearch result = (AbstractResultSearch) indexClient
					.request(searchRequest);
			collectedTokenBuffer.clear();
			if (result.getNumFound() == 0)
				return;
			int max = searchRequest.getEnd();
			if (max > result.getNumFound())
				max = result.getNumFound();
			tokenQueue.clear();
			currentQueuePos = 0;
			for (int i = 0; i < max; i++) {
				ResultDocument resultDoc = result.getDocument(i);
				extractTokens(mergedTokenTerm, resultDoc);
				JoinResult[] joinResults = result.getJoinResult();
				if (joinResults != null)
					for (JoinResult joinResult : joinResults) {
						extractTokens(mergedTokenTerm, joinResult.getDocument(
								(JoinDocInterface) result.getDocs(), i, null));

					}
			}
		}

		@Override
		public final boolean incrementToken() throws IOException {
			current = captureState();
			try {
				for (;;) {
					if (popToken())
						return true;
					if (!input.incrementToken()) {
						if (collectedTokenBuffer.size() == 0)
							return false;
						searchTokens();
						continue;
					}
					collectedTokenBuffer.add(new TokenTerm(termAtt.toString(),
							offsetAtt.startOffset(), offsetAtt.endOffset(),
							posIncrAtt.getPositionIncrement(), null));
					if (collectedTokenBuffer.size() >= batchBuffer)
						searchTokens();
				}
			} catch (SearchLibException e) {
				throw new IOException(e);
			} catch (ParseException e) {
				throw new IOException(e);
			} catch (SyntaxError e) {
				throw new IOException(e);
			}
		}
	}

}
