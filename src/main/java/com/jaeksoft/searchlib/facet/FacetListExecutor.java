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

package com.jaeksoft.searchlib.facet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.ReaderAbstract;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.result.collector.CollapseDocInterface;
import com.jaeksoft.searchlib.result.collector.DocIdInterface;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.schema.SchemaFieldList;
import com.jaeksoft.searchlib.util.ThreadUtils;
import com.jaeksoft.searchlib.util.ThreadUtils.ExceptionCatchThread;
import com.jaeksoft.searchlib.util.Timer;

public class FacetListExecutor {

	private final Timer facetTimer;
	private final List<FacetThread> threads;
	private final FacetList facetList;
	private final ReaderAbstract reader;
	private final DocIdInterface notCollapsedDocs;
	private final CollapseDocInterface collapsedDocs;

	public FacetListExecutor(Config config, ReaderAbstract reader, DocIdInterface notCollapsedDocs,
			CollapseDocInterface collapsedDocs, FacetFieldList facetFieldList, FacetList facetList, Timer timer)
					throws SearchLibException, ParseException, IOException, SyntaxError {
		this.reader = reader;
		this.collapsedDocs = collapsedDocs;
		this.notCollapsedDocs = notCollapsedDocs;
		this.facetList = facetList;
		int size = facetFieldList == null ? 0 : facetFieldList.size();
		if (size == 0) {
			threads = null;
			facetTimer = null;
			return;
		}
		facetTimer = new Timer(timer, "facet");
		threads = new ArrayList<FacetThread>();
		SchemaFieldList schemaFieldList = config.getSchema().getFieldList();
		for (FacetField facetField : facetFieldList) {
			SchemaField schemaField = schemaFieldList.get(facetField.getName());
			if (schemaField == null)
				continue;
			threads.add(new FacetThread(facetField, schemaField));
		}
		ThreadUtils.invokeAndJoin(config.getThreadPool(), threads);
		facetTimer.getDuration();
	}

	public class FacetThread extends ExceptionCatchThread {

		private final FacetField facetField;
		private final SchemaField schemaField;

		public FacetThread(FacetField facetField, SchemaField schemaField) {
			this.facetField = facetField;
			this.schemaField = schemaField;
		}

		@Override
		public void runner() throws ParseException, IOException, SearchLibException, SyntaxError {
			Timer t = new Timer(facetTimer, "facet - " + facetField.getName() + '(' + facetField.getMinCount() + ')');
			facetList.add(facetField.getFacet(reader, schemaField, notCollapsedDocs, collapsedDocs, t));
			t.getDuration();
		}
	}
}
