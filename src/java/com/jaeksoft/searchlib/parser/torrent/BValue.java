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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class BValue {

	protected ByteArrayOutputStream byteArray;

	protected BValue() {
		byteArray = new ByteArrayOutputStream();
	}

	public static BValue next(InputStream input) throws IOException {
		int i = input.read();
		switch (i) {
		case -1:
			return null;
		case 'e':
			return new BEnd();
		case 'd':
			return new BDictionary(input);
		case 'l':
			return new BList(input);
		case 'i':
			return new BInteger(input);
		case '0':
		case '1':
		case '2':
		case '3':
		case '4':
		case '5':
		case '6':
		case '7':
		case '8':
		case '9':
			return new BString(i, input);
		default:
			throw new BException();
		}
	}

}
