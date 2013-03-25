/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
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

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.OpenBitSet;

import com.jaeksoft.searchlib.result.collector.DocIdCollector;
import com.jaeksoft.searchlib.result.collector.DocIdInterface;
import com.jaeksoft.searchlib.result.collector.MaxScoreCollector;
import com.jaeksoft.searchlib.result.collector.NumFoundCollector;
import com.jaeksoft.searchlib.result.collector.ScoreDocCollector;
import com.jaeksoft.searchlib.sort.SortFieldList;
import com.jaeksoft.searchlib.sort.SorterAbstract;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.Timer;

public class DocSetHits {

	final private ReadWriteLock rwl = new ReadWriteLock();

	private ReaderLocal reader;
	private Query query;
	private Filter filter;
	private SortFieldList sortFieldList;
	private NumFoundCollector numFoundCollector;
	private MaxScoreCollector maxScoreCollector;
	private DocIdCollector docIdCollector;
	private ScoreDocCollector scoreDocCollector;

	private class FilterBitSet extends Filter {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1626629515659451528L;

		private OpenBitSet filterBitSet;

		public FilterBitSet(OpenBitSet filterBitSet) {
			this.filterBitSet = filterBitSet;
		}

		@Override
		public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
			return filterBitSet;
		}

	}

	protected DocSetHits(ReaderLocal reader, Query query,
			OpenBitSet filterBitSet, SortFieldList sortFieldList, Timer timer)
			throws IOException {
		rwl.w.lock();
		try {
			this.query = query;
			this.filter = filterBitSet != null ? new FilterBitSet(filterBitSet)
					: null;
			this.reader = reader;
			this.sortFieldList = sortFieldList;
			docIdCollector = null;
			maxScoreCollector = null;
			scoreDocCollector = null;
			numFoundCollector = new NumFoundCollector();
			if (reader.numDocs() == 0)
				return;
			Timer t = new Timer(timer, "DocSetHits: " + query.toString());
			reader.search(query, filter, numFoundCollector);
			t.getDuration();

		} finally {
			rwl.w.unlock();
		}
	}

	// public ResultScoreDocPriorityCollector getPriorityDocs(int rows, Timer
	// timer)
	// throws IOException {
	// rwl.r.lock();
	// try {
	// int numFound = numFoundCollector.getNumFound();
	// if (rows > numFound)
	// rows = numFound;
	// if (rows == 0)
	// return ScoreDocCollector.EMPTY;
	// if (resultScoreDocPriorityCollector != null) {
	// if (resultScoreDocPriorityCollector.match(rows, sort))
	// return ScoreDocPriorityCollector.getDocs(timer);
	// }
	// } finally {
	// rwl.r.unlock();
	// }
	// rwl.w.lock();
	// try {
	// Timer t = new Timer(timer, "Get priority docs: " + rows);
	// ScoreDocCollector rsdc = getAllDocsNoLock(t);
	// resultScoreDocPriorityCollector = new ResultScoreDocPriorityCollector(
	// rows, sort, resultScoreDocPriorityCollector);
	// resultScoreDocPriorityCollector.collect(rsdc);
	// t.duration();
	// return resultScoreDocPriorityCollector;
	// } finally {
	// rwl.w.unlock();
	// }
	// }

	private void sort(DocIdInterface collector, Timer timer) throws IOException {
		if (sortFieldList == null)
			return;
		SorterAbstract sorter = sortFieldList.getSorter(collector, reader);
		sorter.quickSort(timer);
	}

	private ScoreDocCollector getScoreDocCollectorNoLock(Timer timer)
			throws IOException {
		if (scoreDocCollector != null)
			return scoreDocCollector;
		Timer tAllDocs = new Timer(timer, "Get Score Doc Collector ");
		Timer t = new Timer(tAllDocs, "Collection ");
		scoreDocCollector = new ScoreDocCollector(reader.maxDoc(),
				numFoundCollector.getNumFound());
		reader.search(query, filter, scoreDocCollector);
		t.end(t.getInfo() + scoreDocCollector.getSize());
		sort(scoreDocCollector, tAllDocs);
		tAllDocs.end(tAllDocs.getInfo() + scoreDocCollector.getSize());
		docIdCollector = scoreDocCollector;
		return scoreDocCollector;
	}

	public ScoreDocCollector getScoreDocCollector(Timer timer)
			throws IOException {
		rwl.r.lock();
		try {
			if (scoreDocCollector != null)
				return scoreDocCollector;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			if (scoreDocCollector != null)
				return scoreDocCollector;
			return getScoreDocCollectorNoLock(timer);
		} finally {
			rwl.w.unlock();
		}

	}

	private MaxScoreCollector getMaxScoreCollectorNoLock(Timer timer)
			throws IOException {
		if (maxScoreCollector != null)
			return maxScoreCollector;
		Timer t = new Timer(timer, "Get Max Score Collector ");
		maxScoreCollector = new MaxScoreCollector();
		reader.search(query, filter, maxScoreCollector);
		t.end(t.getInfo() + maxScoreCollector.getMaxScore());
		return maxScoreCollector;
	}

	public float getMaxScore(Timer timer) throws IOException {
		rwl.r.lock();
		try {
			if (scoreDocCollector != null)
				return scoreDocCollector.getMaxScore();
			if (maxScoreCollector != null)
				return maxScoreCollector.getMaxScore();
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			if (scoreDocCollector != null)
				return scoreDocCollector.getMaxScore();
			if (maxScoreCollector != null)
				return maxScoreCollector.getMaxScore();
			return getMaxScoreCollectorNoLock(timer).getMaxScore();
		} finally {
			rwl.w.unlock();
		}
	}

	public int getDocNumFound() {
		rwl.r.lock();
		try {
			return numFoundCollector.getNumFound();
		} finally {
			rwl.r.unlock();
		}
	}

	private DocIdCollector getDocIdCollectorNoLock(Timer timer)
			throws IOException {
		if (docIdCollector != null)
			return docIdCollector;
		Timer tAllDocs = new Timer(timer, "Get Doc Id Collector: ");
		Timer t = new Timer(tAllDocs, "Collection ");
		docIdCollector = new DocIdCollector(reader.maxDoc(),
				numFoundCollector.getNumFound());
		reader.search(query, filter, docIdCollector);
		t.end(t.getInfo() + docIdCollector.getSize());
		sort(docIdCollector, tAllDocs);
		tAllDocs.end(tAllDocs.getInfo() + docIdCollector.getSize());

		return docIdCollector;
	}

	public DocIdInterface getDocIdInterface(Timer timer) throws IOException {
		rwl.r.lock();
		try {
			if (docIdCollector != null)
				return docIdCollector;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			if (docIdCollector != null)
				return docIdCollector;
			if (sortFieldList != null && sortFieldList.isScore())
				return getScoreDocCollectorNoLock(timer);
			return getDocIdCollectorNoLock(timer);
		} finally {
			rwl.w.unlock();
		}
	}

	public int[] getIds(Timer timer) throws IOException {
		return getDocIdInterface(timer).getIds();
	}

	public OpenBitSet getBitSet(Timer timer) throws IOException {
		rwl.r.lock();
		try {
			if (docIdCollector != null)
				if (docIdCollector.isBitSet())
					return docIdCollector.getBitSet();
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			if (docIdCollector != null)
				if (docIdCollector.isBitSet())
					return docIdCollector.getBitSet();
			return getDocIdCollectorNoLock(timer).getBitSet();
		} finally {
			rwl.w.unlock();
		}
	}

}
