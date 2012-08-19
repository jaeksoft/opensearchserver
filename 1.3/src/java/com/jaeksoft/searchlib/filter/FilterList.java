/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.index.ReaderLocal;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.util.Timer;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.jaeksoft.searchlib.web.ServletTransaction;

public class FilterList implements Iterable<FilterAbstract<?>> {

	private List<FilterAbstract<?>> filterList;

	private transient Config config;

	public FilterList() {
		config = null;
		this.filterList = new ArrayList<FilterAbstract<?>>();
	}

	public FilterList(FilterList fl) {
		this.config = fl.config;
		this.filterList = new ArrayList<FilterAbstract<?>>(fl.size());
		for (FilterAbstract<?> f : fl)
			add(f.duplicate());
	}

	public FilterList(Config config) {
		this.filterList = new ArrayList<FilterAbstract<?>>();
		this.config = config;
	}

	public void add(FilterAbstract<?> filter) {
		filterList.add(filter);
		renumbered();
	}

	public void remove(FilterAbstract<?> filter) {
		filterList.remove(filter);
		renumbered();
	}

	private void renumbered() {
		int i = 1;
		for (FilterAbstract<?> item : filterList)
			item.setParamPosition(i++);
	}

	public int size() {
		return filterList.size();
	}

	@Override
	public Iterator<FilterAbstract<?>> iterator() {
		return filterList.iterator();
	}

	public FilterHits getFilterHits(ReaderLocal reader,
			SchemaField defaultField, Analyzer analyzer, Timer timer)
			throws IOException, ParseException {

		if (size() == 0)
			return null;

		FilterHits filterHits = new FilterHits();
		for (FilterAbstract<?> filter : filterList)
			filterHits.and(reader.getFilterHits(defaultField, analyzer, filter,
					timer));

		return filterHits;
	}

	public Object[] toArray() {
		return filterList.toArray();
	}

	public void writeXmlConfig(XmlWriter xmlWriter) throws SAXException {
		for (FilterAbstract<?> filter : filterList)
			filter.writeXmlConfig(xmlWriter);
	}

	public void setFromServlet(ServletTransaction transaction) {
		for (FilterAbstract<?> filter : filterList)
			filter.setFromServlet(transaction);
	}

}
