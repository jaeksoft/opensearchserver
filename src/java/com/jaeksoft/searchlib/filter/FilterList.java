/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft SearchLib Community.
 *
 * Jaeksoft SearchLib Community is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft SearchLib Community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft SearchLib Community. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.filter;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.queryParser.ParseException;

import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.filter.Filter.Source;

public class FilterList extends AbstractList<Filter> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5575695644602182902L;

	private List<Filter> filterList;

	private transient Config config;

	public FilterList(FilterList fl) {
		this.config = fl.config;
		this.filterList = new ArrayList<Filter>();
		for (Filter f : fl)
			this.add(f);
	}

	public FilterList(Config config) {
		this.filterList = new ArrayList<Filter>();
		this.config = config;
	}

	@Override
	public Filter get(int index) {
		return filterList.get(index);
	}

	@Override
	public boolean add(Filter filter) {
		return this.filterList.add(filter);
	}

	public boolean add(String req, Source src) throws ParseException {
		return this.add(new Filter(req, src));
	}

	@Override
	public int size() {
		return filterList.size();
	}

}
