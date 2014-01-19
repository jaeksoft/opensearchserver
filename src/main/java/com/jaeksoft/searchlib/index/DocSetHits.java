/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2014 Emmanuel Keller / Jaeksoft
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

import org.apache.lucene.search.Query;
import org.apache.lucene.util.OpenBitSet;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.filter.FilterHits;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.result.collector.CollectorInterface;
import com.jaeksoft.searchlib.result.collector.docsethit.DistanceCollector;
import com.jaeksoft.searchlib.result.collector.docsethit.DocIdBufferCollector;
import com.jaeksoft.searchlib.result.collector.docsethit.DocSetHitBaseCollector;
import com.jaeksoft.searchlib.result.collector.docsethit.DocSetHitCollectorInterface;
import com.jaeksoft.searchlib.result.collector.docsethit.ScoreBufferAdvancedCollector;
import com.jaeksoft.searchlib.result.collector.docsethit.ScoreBufferCollector;
import com.jaeksoft.searchlib.scoring.AdvancedScore;
import com.jaeksoft.searchlib.sort.SortFieldList;
import com.jaeksoft.searchlib.sort.SorterAbstract;
import com.jaeksoft.searchlib.util.Timer;

public class DocSetHits {

	private final DocSetHitBaseCollector docSetHitCollector;
	private final DocIdBufferCollector docIdBufferCollector;
	private final DistanceCollector distanceCollector;
	private final ScoreBufferCollector scoreBufferCollector;
	private final DocSetHitCollectorInterface lastCollector;

	protected DocSetHits(ReaderLocal reader,
			AbstractSearchRequest searchRequest, FilterHits filterHits,
			SortFieldList sortFieldList, Timer timer) throws IOException,
			ParseException, SyntaxError, SearchLibException {
		Query query = searchRequest.getQuery();
		if (reader.numDocs() == 0) {
			docSetHitCollector = null;
			distanceCollector = null;
			docIdBufferCollector = null;
			scoreBufferCollector = null;
			lastCollector = null;
			return;
		}
		ScoreBufferCollector sc = null;
		DocSetHitCollectorInterface last = docSetHitCollector = new DocSetHitBaseCollector(
				reader.maxDoc());
		if (searchRequest.isScoreRequired())
			last = sc = new ScoreBufferCollector(docSetHitCollector);
		if (searchRequest.isDistanceRequired())
			last = distanceCollector = new DistanceCollector(
					docSetHitCollector, reader,
					searchRequest.getGeoParameters());
		else
			distanceCollector = null;
		AdvancedScore advancedScore = searchRequest.getAdvancedScore();
		if (advancedScore != null && !advancedScore.isEmpty()) {
			last = sc = new ScoreBufferAdvancedCollector(reader, searchRequest,
					docSetHitCollector, sc, distanceCollector);
		}
		if (searchRequest.isDocIdRequired())
			last = docIdBufferCollector = new DocIdBufferCollector(
					docSetHitCollector);
		else
			docIdBufferCollector = null;
		Timer t = new Timer(timer, "DocSetHits: " + query.toString());
		reader.search(query, filterHits, docSetHitCollector.collector);
		t.getDuration();
		last.endCollection();
		if (sortFieldList != null) {
			SorterAbstract sorter = sortFieldList.getSorter(last, reader);
			sorter.quickSort(timer);
		}
		lastCollector = last;
		scoreBufferCollector = sc;
	}

	final public int getNumFound() {
		if (docSetHitCollector == null)
			return 0;
		return docSetHitCollector.getSize();
	}

	final public OpenBitSet getBitSet() {
		if (docIdBufferCollector == null)
			return null;
		return docIdBufferCollector.getBitSet();
	}

	final public int[] getIds() {
		if (docIdBufferCollector == null)
			return null;
		return docIdBufferCollector.getIds();
	}

	final public float[] getScores() {
		if (scoreBufferCollector == null)
			return null;
		return scoreBufferCollector.getScores();
	}

	final public float getMaxScore() {
		if (scoreBufferCollector == null)
			return 0;
		return scoreBufferCollector.getMaxScore();
	}

	final public <T extends CollectorInterface> T getCollector(
			Class<T> collectorType) {
		return lastCollector.getCollector(collectorType);
	}

}
