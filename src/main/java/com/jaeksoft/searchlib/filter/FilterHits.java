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

package com.jaeksoft.searchlib.filter;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.OpenBitSet;

public class FilterHits extends Filter {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7434283983275758714L;

	protected OpenBitSet docSet;

	FilterHits() {
		docSet = null;
	}

	public FilterHits(OpenBitSet docSet) {
		this.docSet = docSet;
	}

	public void and(OpenBitSet bitSet) {
		if (docSet == null)
			docSet = (OpenBitSet) bitSet.clone();
		else
			docSet.and(bitSet);
	}

	@Override
	final public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
		return docSet;
	}

}
