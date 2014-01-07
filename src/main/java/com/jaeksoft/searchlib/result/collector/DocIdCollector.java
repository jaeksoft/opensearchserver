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

import java.io.IOException;

import org.apache.commons.lang.ArrayUtils;
import org.apache.lucene.util.OpenBitSet;

class DocIdCollector extends AbstractCollector implements DocIdInterface {

	private final int[] ids;
	private int currentPos;
	private OpenBitSet bitSet;

	DocIdCollector(int maxDoc, int maxSize) {
		super(null, maxDoc);
		ids = new int[maxSize];
		currentPos = 0;
		bitSet = new OpenBitSet(maxDoc);
	}

	private DocIdCollector(DocIdCollector source) {
		super(source.parent, source.maxDoc);
		this.ids = ArrayUtils.clone(source.ids);
		this.currentPos = source.currentPos;
		this.bitSet = (OpenBitSet) source.bitSet.clone();
	}

	@Override
	public DocIdInterface duplicate() {
		return new DocIdCollector(this);
	}

	public void collectDoc(int doc) throws IOException {
		ids[currentPos++] = doc;
		bitSet.fastSet(doc);
	}

	@Override
	public void swap(int a, int b) {
		int v1 = ids[a];
		int v2 = ids[b];
		ids[a] = v2;
		ids[b] = v1;
	}

	@Override
	public int[] getIds() {
		return ids;
	}

	@Override
	public int getSize() {
		return currentPos;
	}

	@Override
	public OpenBitSet getBitSet() {
		return bitSet;
	}

}
