/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013 Emmanuel Keller / Jaeksoft
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

import org.apache.commons.codec.language.bm.NameType;
import org.apache.commons.codec.language.bm.PhoneticEngine;
import org.apache.commons.codec.language.bm.RuleType;

import com.jaeksoft.searchlib.cache.LRUCache;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.StringUtils;

public class BeiderMorseCache extends
		LRUCache<BeiderMorseCache.TermKey, String[]> {

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

	public static class TermKey implements Comparable<TermKey> {

		final public EncoderKey encoderKey;
		final public String term;

		public TermKey(EncoderKey encoderKey, final String term) {
			this.encoderKey = encoderKey;
			this.term = term;
		}

		@Override
		public int compareTo(TermKey key) {
			int c;
			if ((c = encoderKey.compareTo(key.encoderKey)) != 0)
				return c;
			return StringUtils.compareNullString(term, key.term);
		}
	}

	private final TreeMap<EncoderKey, PhoneticEngine> encoders = new TreeMap<EncoderKey, PhoneticEngine>();

	private final ReadWriteLock encodersLock = new ReadWriteLock();

	public PhoneticEngine getEncoder(EncoderKey encoderKey) {
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
		super(null, 10000);
	}

	private final String[] get(final TermKey termKey) {
		String[] tokens = getAndPromote(termKey);
		if (tokens != null)
			return tokens;
		PhoneticEngine encoder = getEncoder(termKey.encoderKey);
		String terms = null;
		synchronized (encoder) {
			terms = encoder.encode(termKey.term);
		}
		if (terms == null)
			return null;
		put(termKey, StringUtils.split(terms, '|'));
		return tokens;
	}

	private final static BeiderMorseCache INSTANCE = new BeiderMorseCache();

	public final static String[] get(EncoderKey encoderKey, String term) {
		return INSTANCE.get(new TermKey(encoderKey, term));
	}
}
