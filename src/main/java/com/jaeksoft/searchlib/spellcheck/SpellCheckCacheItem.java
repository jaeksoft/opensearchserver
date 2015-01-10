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

import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import com.jaeksoft.searchlib.cache.LRUItemAbstract;
import com.jaeksoft.searchlib.index.ReaderLocal;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.Timer;

public class SpellCheckCacheItem extends LRUItemAbstract<SpellCheckCacheItem> {

	private SpellChecker spellChecker = null;

	private final ReaderLocal reader;
	private final String field;

	public SpellCheckCacheItem(ReaderLocal reader, String field) {
		this.reader = reader;
		this.field = field;
	}

	@Override
	public int compareTo(SpellCheckCacheItem i) {
		return StringUtils.compareNullString(field, i.field);
	}

	@Override
	protected void populate(Timer timer) throws Exception {
		LuceneDictionary dict = reader.getLuceneDirectionary(field);
		spellChecker = new SpellChecker(new RAMDirectory());
		spellChecker.indexDictionary(dict, new IndexWriterConfig(
				Version.LUCENE_36, null), true);
	}

	public SpellChecker getSpellChecker() {
		return spellChecker;
	}
}
