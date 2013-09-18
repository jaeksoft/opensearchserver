/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012-2013 Emmanuel Keller / Jaeksoft
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

import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.PerFieldAnalyzer;
import com.jaeksoft.searchlib.index.ReaderLocal;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.util.Timer;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.jaeksoft.searchlib.web.ServletTransaction;

public abstract class FilterAbstract<T extends FilterAbstract<?>> {

	private Source source;

	private boolean negative;

	private String paramPosition;

	private final FilterType filterType;

	public static enum Source {
		CONFIGXML, REQUEST
	}

	public enum FilterType {

		QUERY_FILTER("Query filter", QueryFilter.class,
				"/WEB-INF/zul/query/search/filterQuery.zul"),

		GEO_FILTER("Geo filter", GeoFilter.class,
				"/WEB-INF/zul/query/search/filterGeo.zul"),

		RELATIVE_DATE_FILTER("Relative date filter", RelativeDateFilter.class,
				"/WEB-INF/zul/query/search/filterRelativeDate.zul");

		private final String label;

		private final Class<? extends FilterAbstract<?>> filterClass;

		private final String templatePath;

		private FilterType(String label,
				Class<? extends FilterAbstract<?>> filterClass,
				String templatePath) {
			this.label = label;
			this.filterClass = filterClass;
			this.templatePath = templatePath;
		}

		/**
		 * @return the label
		 */
		public String getLabel() {
			return label;
		}

		@Override
		public String toString() {
			return label;
		}

		public FilterAbstract<?> newInstance() throws InstantiationException,
				IllegalAccessException {
			return filterClass.newInstance();
		}

		/**
		 * @return the templatePath
		 */
		public String getTemplatePath() {
			return templatePath;
		}
	}

	protected FilterAbstract(FilterType filterType, Source source,
			boolean negative, String paramPosition) {
		this.filterType = filterType;
		this.source = source;
		this.negative = negative;
		this.paramPosition = paramPosition;
	}

	public Source getSource() {
		return this.source;
	}

	public boolean isNegative() {
		return negative;
	}

	public void setNegative(boolean negative) {
		this.negative = negative;
	}

	public void setParamPosition(int position) {
		StringBuffer sb = new StringBuffer("fq");
		sb.append(position);
		paramPosition = sb.toString();
	}

	public String getParamPosition() {
		return paramPosition;
	}

	public abstract String getDescription();

	public abstract String getCacheKey(SchemaField defaultField,
			PerFieldAnalyzer analyzer, AbstractSearchRequest request) throws ParseException;

	public abstract void writeXmlConfig(XmlWriter xmlWriter)
			throws SAXException;

	public abstract FilterHits getFilterHits(ReaderLocal reader,
			SchemaField defaultField, PerFieldAnalyzer analyzer, AbstractSearchRequest request,Timer timer)
			throws ParseException, IOException;

	public abstract T duplicate();

	public boolean isQueryFilter() {
		return this instanceof QueryFilter;
	}

	public boolean isGeoFilter() {
		return this instanceof GeoFilter;
	}

	public boolean isRelativeDateFilter() {
		return this instanceof RelativeDateFilter;
	}

	public void copyTo(FilterAbstract<?> selectedItem) {
		selectedItem.paramPosition = paramPosition;
		selectedItem.negative = negative;
		selectedItem.source = source;
	}

	public abstract void setFromServlet(ServletTransaction transaction);

	public abstract void setParam(String param) throws SearchLibException;

	public FilterType getFilterType() {
		return filterType;
	}

}
