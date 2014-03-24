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

import org.apache.lucene.index.IndexReader;

public class IndexStatistics {

	private long maxDoc;
	private long numDocs;
	private long numDeletedDocs;
	private boolean hasDeletions;
	private boolean isOptimized;

	protected IndexStatistics() {
		maxDoc = 0;
		numDocs = 0;
		numDeletedDocs = 0;
		hasDeletions = false;
		isOptimized = false;
	}

	protected IndexStatistics(IndexReader indexReader) {
		maxDoc = indexReader.maxDoc();
		numDocs = indexReader.numDocs();
		numDeletedDocs = indexReader.numDeletedDocs();
		hasDeletions = indexReader.hasDeletions();
		isOptimized = indexReader.isOptimized();
	}

	public IndexStatistics(long maxDoc, long numDocs, long numDeletedDocs) {
		this.maxDoc = maxDoc;
		this.numDocs = numDocs;
		this.numDeletedDocs = numDeletedDocs;
		this.hasDeletions = numDeletedDocs > 0;
		this.isOptimized = true;
	}

	protected void add(IndexStatistics stats) {
		maxDoc += stats.maxDoc;
		numDocs += stats.numDocs;
		numDeletedDocs += stats.numDeletedDocs;
		if (stats.hasDeletions)
			hasDeletions = stats.hasDeletions;
		if (stats.isOptimized)
			isOptimized = stats.isOptimized;
	}

	final public long getMaxDoc() throws IOException {
		return maxDoc;
	}

	final public long getNumDocs() {
		return numDocs;
	}

	final public long getNumDeletedDocs() {
		return numDeletedDocs;
	}

	final public boolean isDeletions() {
		return hasDeletions;
	}

	final public boolean isOptimized() {
		return isOptimized;
	}

}
