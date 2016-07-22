/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2015 Emmanuel Keller / Jaeksoft
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
import java.util.ArrayList;
import java.util.List;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.PerFieldAnalyzer;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.AbstractLocalSearchRequest;
import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.util.ThreadUtils;
import com.jaeksoft.searchlib.util.ThreadUtils.ExceptionCatchThread;
import com.jaeksoft.searchlib.util.Timer;
import com.jaeksoft.searchlib.webservice.query.search.SearchQueryAbstract.OperatorEnum;

public class FilterListExecutor {

	private final SchemaField defaultField;
	private final PerFieldAnalyzer analyzer;
	private final AbstractLocalSearchRequest request;
	private final OperatorEnum defaultOperator;
	private final Timer timer;
	private final FilterHits finalFilterHits;
	private final List<FilterThread> threads;

	public FilterListExecutor(AbstractLocalSearchRequest searchRequest, Timer timer)
			throws SearchLibException, ParseException, IOException, SyntaxError {
		Config config = searchRequest.getConfig();
		Schema schema = config.getSchema();
		defaultField = schema.getFieldList().getDefaultField();
		analyzer = searchRequest.getAnalyzer();
		request = searchRequest;
		this.timer = timer;
		FilterList filterList = searchRequest.getFilterList();
		this.defaultOperator = filterList == null ? null : filterList.getDefaultOperator();
		int size = filterList == null ? 0 : filterList.size();
		switch (size) {
		case 0:
			finalFilterHits = null;
			threads = null;
			return;
		case 1:
			finalFilterHits = filterList.first().getFilterHits(defaultField, analyzer, request, timer);
			threads = null;
			return;
		}
		threads = new ArrayList<FilterThread>();
		finalFilterHits = new FilterHits(true);
		for (FilterAbstract<?> filter : filterList)
			threads.add(new FilterThread(filter));
		ThreadUtils.invokeAndJoin(config.getThreadPool(), threads);
	}

	final public FilterHits getFilterHits() {
		return finalFilterHits;
	}

	public class FilterThread extends ExceptionCatchThread {

		private final FilterAbstract<?> filter;

		public FilterThread(FilterAbstract<?> filter) {
			this.filter = filter;
		}

		@Override
		public void runner() throws ParseException, IOException, SearchLibException, SyntaxError {
			FilterHits filterHits = filter.getFilterHits(defaultField, analyzer, request, timer);
			synchronized (finalFilterHits) {
				finalFilterHits.operate(filterHits, filter.getOperator(defaultOperator));
			}
		}
	}
}
