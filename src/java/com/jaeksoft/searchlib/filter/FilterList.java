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

package com.jaeksoft.searchlib.filter;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.ParseException;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.filter.Filter.Source;
import com.jaeksoft.searchlib.index.ReaderLocal;
import com.jaeksoft.searchlib.schema.Field;
import com.jaeksoft.searchlib.util.External;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.jaeksoft.searchlib.util.External.Collecter;

public class FilterList implements Externalizable, Collecter<Filter>,
		Iterable<Filter> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5575695644602182902L;

	private List<Filter> filterList;

	private transient Config config;

	public FilterList() {
		config = null;
		this.filterList = new ArrayList<Filter>();
	}

	public FilterList(FilterList fl) {
		this.config = fl.config;
		this.filterList = new ArrayList<Filter>(fl.size());
		for (Filter f : fl)
			addObject(f);
	}

	public FilterList(Config config) {
		this.filterList = new ArrayList<Filter>();
		this.config = config;
	}

	public void addObject(Filter filter) {
		filterList.add(filter);
	}

	public void add(String req, Source src) {
		addObject(new Filter(req, src));
	}

	public void remove(Filter filter) {
		filterList.remove(filter);
	}

	public int size() {
		return filterList.size();
	}

	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		External.readCollection(in, this);
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		External.writeCollection(filterList, out);
	}

	public Iterator<Filter> iterator() {
		return filterList.iterator();
	}

	public FilterHits getFilterHits(ReaderLocal reader, Field defaultField,
			Analyzer analyzer, boolean noCache) throws IOException,
			ParseException {

		if (size() == 0)
			return null;

		FilterHits filterHits = new FilterHits();
		for (Filter filter : filterList)
			filterHits.and(reader.getFilterHits(defaultField, analyzer, filter,
					noCache));

		return filterHits;
	}

	public Object[] toArray() {
		return filterList.toArray();
	}

	public void writeXmlConfig(XmlWriter xmlWriter) throws SAXException {
		for (Filter filter : filterList)
			filter.writeXmlConfig(xmlWriter);
	}

}
