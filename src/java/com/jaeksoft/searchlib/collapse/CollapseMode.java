/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.collapse;

public enum CollapseMode {

	COLLAPSE_OFF(0, "off"), COLLAPSE_FULL(1, "full"), COLLAPSE_OPTIMIZED(2,
			"optimized"), COLLAPSE_CLUSTER(3, "cluster");

	public int code;
	private String label;

	private CollapseMode(int code, String label) {
		this.code = code;
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public static CollapseMode valueOf(int code) {
		for (CollapseMode mode : values())
			if (mode.code == code)
				return mode;
		return null;
	}

	public static CollapseMode valueOfLabel(String label) {
		for (CollapseMode mode : values())
			if (label.equals(mode.label))
				return mode;
		return null;
	}
}
