/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2015 Emmanuel Keller / Jaeksoft
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

import java.io.IOException;

import org.apache.commons.lang3.ArrayUtils;
import org.roaringbitmap.RoaringBitmap;

import it.unimi.dsi.fastutil.Swapper;

public class DocIdCollector implements DocIdInterface, Swapper {

	private final int maxDoc;
	private final int[] ids;
	private int currentPos;
	private RoaringBitmap bitSet;
	private final int classType = getClass().hashCode();

	public DocIdCollector(final int maxDoc, final int maxSize) {
		this.maxDoc = maxDoc;
		ids = new int[maxSize];
		currentPos = 0;
		bitSet = new RoaringBitmap();
	}

	private DocIdCollector(final DocIdCollector source) {
		this.maxDoc = source.maxDoc;
		this.ids = ArrayUtils.clone(source.ids);
		this.currentPos = source.currentPos;
		this.bitSet = source.bitSet.clone();
	}

	@Override
	public final int getClassType() {
		return classType;
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
		if (doc == -1)
			return;
		ids[currentPos++] = doc;
		bitSet.add(doc);
	}

	@Override
	final public void swap(final int a, final int b) {
		int v1 = ids[a];
		int v2 = ids[b];
		ids[a] = v2;
		ids[b] = v1;
	}

	@Override
	final public void doSwap(final int a, final int b) {
		swap(a, b);
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
	final public RoaringBitmap getBitSet() {
		return bitSet;
	}

	@Override
	final public int getMaxDoc() {
		return maxDoc;
	}

	@Override
	public <T extends CollectorInterface> T getCollector(final Class<T> collectorType) {
		return null;
	}

	@Override
	public CollectorInterface getParent() {
		return null;
	}

}
