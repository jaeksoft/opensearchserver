/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2010 Emmanuel Keller / Jaeksoft
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

import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.OpenBitSet;

import com.jaeksoft.searchlib.result.ResultScoreDoc;
import com.jaeksoft.searchlib.result.collector.DocIdCollector;
import com.jaeksoft.searchlib.result.collector.MaxScoreCollector;
import com.jaeksoft.searchlib.result.collector.NumFoundCollector;
import com.jaeksoft.searchlib.result.collector.ResultScoreDocCollector;
import com.jaeksoft.searchlib.result.collector.ResultScoreDocPriorityCollector;
import com.jaeksoft.searchlib.sort.SorterAbstract;
import com.jaeksoft.searchlib.util.ReadWriteLock;

public class DocSetHits {

	final private ReadWriteLock rwl = new ReadWriteLock();

	private ReaderLocal reader;
	private Query query;
	private Filter filter;
	private SorterAbstract sort;
	private NumFoundCollector numFoundCollector;
	private MaxScoreCollector maxScoreCollector;
	private DocIdCollector docIdCollector;
	private ResultScoreDocCollector resultScoreDocCollector;
	private ResultScoreDocPriorityCollector resultScoreDocPriorityCollector;

	protected DocSetHits(ReaderLocal reader, Query query, Filter filter,
			SorterAbstract sort) throws IOException {
		rwl.w.lock();
		try {
			this.query = query;
			this.filter = filter;
			this.reader = reader;
			this.sort = sort;
			docIdCollector = null;
			maxScoreCollector = null;
			resultScoreDocCollector = null;
			resultScoreDocPriorityCollector = null;
			numFoundCollector = new NumFoundCollector();
			if (reader.numDocs() == 0)
				return;
			reader.search(query, filter, numFoundCollector);

		} finally {
			rwl.w.unlock();
		}
	}

	public ResultScoreDoc[] getPriorityDocs(int rows) throws IOException {
		rwl.r.lock();
		try {
			if (rows == 0)
				return ResultScoreDoc.EMPTY_ARRAY;
			int numFound = numFoundCollector.getNumFound();
			if (rows > numFound)
				rows = numFound;
			if (resultScoreDocPriorityCollector != null) {
				if (resultScoreDocPriorityCollector.match(rows, sort))
					return resultScoreDocPriorityCollector.getDocs();
			}
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			resultScoreDocPriorityCollector = new ResultScoreDocPriorityCollector(
					rows, sort);
			reader.search(query, filter, resultScoreDocPriorityCollector);
			return resultScoreDocPriorityCollector.getDocs();
		} finally {
			rwl.w.unlock();
		}
	}

	public ResultScoreDoc[] getAllDocs() throws IOException {
		rwl.r.lock();
		try {
			if (resultScoreDocCollector != null)
				return resultScoreDocCollector.getDocs();
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			if (resultScoreDocCollector == null) {
				resultScoreDocCollector = new ResultScoreDocCollector(
						numFoundCollector.getNumFound());
				reader.search(query, filter, resultScoreDocCollector);
			}
			return resultScoreDocCollector.getDocs();
		} finally {
			rwl.w.unlock();
		}

	}

	public float getMaxScore() throws IOException {
		rwl.r.lock();
		try {
			if (resultScoreDocCollector != null)
				return resultScoreDocCollector.getMaxScore();
			if (maxScoreCollector != null)
				return maxScoreCollector.getMaxScore();
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			if (maxScoreCollector != null)
				return maxScoreCollector.getMaxScore();
			maxScoreCollector = new MaxScoreCollector();
			reader.search(query, filter, maxScoreCollector);
			return maxScoreCollector.getMaxScore();
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

	private DocIdCollector getDocIdCollector() throws IOException {
		if (docIdCollector == null) {
			docIdCollector = new DocIdCollector(reader.maxDoc(),
					numFoundCollector.getNumFound());
			reader.search(query, filter, docIdCollector);
		}
		return docIdCollector;
	}

	public OpenBitSet getBitSet() throws IOException {
		rwl.r.lock();
		try {
			if (docIdCollector != null)
				return docIdCollector.getBitSet();
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			return getDocIdCollector().getBitSet();
		} finally {
			rwl.w.unlock();
		}
	}

	public int[] getDocs() throws IOException {
		rwl.r.lock();
		try {
			if (docIdCollector != null)
				return docIdCollector.getDocs();
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			return getDocIdCollector().getDocs();
		} finally {
			rwl.w.unlock();
		}
	}

}
