/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.scheduler;

public class TaskPropertyDef implements Comparable<TaskPropertyDef> {

	public final TaskPropertyType type;

	public final String name;

	public final int cols;

	public final int rows;

	public TaskPropertyDef(TaskPropertyType type, String name, int cols,
			int rows) {
		this.type = type;
		this.name = name;
		this.cols = cols;
		this.rows = rows;
	}

	public TaskPropertyDef(TaskPropertyType type, String name, int cols) {
		this(type, name, cols, 1);
	}

	@Override
	public int compareTo(TaskPropertyDef propDef) {
		return name.compareTo(propDef.name);
	}
}
