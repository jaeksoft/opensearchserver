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

package com.jaeksoft.searchlib.util;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.atlascopco.hunspell.Hunspell;

public class HunspellUtils {

	private final static ReadWriteLock rwl = new ReadWriteLock();

	private final static Map<String, Api> bridjMap = new TreeMap<String, Api>();

	/**
	 * Return an Hunspell directory.
	 * 
	 * @param dict_path
	 * @return
	 */
	final public static Api getBridj(String dict_path) {
		rwl.r.lock();
		try {
			Api api = bridjMap.get(dict_path);
			if (api != null)
				return api;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			Api api = bridjMap.get(dict_path);
			if (api != null)
				return api;
			api = new BridJApi(dict_path);
			bridjMap.put(dict_path, api);
			return api;
		} finally {
			rwl.w.unlock();
		}
	}

	public interface Api {

		public boolean isCorrect(String word);

		public List<String> stem(String word);

		public List<String> suggest(String word);
	}

	private static class BridJApi implements Api {

		private final Hunspell hunspell;

		private BridJApi(String dict_path) {
			hunspell = new Hunspell(dict_path + ".dic", dict_path + ".aff");
		}

		@Override
		public boolean isCorrect(String word) {
			synchronized (hunspell) {
				return hunspell.isCorrect(word);
			}
		}

		@Override
		public List<String> stem(String word) {
			synchronized (hunspell) {
				return hunspell.stem(word);
			}
		}

		@Override
		public List<String> suggest(String word) {
			synchronized (hunspell) {
				return hunspell.suggest(word);
			}
		}
	}

	private final static void test(Api api) {
		long t = System.currentTimeMillis();
		final String[] WORDS = { "test", "la", "les", "ou", "quand", "comment",
				"pourquoi", "si", "il", "faut" };
		for (String word : WORDS) {
			System.out.print(word + ": ");
			for (String suggest : api.suggest(word))
				System.out.print(suggest + ",");
			System.out.println();
		}
		Runtime.getRuntime().gc();
		System.out.println(api.getClass().getSimpleName() + " - Time: "
				+ (System.currentTimeMillis() - t) + " Memory: "
				+ Runtime.getRuntime().freeMemory());
	}

	public final static void main(String[] args) throws IOException {
		final String dict_path = "/var/local/dict/fr-toutesvariantes";
		for (int i = 0; i < 5; i++) {
			test(getBridj(dict_path));
		}
	}
}
