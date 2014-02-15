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
import com.jaeksoft.searchlib.geo.GeoParameters;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.request.SearchFilterRequest;
import com.jaeksoft.searchlib.result.collector.CollectorInterface;
import com.jaeksoft.searchlib.result.collector.docsethit.DistanceCollector;
import com.jaeksoft.searchlib.result.collector.docsethit.DocIdBufferCollector;
import com.jaeksoft.searchlib.result.collector.docsethit.DocSetHitBaseCollector;
import com.jaeksoft.searchlib.result.collector.docsethit.DocSetHitBaseCollector.FilterHitsCollector;
import com.jaeksoft.searchlib.result.collector.docsethit.DocSetHitCollectorInterface;
import com.jaeksoft.searchlib.result.collector.docsethit.ScoreBufferAdvancedCollector;
import com.jaeksoft.searchlib.result.collector.docsethit.ScoreBufferCollector;
import com.jaeksoft.searchlib.scoring.AdvancedScore;
import com.jaeksoft.searchlib.util.Timer;

public class DocSetHits {

	public static class Params {

		final ReaderAbstract reader;
		final Query query;
		final GeoParameters geoParameters;
		final AdvancedScore advancedScore;
		final FilterHits filterHits;
		final boolean isScoreRequired;
		final boolean isDistanceRequired;
		final boolean isDocIdRequired;
		final boolean forFilterHits;

		public Params(ReaderAbstract reader,
				AbstractSearchRequest searchRequest, FilterHits filterHits)
				throws ParseException, SyntaxError, SearchLibException,
				IOException {
			this.reader = reader;
			this.query = searchRequest.getQuery();
			this.isScoreRequired = searchRequest.isScoreRequired();
			this.isDistanceRequired = searchRequest.isDistanceRequired();
			this.geoParameters = searchRequest.getGeoParameters();
			this.advancedScore = searchRequest.getAdvancedScore();
			this.isDocIdRequired = searchRequest.isDocIdRequired();
			this.filterHits = filterHits;
			this.forFilterHits = searchRequest instanceof SearchFilterRequest;
		}
	}

	private final DocSetHitBaseCollector docSetHitCollector;
	private final DocIdBufferCollector docIdBufferCollector;
	private final DistanceCollector distanceCollector;
	private final ScoreBufferCollector scoreBufferCollector;
	private final DocSetHitCollectorInterface lastCollector;

	protected DocSetHits(Params params, Timer timer) throws IOException,
			ParseException, SyntaxError, SearchLibException {
		ScoreBufferCollector sc = null;
		DocSetHitCollectorInterface last = docSetHitCollector = new DocSetHitBaseCollector(
				params.reader.maxDoc(), params.forFilterHits);
		if (params.isScoreRequired)
			last = sc = new ScoreBufferCollector(docSetHitCollector);
		if (params.isDistanceRequired)
			last = distanceCollector = new DistanceCollector(
					docSetHitCollector, params.reader, params.geoParameters);
		else
			distanceCollector = null;
		if (params.advancedScore != null && !params.advancedScore.isEmpty()) {
			last = sc = new ScoreBufferAdvancedCollector(params.reader,
					params.advancedScore, docSetHitCollector, sc,
					distanceCollector);
		}
		if (params.isDocIdRequired)
			last = docIdBufferCollector = new DocIdBufferCollector(
					docSetHitCollector);
		else
			docIdBufferCollector = null;
		Timer t = new Timer(timer, "DocSetHits: " + params.query.toString());
		if (params.reader.numDocs() > 0)
			params.reader.search(params.query, params.filterHits,
					docSetHitCollector.collector);
		t.end(null);
		last.endCollection();
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

	final public FilterHitsCollector getFilterHitsCollector() {
		return (FilterHitsCollector) (docSetHitCollector.collector instanceof FilterHitsCollector ? docSetHitCollector.collector
				: null);
	}

}
