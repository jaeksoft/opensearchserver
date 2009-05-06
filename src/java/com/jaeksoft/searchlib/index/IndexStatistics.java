/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
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

import org.apache.lucene.index.IndexReader;

public class IndexStatistics {

	private int maxDoc;
	private int numDocs;
	private boolean hasDeletions;
	private boolean isOptimized;

	protected IndexStatistics() {
		maxDoc = 0;
		numDocs = 0;
		hasDeletions = false;
		isOptimized = false;
	}

	protected IndexStatistics(IndexReader indexReader) {
		maxDoc = indexReader.maxDoc();
		numDocs = indexReader.maxDoc();
		hasDeletions = indexReader.hasDeletions();
		isOptimized = indexReader.isOptimized();
	}

	protected void add(IndexStatistics stats) {
		maxDoc += stats.maxDoc;
		numDocs += stats.numDocs;
		if (stats.hasDeletions)
			hasDeletions = stats.hasDeletions;
		if (stats.isOptimized)
			isOptimized = stats.isOptimized;
	}

	public int getMaxDoc() throws IOException {
		return maxDoc;
	}

	public int getNumDocs() {
		return numDocs;
	}

	public boolean isDeletions() {
		return hasDeletions;
	}

	public boolean isOptimized() {
		return isOptimized;
	}

}
