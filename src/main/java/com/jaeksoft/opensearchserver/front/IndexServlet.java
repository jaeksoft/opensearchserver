/*
 * Copyright 2017-2018 Emmanuel Keller / Jaeksoft
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jaeksoft.opensearchserver.front;

import com.jaeksoft.opensearchserver.services.IndexesService;
import com.jaeksoft.opensearchserver.services.WebCrawlsService;
import com.qwazr.library.freemarker.FreeMarkerTool;
import com.qwazr.utils.StringUtils;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/index/*")
public class IndexServlet extends BaseServlet {

	final FreeMarkerTool freemarker;
	final IndexesService indexesService;
	final WebCrawlsService webCrawlsService;

	public IndexServlet(final FreeMarkerTool freemarker, final IndexesService indexesService,
			final WebCrawlsService webCrawlsService) {
		this.freemarker = freemarker;
		this.indexesService = indexesService;
		this.webCrawlsService = webCrawlsService;
	}

	@Override
	protected ServletTransaction getServletTransaction(final HttpServletRequest request,
			final HttpServletResponse response) {
		final String[] pathParts = StringUtils.split(request.getPathInfo(), '/');
		if (pathParts == null || pathParts.length == 0)
			return null;
		final String indexName = pathParts[0];
		if (pathParts.length == 1)
			return new IndexTransaction(this, indexName, request, response);
		return dispatchIndex(indexName, pathParts, request, response);
	}

	private ServletTransaction dispatchIndex(final String indexName, final String[] pathParts,
			final HttpServletRequest request, final HttpServletResponse response) {
		final String path2 = pathParts[1];
		if (StringUtils.isBlank(path2))
			return null;
		switch (path2) {
		case "crawler":
			return dispatchCrawler(indexName, pathParts, request, response);
		default:
			return null;
		}
	}

	private ServletTransaction dispatchCrawler(final String indexName, final String[] pathParts,
			final HttpServletRequest request, final HttpServletResponse response) {
		if (pathParts.length < 3)
			return null;
		final String path3 = pathParts[2];
		if (StringUtils.isBlank(path3))
			return null;
		switch (path3) {
		case "web":
			return dispatchCrawlerWeb(indexName, pathParts, request, response);
		default:
			return null;
		}
	}

	private ServletTransaction dispatchCrawlerWeb(final String indexName, final String[] pathParts,
			final HttpServletRequest request, final HttpServletResponse response) {
		if (pathParts.length < 4)
			return new CrawlerWebsTransaction(this, indexName, request, response);
		final String path4 = pathParts[3];
		if (StringUtils.isBlank(path4))
			return null;
		return new CrawlerWebTransaction(this, indexName, path4, request, response);
	}

}