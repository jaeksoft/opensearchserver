/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013-2015 Emmanuel Keller / Jaeksoft
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

import java.util.TreeMap;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.language.bm.NameType;
import org.apache.commons.codec.language.bm.PhoneticEngine;
import org.apache.commons.codec.language.bm.RuleType;

import com.jaeksoft.searchlib.cache.LRUCache;
import com.jaeksoft.searchlib.util.ExceptionUtils;
import com.jaeksoft.searchlib.util.ReadWriteLock;

public class BeiderMorseCache extends LRUCache<BeiderMorseCacheItem> {

	final static BeiderMorseCache INSTANCE = new BeiderMorseCache();

	public static class EncoderKey implements Comparable<EncoderKey> {

		final public RuleType type;
		final public int maxPhonemes;

		public EncoderKey(final RuleType type, final int maxPhonemes) {
			this.type = type;
			this.maxPhonemes = maxPhonemes;
		}

		@Override
		public int compareTo(EncoderKey key) {
			int c;
			if ((c = type.ordinal() - key.type.ordinal()) != 0)
				return c;
			return maxPhonemes - key.maxPhonemes;
		}
	}

	private final TreeMap<EncoderKey, PhoneticEngine> encoders = new TreeMap<EncoderKey, PhoneticEngine>();

	private final ReadWriteLock encodersLock = new ReadWriteLock();

	PhoneticEngine getEncoder(EncoderKey encoderKey) {
		PhoneticEngine encoder;
		encodersLock.r.lock();
		try {
			if ((encoder = encoders.get(encoderKey)) != null)
				return encoder;
		} finally {
			encodersLock.r.unlock();
		}
		encodersLock.w.lock();
		try {
			if ((encoder = encoders.get(encoderKey)) != null)
				return encoder;
			encoder = new PhoneticEngine(NameType.GENERIC, encoderKey.type,
					true, encoderKey.maxPhonemes);
			encoders.put(encoderKey, encoder);
			return encoder;
		} finally {
			encodersLock.w.unlock();
		}
	}

	public BeiderMorseCache() {
		super("BeiderMorse", 10000);
	}

	public String[] get(EncoderKey encoderKey, String term)
			throws EncoderException {
		try {
			return getAndJoin(new BeiderMorseCacheItem(encoderKey, term), null).tokens;
		} catch (Exception e) {
			throw ExceptionUtils.<EncoderException> throwException(e,
					EncoderException.class);
		}
	}
}
