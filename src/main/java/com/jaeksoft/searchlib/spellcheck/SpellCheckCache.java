/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2015 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.spellcheck;

import org.apache.lucene.search.spell.SpellChecker;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.cache.LRUCache;
import com.jaeksoft.searchlib.index.ReaderLocal;

public class SpellCheckCache extends LRUCache<SpellCheckCacheItem> {

	public SpellCheckCache(int maxSize) {
		super("Spellcheck", maxSize);
	}

	public SpellChecker get(ReaderLocal reader, String fieldName)
			throws SearchLibException {
		try {
			return getAndJoin(new SpellCheckCacheItem(reader, fieldName), null)
					.getSpellChecker();
		} catch (Exception e) {
			throw new SearchLibException(e);
		}
	}

}
