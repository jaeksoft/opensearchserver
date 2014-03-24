/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012-2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.index.osse;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

public class OsseTermOffset extends Structure {

	final public int ui32StartOffset;
	final public int ui32EndOffset;

	public OsseTermOffset(int start, int end) {
		this.ui32StartOffset = start;
		this.ui32EndOffset = end;
	}

	private final static List<?> fieldOrderList = Arrays.asList(new String[] {
			"ui32StartOffset", "ui32EndOffset" });

	@Override
	protected List<?> getFieldOrder() {
		return fieldOrderList;
	}

}