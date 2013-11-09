/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.parser.torrent;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class BList extends BValue {

	private List<BValue> list;

	public BList(InputStream input) throws IOException {
		list = new ArrayList<BValue>();
		byteArray.write('l');
		for (;;) {
			BValue value = BValue.next(input);
			if (value == null)
				throw new BException();
			if (value instanceof BEnd)
				break;
			list.add(value);
			byteArray.write(value.byteArray.toByteArray());
		}
		byteArray.write('e');
	}

	public BValue get(int index) {
		return list.get(index);
	}

	public int size() {
		return list.size();
	}

	public String getFilePath() throws BException {
		StringBuilder sb = new StringBuilder();
		for (BValue v : list) {
			if (sb.length() != 0)
				sb.append('/');
			if (v == null)
				throw new BException();
			if (!(v instanceof BString))
				throw new BException();
			sb.append(((BString) v).getString());
		}
		return sb.toString();
	}
}
