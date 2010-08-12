/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.parser.torrent;

import java.io.IOException;
import java.io.InputStream;
import java.util.TreeMap;

public class BDictionary extends BValue {

	private TreeMap<String, BValue> treeMap;

	public BDictionary(InputStream input) throws IOException {
		treeMap = new TreeMap<String, BValue>();
		byteArray.write('d');
		for (;;) {
			BValue v = BValue.next(input);
			if (v instanceof BEnd)
				break;
			if (!(v instanceof BString))
				throw new BException();
			byteArray.write(v.byteArray.toByteArray());
			String key = ((BString) v).getString();
			v = BValue.next(input);
			if (v == null || v instanceof BEnd)
				throw new BException();
			byteArray.write(v.byteArray.toByteArray());
			treeMap.put(key, v);
		}
		byteArray.write('e');
	}

	public BValue get(String key) {
		return treeMap.get(key);
	}
}
