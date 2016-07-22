/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2015 Emmanuel Keller / Jaeksoft
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
import org.roaringbitmap.RoaringBitmap;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.cache.LRUItemAbstract;
import com.jaeksoft.searchlib.filter.FilterHits;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.geo.GeoParameters;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.request.BoostQuery;
import com.jaeksoft.searchlib.result.collector.CollectorInterface;
import com.jaeksoft.searchlib.result.collector.docsethit.DistanceCollector;
import com.jaeksoft.searchlib.result.collector.docsethit.DocIdBufferCollector;
import com.jaeksoft.searchlib.result.collector.docsethit.DocSetHitBaseCollector;
import com.jaeksoft.searchlib.result.collector.docsethit.DocSetHitBaseCollector.FilterHitsCollector;
import com.jaeksoft.searchlib.result.collector.docsethit.DocSetHitCollectorInterface;
import com.jaeksoft.searchlib.result.collector.docsethit.ScoreBufferAdvancedCollector;
import com.jaeksoft.searchlib.result.collector.docsethit.ScoreBufferCollector;
import com.jaeksoft.searchlib.scoring.AdvancedScore;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.Timer;

public class DocSetHits extends LRUItemAbstract<DocSetHits> {

	final ReaderAbstract reader;
	final Query query;
	final String queryKey;
	final String boostQueryKey;
	final String advancedScoringKey;
	final FilterHits filterHits;
	final GeoParameters geoParameters;
	final DocSetHitBaseCollector docSetHitCollector;
	final DocIdBufferCollector docIdBufferCollector;
	final DistanceCollector distanceCollector;
	final ScoreBufferCollector scoreBufferCollector;
	final DocSetHitCollectorInterface lastCollector;

	DocSetHits(ReaderAbstract reader, AbstractSearchRequest searchRequest, FilterHits filterHits)
			throws IOException, ParseException, SyntaxError, SearchLibException {
		this.reader = reader;
		this.filterHits = filterHits;
		this.query = searchRequest.getQuery();
		this.queryKey = query == null ? null : query.toString();
		ScoreBufferCollector sc = null;
		DocSetHitCollectorInterface last = docSetHitCollector = new DocSetHitBaseCollector(reader.maxDoc(),
				searchRequest.isForFilter());
		if (searchRequest.isScoreRequired())
			last = sc = new ScoreBufferCollector(docSetHitCollector);
		if (searchRequest.isDistanceRequired()) {
			geoParameters = searchRequest.getGeoParameters();
			last = distanceCollector = new DistanceCollector(docSetHitCollector, reader, geoParameters);
		} else {
			distanceCollector = null;
			geoParameters = null;
		}
		AdvancedScore advancedScore = searchRequest.getAdvancedScore();
		if (advancedScore != null && !advancedScore.isEmpty()) {
			last = sc = new ScoreBufferAdvancedCollector(reader, advancedScore, docSetHitCollector, sc,
					distanceCollector);
		}
		advancedScoringKey = AdvancedScore.getCacheKey(advancedScore);
		if (searchRequest.isDocIdRequired())
			last = docIdBufferCollector = new DocIdBufferCollector(docSetHitCollector);
		else
			docIdBufferCollector = null;
		boostQueryKey = BoostQuery.getCacheKey(searchRequest.getBoostingQueries());
		lastCollector = last;
		scoreBufferCollector = sc;
	}

	@Override
	protected void populate(Timer timer) throws IOException, ParseException, SyntaxError, SearchLibException {
		Timer t = (timer == null) ? null : new Timer(timer, "DocSetHits: " + queryKey);
		if (reader.numDocs() > 0)
			reader.search(query, filterHits, docSetHitCollector.collector);
		if (t != null)
			t.end(null);
		lastCollector.endCollection();
	}

	final public int getNumFound() {
		if (docSetHitCollector == null)
			return 0;
		return docSetHitCollector.getSize();
	}

	final public RoaringBitmap getBitSet() {
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

	final public <T extends CollectorInterface> T getCollector(Class<T> collectorType) {
		return lastCollector.getCollector(collectorType);
	}

	final public FilterHitsCollector getFilterHitsCollector() {
		return (FilterHitsCollector) (docSetHitCollector.collector instanceof FilterHitsCollector
				? docSetHitCollector.collector : null);
	}

	final public static int compare(CollectorInterface c1, CollectorInterface c2) {
		if (c1 == null)
			if (c2 == null)
				return 0;
			else
				return -1;
		if (c2 == null)
			return 1;
		return c2.getClassType() - c1.getClassType();
	}

	@Override
	public int compareTo(DocSetHits dsh) {
		int c;
		if ((c = compare(docSetHitCollector, dsh.docSetHitCollector)) != 0)
			return c;
		if ((c = compare(docIdBufferCollector, dsh.docIdBufferCollector)) != 0)
			return c;
		if ((c = compare(distanceCollector, dsh.distanceCollector)) != 0)
			return c;
		if ((c = compare(scoreBufferCollector, dsh.scoreBufferCollector)) != 0)
			return c;
		if ((c = StringUtils.compareNullString(queryKey, dsh.queryKey)) != 0)
			return c;
		if ((c = GeoParameters.compare(geoParameters, dsh.geoParameters)) != 0)
			return c;
		if ((c = StringUtils.compareNullString(boostQueryKey, dsh.boostQueryKey)) != 0)
			return c;
		if ((c = StringUtils.compareNullString(advancedScoringKey, dsh.advancedScoringKey)) != 0)
			return c;
		return StringUtils.compareNullHashCode(filterHits, dsh.filterHits);
	}
}
