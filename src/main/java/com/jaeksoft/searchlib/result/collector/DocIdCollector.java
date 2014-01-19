/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.result.collector;

import it.unimi.dsi.fastutil.Swapper;

import java.io.IOException;

import org.apache.commons.lang.ArrayUtils;
import org.apache.lucene.util.OpenBitSet;

public class DocIdCollector implements DocIdInterface, Swapper {

	private final int maxDoc;
	private final int[] ids;
	private int currentPos;
	private OpenBitSet bitSet;

	public DocIdCollector(final int maxDoc, final int maxSize) {
		this.maxDoc = maxDoc;
		ids = new int[maxSize];
		currentPos = 0;
		bitSet = new OpenBitSet(maxDoc);
	}

	private DocIdCollector(final DocIdCollector source) {
		this.maxDoc = source.maxDoc;
		this.ids = ArrayUtils.clone(source.ids);
		this.currentPos = source.currentPos;
		this.bitSet = (OpenBitSet) source.bitSet.clone();
	}

	@Override
	final public CollectorInterface duplicate() {
		return new DocIdCollector(this);
	}

	@Override
	final public DocIdCollector duplicate(final AbstractBaseCollector<?> base) {
		return new DocIdCollector(this);
	}

	final public void collectDoc(final int doc) throws IOException {
		ids[currentPos++] = doc;
		bitSet.fastSet(doc);
	}

	@Override
	final public void swap(final int a, final int b) {
		int v1 = ids[a];
		int v2 = ids[b];
		ids[a] = v2;
		ids[b] = v1;
	}

	@Override
	final public int[] getIds() {
		return ids;
	}

	@Override
	final public int getSize() {
		return currentPos;
	}

	@Override
	final public OpenBitSet getBitSet() {
		return bitSet;
	}

	@Override
	final public int getMaxDoc() {
		return maxDoc;
	}

	@Override
	public <T extends CollectorInterface> T getCollector(
			final Class<T> collectorType) {
		return null;
	}

	@Override
	public CollectorInterface getParent() {
		return null;
	}

}
