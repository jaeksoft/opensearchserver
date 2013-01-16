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

package com.jaeksoft.searchlib.ocr;

import org.apache.commons.lang.StringUtils;

import com.jaeksoft.searchlib.SearchLibException;

public class HocrBox {

	private int x0;
	private int y0;
	private int x1;
	private int y1;

	public HocrBox(String bbox) throws SearchLibException {
		int i = 0;
		String[] array = StringUtils.split(bbox);
		switch (array.length) {
		case 5:
			if (!"bbox".equalsIgnoreCase(array[i++]))
				throw new SearchLibException("bad HocrBox record: " + bbox);
			break;
		case 4:
			break;
		default:
			throw new SearchLibException("bad bbox record: " + bbox);
		}

		x0 = Integer.parseInt(array[i++]);
		y0 = Integer.parseInt(array[i++]);
		x1 = Integer.parseInt(array[i++]);
		y1 = Integer.parseInt(array[i++]);
	}

	public void offset(int x, int y) {
		x0 += x;
		x1 += x;
		y0 += y;
		y1 += y;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(x0);
		sb.append(' ');
		sb.append(y0);
		sb.append(' ');
		sb.append(x1);
		sb.append(' ');
		sb.append(y1);
		return sb.toString();
	}
}
