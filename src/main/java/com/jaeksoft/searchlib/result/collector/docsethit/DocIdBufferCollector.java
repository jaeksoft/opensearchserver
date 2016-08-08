/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2014-2015 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.result.collector.docsethit;

import java.io.IOException;

import org.apache.commons.lang3.ArrayUtils;
import org.roaringbitmap.RoaringBitmap;

import com.jaeksoft.searchlib.result.collector.AbstractBaseCollector;
import com.jaeksoft.searchlib.result.collector.AbstractExtendsCollector;
import com.jaeksoft.searchlib.result.collector.CollectorInterface;
import com.jaeksoft.searchlib.result.collector.DocIdInterface;
import com.jaeksoft.searchlib.util.array.IntBufferedArrayFactory;
import com.jaeksoft.searchlib.util.array.IntBufferedArrayInterface;

public class DocIdBufferCollector
		extends
		AbstractExtendsCollector<DocSetHitCollectorInterface, DocSetHitBaseCollector>
		implements DocSetHitCollectorInterface, DocIdInterface {

	final private IntBufferedArrayInterface idsBuffer;
	private int[] ids;
	private RoaringBitmap bitSet;

	public DocIdBufferCollector(final DocSetHitBaseCollector base) {
		super(base);
		bitSet = new RoaringBitmap();
		idsBuffer = IntBufferedArrayFactory.INSTANCE.newInstance(base
				.getMaxDoc());
		ids = null;
	}

	private DocIdBufferCollector(final DocSetHitBaseCollector base,
			final DocIdBufferCollector source) {
		super(base);
		this.idsBuffer = null;
		this.ids = ArrayUtils.clone(source.ids);
		this.bitSet = source.bitSet.clone();
	}

	@Override
	public CollectorInterface duplicate(final AbstractBaseCollector<?> base) {
		parent.duplicate(base);
		return new DocIdBufferCollector((DocSetHitBaseCollector) base, this);
	}

	@Override
	final public void collectDoc(final int docId) throws IOException {
		parent.collectDoc(docId);
		idsBuffer.add(docId);
		bitSet.add(docId);
	}

	@Override
	final public void doSwap(final int a, final int b) {
		parent.doSwap(a, b);
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
	final public int[] getIds() {
		return ids;
	}

	@Override
	final public RoaringBitmap getBitSet() {
		return bitSet;
	}

	@Override
	final public int getMaxDoc() {
		return base.getMaxDoc();
	}

	@Override
	final public int getSize() {
		return base.getSize();
	}

}