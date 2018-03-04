/*
 * Copyright 2017-2018 Emmanuel Keller / Jaeksoft
 *  <p>
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  <p>
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  <p>
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.jaeksoft.opensearchserver.front.search;

import com.jaeksoft.opensearchserver.Components;
import com.jaeksoft.opensearchserver.front.ServletTransaction;
import com.jaeksoft.opensearchserver.model.AccountRecord;
import com.jaeksoft.opensearchserver.model.UrlRecord;
import com.jaeksoft.opensearchserver.services.IndexService;
import com.qwazr.search.index.ResultDefinition;
import com.qwazr.search.index.ResultDocumentObject;
import com.qwazr.utils.LinkUtils;
import com.qwazr.utils.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.NotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SearchTransaction extends ServletTransaction {

	private final static String TEMPLATE = "search/home.ftl";

	private final IndexService indexService;

	public SearchTransaction(final Components components, final AccountRecord accountRecord, final String indexName,
			final HttpServletRequest request, final HttpServletResponse response)
			throws IOException, URISyntaxException {
		super(components, request, response, false);
		this.indexService = components.getIndexesService().getIndex(accountRecord.id, indexName);
		if (indexService == null)
			throw new NotFoundException("Index not found: " + indexName);
		request.setAttribute("account", accountRecord);
		request.setAttribute("indexName", indexName);
	}

	@Override
	protected String getTemplate() {
		return TEMPLATE;
	}

	@Override
	public void doGet() throws IOException, ServletException {
		final int start = getRequestParameter("start", 0);
		final String keywords = request.getParameter("keywords");
		if (!StringUtils.isBlank(keywords)) {
			final ResultDefinition.WithObject<UrlRecord> results = indexService.search(keywords, start, 25);
			request.setAttribute("keywords", keywords);
			request.setAttribute("numDocs", results.getTotalHits());
			request.setAttribute("totalTime", (double) (results.getTimer().totalTime) / 1000);
			request.setAttribute("results", Result.from(results.documents));
		}
		super.doGet();
	}

	public static class Result {

		private final String url;
		private final String urlDisplay;
		private final String title;
		private final String description;
		private final String content;

		private Result(ResultDocumentObject<UrlRecord> document) {
			url = document.record.urlStore;
			urlDisplay = url == null ? null : LinkUtils.urlHostPathWrapReduce(url, 70);
			title = document.highlights == null ? null : document.highlights.get("title");
			description = document.highlights == null ? null : document.highlights.get("description");
			content = document.highlights == null ? null : document.highlights.get("content");
		}

		public String getUrl() {
			return url;
		}

		public String getUrlDisplay() {
			return urlDisplay;
		}

		public String getTitle() {
			return title;
		}

		public String getDescription() {
			return description;
		}

		public String getContent() {
			return content;
		}

		public static List<Result> from(final List<ResultDocumentObject<UrlRecord>> documentResults) {
			if (documentResults == null)
				return null;
			if (documentResults.isEmpty())
				return Collections.emptyList();
			final List<Result> results = new ArrayList<>(documentResults.size());
			documentResults.forEach(doc -> results.add(new Result(doc)));
			return results;
		}
	}
}
