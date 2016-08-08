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

import java.awt.Rectangle;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.jaeksoft.searchlib.SearchLibException;

public class HocrBox {

	final private int x0;
	final private int y0;
	final private int x1;
	final private int y1;

	public HocrBox(String bbox) throws SearchLibException {
		int i = bbox.indexOf(';');
		if (i != -1)
			bbox = bbox.substring(0, i);
		i = 0;
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

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(x0);
		sb.append(' ');
		sb.append(y0);
		sb.append(' ');
		sb.append(x1);
		sb.append(' ');
		sb.append(y1);
		return sb.toString();
	}

	public void addRectangle(List<Rectangle> boxList, float xFactor,
			float yFactor) {
		int x = (int) (x0 * xFactor);
		int y = (int) (y0 * yFactor);
		int w = (int) ((x1 - x0) * xFactor);
		int h = (int) ((y1 - y0) * yFactor);
		Rectangle r = new Rectangle(x, y, w, h);
		boxList.add(r);
	}

	public final static void main(String[] args) throws SearchLibException {
		System.out.println(new HocrBox("343 129 548 152; baseline 0 -5"));
	}
}
