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

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Scorer;

final public class DocSetHitCollector extends Collector implements
		DocSetHitCollectorInterface {

	private final int maxDoc;
	private DocSetHitCollectorInterface collectorInterface;
	private int currentDocBase = 0;
	private Scorer scorer = null;
	private Float score;
	private int size = 0;

	public DocSetHitCollector(final int maxDoc) {
		this.maxDoc = maxDoc;
		setLastCollector(this);
	}

	final DocSetHitCollectorInterface setLastCollector(
			final DocSetHitCollectorInterface collectorInterface) {
		DocSetHitCollectorInterface old = this.collectorInterface;
		this.collectorInterface = collectorInterface;
		return old;
	}

	@Override
	final public boolean acceptsDocsOutOfOrder() {
		return true;
	}

	@Override
	final public void setNextReader(final IndexReader reader, final int docBase)
			throws IOException {
		currentDocBase = docBase;
	}

	@Override
	final public void collect(final int doc) throws IOException {
		score = null;
		collectorInterface.collectDoc(doc + currentDocBase);
	}

	@Override
	final public void collectDoc(final int doc) throws IOException {
		size++;
	}

	@Override
	final public void setScorer(Scorer scorer) throws IOException {
		this.scorer = scorer;
	}

	final public float score() throws IOException {
		if (score == null)
			score = scorer.score();
		return score;
	}

	@Override
	final public int getSize() {
		return size;
	}

	@Override
	final public int getMaxDoc() {
		return maxDoc;
	}

	@Override
	final public void swap(final int a, final int b) {
	}

	@Override
	final public void endCollection() {
	}

	@SuppressWarnings("unchecked")
	@Override
	final public <T extends CollectorInterface> T getCollector(
			Class<T> collectorType) {
		return collectorType.isInstance(this) ? (T) this : null;
	}

	@SuppressWarnings("unchecked")
	<T extends CollectorInterface> T findCollector(Class<T> collectorType) {
		DocSetHitCollectorInterface current = collectorInterface;
		while (current != null) {
			if (collectorType.isInstance(current))
				return (T) current;
			current = current.getParent();
		}
		return null;
	}

	@Override
	public DocSetHitCollectorInterface getParent() {
		return null;
	}

}
