/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.index;

public enum IndexMode {

	READ_ONLY("Read only"), READ_WRITE("Read/write");

	final protected String label;

	private IndexMode(String label) {
		this.label = label;
	}

	final public String getLabel() {
		return label;
	}

	@Override
	public final String toString() {
		return label;
	}

	final public static IndexMode find(String label) {
		if (label == null)
			return READ_WRITE;
		for (IndexMode mode : values()) {
			if (label.equalsIgnoreCase(mode.label))
				return mode;
			if (label.equalsIgnoreCase(mode.name()))
				return mode;
		}
		return READ_WRITE;
	}
}
