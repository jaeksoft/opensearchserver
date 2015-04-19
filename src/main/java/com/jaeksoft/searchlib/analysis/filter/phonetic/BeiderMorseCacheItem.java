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

package com.jaeksoft.searchlib.analysis.filter.phonetic;

import org.apache.commons.codec.language.bm.PhoneticEngine;

import com.jaeksoft.searchlib.analysis.filter.phonetic.BeiderMorseCache.EncoderKey;
import com.jaeksoft.searchlib.cache.LRUItemAbstract;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.Timer;

public class BeiderMorseCacheItem extends LRUItemAbstract<BeiderMorseCacheItem> {

	final private EncoderKey encoderKey;
	final private String term;
	String[] tokens;

	BeiderMorseCacheItem(final EncoderKey encoderKey, final String term) {
		this.encoderKey = encoderKey;
		this.term = term;
		tokens = null;
	}

	@Override
	public int compareTo(BeiderMorseCacheItem key) {
		int c;
		if ((c = encoderKey.compareTo(key.encoderKey)) != 0)
			return c;
		return StringUtils.compareNullString(term, key.term);
	}

	@Override
	protected void populate(Timer timer) throws Exception {
		PhoneticEngine encoder = BeiderMorseCache.INSTANCE
				.getEncoder(encoderKey);
		String terms = null;
		synchronized (encoder) {
			terms = encoder.encode(term);
		}
		if (terms == null)
			return;
		tokens = StringUtils.split(terms, '|');
	}

}
