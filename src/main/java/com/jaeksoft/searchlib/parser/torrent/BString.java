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

public class BString extends BValue {

	private String string;

	public BString(int i, InputStream input) throws IOException {
		string = null;
		StringBuilder length = new StringBuilder();
		if (i != 0) {
			length.append((char) i);
			byteArray.write(i);
		}
		for (;;) {
			i = input.read();
			if (i == -1)
				throw new BException();
			if (i == ':') {
				byteArray.write(i);
				break;
			}
			if (!Character.isDigit((char) i))
				throw new BException();
			byteArray.write(i);
			length.append((char) i);
		}
		int l = Integer.parseInt(length.toString());
		if (l == 0)
			throw new BException();
		byte[] bytes = new byte[l];
		if (input.read(bytes, 0, bytes.length) != l)
			throw new BException();
		byteArray.write(bytes, 0, bytes.length);
		string = new String(bytes, "UTF-8");
	}

	public BString(InputStream input) throws IOException {
		this(0, input);
	}

	public String getString() {
		return string;
	}
}
