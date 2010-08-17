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

public class BInteger extends BValue {

	private Long integer;

	public BInteger(InputStream input) throws IOException {
		integer = null;
		StringBuffer integerString = new StringBuffer();
		byteArray.write('i');
		for (;;) {
			int i = input.read();
			if (i == -1)
				throw new BException();
			if (i == 'e')
				break;
			if (!Character.isDigit((char) i))
				throw new BException();
			byteArray.write(i);
			integerString.append((char) i);
		}
		byteArray.write('e');
		integer = Long.parseLong(integerString.toString());
	}

	public Long getInteger() {
		return integer;
	}
}
