/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2014 Emmanuel Keller / Jaeksoft
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

import com.jaeksoft.searchlib.util.array.IntBufferedArray;

public class DocIdBufferCollector extends AbstractDocSetHitCollector implements
		DocIdInterface, DocSetHitCollectorInterface {

	final private IntBufferedArray idsBuffer;
	private int[] ids;
	private OpenBitSet bitSet;

	public DocIdBufferCollector(DocSetHitCollector base) {
		super(base);
		bitSet = new OpenBitSet(base.getMaxDoc());
		idsBuffer = new IntBufferedArray(base.getMaxDoc());
		ids = null;
	}

	private DocIdBufferCollector(DocIdBufferCollector source) {
		super(null);
		this.idsBuffer = null;
		this.ids = ArrayUtils.clone(source.ids);
		this.bitSet = (OpenBitSet) source.bitSet.clone();
	}

	@Override
	public DocIdBufferCollector duplicate() {
		return new DocIdBufferCollector(this);
	}

	@Override
	final public void collectDoc(final int docId) throws IOException {
		parent.collectDoc(docId);
		idsBuffer.add(docId);
		bitSet.fastSet(docId);
	}

	@Override
	final public void swap(final int a, final int b) {
		parent.swap(a, b);
		int i1 = ids[a];
		int i2 = ids[b];
		ids[a] = i2;
		ids[b] = i1;
	}

	@Override
	final public void endCollection() {
		parent.endCollection();
		ids = idsBuffer.getFinalArray();
	}

	@Override
	final public int getSize() {
		return ids.length;
	}

	@Override
	final public int[] getIds() {
		return ids;
	}

	@Override
	final public OpenBitSet getBitSet() {
		return bitSet;
	}

}