/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2011 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.cache;

import java.io.IOException;

import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.RAMDirectory;

import com.jaeksoft.searchlib.index.ReaderLocal;

public class SpellCheckerCache extends LRUCache<FieldNameKey, SpellChecker> {

	public SpellCheckerCache(int maxSize) {
		super("Spellchecker cache", maxSize);
	}

	public SpellChecker get(ReaderLocal reader, String field)
			throws IOException {
		rwl.w.lock();
		try {
			FieldNameKey key = new FieldNameKey(field);
			SpellChecker spellChecker = getAndPromote(key);
			if (spellChecker != null)
				return spellChecker;
			LuceneDictionary dict = reader.getLuceneDirectionary(key
					.getFieldName());
			SpellChecker spellchecker = new SpellChecker(new RAMDirectory());
			spellchecker.indexDictionary(dict);

			put(key, spellchecker);
			return spellchecker;
		} finally {
			rwl.w.unlock();
		}
	}

}
