/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.index;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;

public class DocSetHits {

	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock(true);
	private final Lock r = rwl.readLock();
	private final Lock w = rwl.writeLock();

	private int[] collectedDocs;
	private ReaderLocal reader;
	private Query query;
	private Filter filter;
	private Sort sort;
	private ScoreDoc[] scoreDocs;
	private int docNumFound;
	private float maxScore;

	private class ScoreHitCollector extends Collector {
		@Override
		public void collect(int docId) {
			collectedDocs[docNumFound++] = docId;
		}

		@Override
		public boolean acceptsDocsOutOfOrder() {
			return true;
		}

		@Override
		public void setNextReader(IndexReader reader, int docId)
				throws IOException {
		}

		@Override
		public void setScorer(Scorer scorer) throws IOException {
			float sc = scorer.score();
			if (sc > maxScore)
				maxScore = sc;

		}
	}

	private class DeleteHitCollector extends ScoreHitCollector {
		@Override
		public void collect(int docId) {
			super.collect(docId);
			try {
				reader.fastDeleteDocument(docId);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	protected DocSetHits(ReaderLocal reader, Query query, Filter filter,
			Sort sort, boolean delete, boolean collect) throws IOException {
		w.lock();
		try {
			this.query = query;
			this.filter = filter;
			this.sort = sort;
			this.docNumFound = 0;
			this.maxScore = 0;
			this.scoreDocs = new ScoreDoc[0];
			this.reader = reader;
			this.collectedDocs = new int[0];
			Collector collector = null;
			if (reader.numDocs() == 0)
				return;
			if (delete)
				collector = new DeleteHitCollector();
			else if (collect)
				collector = new ScoreHitCollector();

			TopDocs topDocs = reader.search(query, filter, sort, 1);
			if (collector != null) {
				this.collectedDocs = new int[topDocs.totalHits];
				reader.search(query, filter, collector);
			} else {
				docNumFound = topDocs.totalHits;
				maxScore = topDocs.getMaxScore();
			}
		} finally {
			w.unlock();
		}
	}

	public ScoreDoc[] getScoreDocs(int rows) throws IOException {
		w.lock();
		try {
			if (rows > docNumFound)
				rows = docNumFound;
			if (rows <= scoreDocs.length)
				return scoreDocs;
			TopDocs topDocs = reader.search(query, filter, sort, rows);
			this.scoreDocs = topDocs.scoreDocs;
			return scoreDocs;
		} finally {
			w.unlock();
		}
	}

	public int getDocByPos(int pos) {
		r.lock();
		try {
			return scoreDocs[pos].doc;
		} finally {
			r.unlock();
		}
	}

	public float getScoreByPos(int pos) {
		r.lock();
		try {
			return scoreDocs[pos].score;
		} finally {
			r.unlock();
		}
	}

	public boolean contains(int docId) {
		r.lock();
		try {
			for (int id : collectedDocs)
				if (id == docId)
					return true;
			return false;
		} finally {
			r.unlock();
		}
	}

	public int[] getCollectedDocs() {
		r.lock();
		try {
			return collectedDocs;
		} finally {
			r.unlock();
		}
	}

	public float getMaxScore() {
		r.lock();
		try {
			return maxScore;
		} finally {
			r.unlock();
		}
	}

	public int getDocNumFound() {
		r.lock();
		try {
			return docNumFound;
		} finally {
			r.unlock();
		}
	}

}
