/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2013 Emmanuel Keller / Jaeksoft
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

import org.xml.sax.SAXException;

import com.google.common.collect.Iterators;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.PerFieldAnalyzer;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.filter.GeoFilter.Type;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.Timer;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.jaeksoft.searchlib.web.ServletTransaction;

public class FilterList implements Iterable<FilterAbstract<?>> {

	private List<FilterAbstract<?>> filterList;

	private transient Config config;

	public FilterList() {
		config = null;
		filterList = null;
	}

	public FilterList(FilterList fl) {
		this.config = fl.config;
		this.filterList = null;
		if (fl != null)
			for (FilterAbstract<?> f : fl)
				add(f.duplicate());
	}

	public FilterList(Config config) {
		this.filterList = null;
		this.config = config;
	}

	public void add(FilterAbstract<?> filter) {
		if (filter == null)
			return;
		if (filterList == null)
			filterList = new ArrayList<FilterAbstract<?>>(1);
		filterList.add(filter);
		renumbered();
	}

	public void remove(FilterAbstract<?> filter) {
		if (filter == null)
			return;
		if (filterList == null)
			return;
		filterList.remove(filter);
		renumbered();
	}

	public void reset() {
		if (filterList == null)
			return;
		for (FilterAbstract<?> filter : filterList)
			filter.reset();
	}

	private void renumbered() {
		if (filterList == null)
			return;
		int i = 1;
		for (FilterAbstract<?> item : filterList)
			item.setParamPosition(i++);
	}

	public int size() {
		return filterList == null ? 0 : filterList.size();
	}

	@Override
	public Iterator<FilterAbstract<?>> iterator() {
		return filterList == null ? Iterators
				.<FilterAbstract<?>> emptyIterator() : filterList.iterator();
	}

	public FilterHits getFilterHits(SchemaField defaultField,
			PerFieldAnalyzer analyzer, AbstractSearchRequest request,
			Timer timer) throws IOException, ParseException,
			SearchLibException, SyntaxError, InstantiationException,
			IllegalAccessException {

		if (size() == 0)
			return null;

		FilterHits finalFilterHits = new FilterHits(true);
		for (FilterAbstract<?> filter : filterList)
			finalFilterHits.and(filter.getFilterHits(defaultField, analyzer,
					request, timer));
		return finalFilterHits;
	}

	public Object[] getArray() {
		return filterList == null ? new Object[0] : filterList.toArray();
	}

	public void writeXmlConfig(XmlWriter xmlWriter) throws SAXException {
		if (filterList != null)
			for (FilterAbstract<?> filter : filterList)
				filter.writeXmlConfig(xmlWriter);
	}

	final public void setFromServlet(final ServletTransaction transaction,
			final String prefix) {
		if (filterList != null)
			for (FilterAbstract<?> filter : filterList)
				filter.setFromServlet(transaction, prefix);
	}

	private void addFromServlet(String[] values, boolean negative) {
		if (values == null)
			return;
		for (String value : values)
			if (value != null)
				if (value.trim().length() > 0)
					add(new QueryFilter(value, negative,
							FilterAbstract.Source.REQUEST, null));
	}

	public void addFromServlet(ServletTransaction transaction, String prefix) {
		if (prefix == null)
			prefix = StringUtils.EMPTY;
		addFromServlet(transaction.getParameterValues(StringUtils.fastConcat(
				prefix, "fq")), false);
		addFromServlet(transaction.getParameterValues(StringUtils.fastConcat(
				prefix, "fqn")), true);
	}

	public void setParam(int pos, String param) throws SearchLibException {
		if (filterList == null || pos < 0 || pos >= filterList.size())
			throw new SearchLibException("Wrong filter parameter (" + pos + ")");
		filterList.get(pos).setParam(param);
	}

	final public boolean isDistance() {
		if (filterList != null)
			for (FilterAbstract<?> filter : filterList)
				if (filter.isDistance())
					return true;
		return false;
	}

	final public float getMaxDistance() {
		double maxDistance = Double.MAX_VALUE;
		if (filterList != null) {
			for (FilterAbstract<?> filter : filterList) {
				if (filter.isGeoFilter()) {
					GeoFilter geoFilter = (GeoFilter) filter;
					if (geoFilter.getType() == Type.RADIUS)
						if (maxDistance > geoFilter.getMaxDistance())
							maxDistance = geoFilter.getMaxDistance();
				}
			}
		}
		return (float) (maxDistance == Double.MAX_VALUE ? 0 : maxDistance);
	}

	public void addAuthFilter() {
		if (filterList != null)
			for (FilterAbstract<?> filter : filterList)
				if (filter instanceof AuthFilter)
					return;
		add(new AuthFilter());
	}
}
