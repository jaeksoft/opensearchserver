/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
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

package com.jaeksoft.searchlib.crawler.web.database;

import java.util.List;

public class NamedItem {

	public String name;
	public int count;
	public transient List<NamedItem> list;;

	public NamedItem() {
		name = null;
		count = 0;
	}

	public NamedItem(String name) {
		this.name = name;
		this.count = 0;
	}

	public NamedItem(String name, int count) {
		this.name = name;
		this.count = count;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int c) {
		this.count = c;
	}

	public String getNameAndCount() {
		StringBuffer sb = new StringBuffer();
		sb.append(name);
		sb.append(" (");
		sb.append(count);
		sb.append(")");
		return sb.toString();
	}

	public void setList(List<NamedItem> list) {
		this.list = list;
	}
}
