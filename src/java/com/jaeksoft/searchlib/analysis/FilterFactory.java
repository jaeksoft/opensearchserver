/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2010 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.analysis;

import org.apache.lucene.analysis.TokenStream;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.util.XmlWriter;

public abstract class FilterFactory extends ClassFactory {

	final private static String FILTER_PACKAGE = "com.jaeksoft.searchlib.analysis.filter";

	public abstract TokenStream create(TokenStream tokenStream);

	public void writeXmlConfig(XmlWriter writer) throws SAXException {
		writer.startElement("filter", getAttributes());
		writer.endElement();
	}

	@Override
	protected void initProperties() throws SearchLibException {
		addProperty(ClassPropertyEnum.SCOPE, FilterScope.QUERY_INDEX.name(),
				FilterScope.values());
	}

	public FilterScope getScope() {
		return FilterScope.valueOf(getProperty(ClassPropertyEnum.SCOPE)
				.getValue());
	}

	public void setScope(FilterScope scope) throws SearchLibException {
		getProperty(ClassPropertyEnum.SCOPE).setValue(scope.name());
	}

	public static FilterFactory getDefaultFilter(Config config)
			throws SearchLibException {
		return (FilterFactory) ClassFactory.create(config, FILTER_PACKAGE,
				FilterEnum.StandardFilter.name());
	}

	/**
	 * Create a new filter by reading the attributes of an XML node
	 * 
	 * @param config
	 * @param node
	 * @return a FilterFactory
	 * @throws SearchLibException
	 */
	public static FilterFactory create(Config config, Node node)
			throws SearchLibException {
		return (FilterFactory) ClassFactory
				.create(config, FILTER_PACKAGE, node);
	}

	/**
	 * Clone a filter
	 * 
	 * @param filter
	 * @return a FilterFactory
	 * @throws SearchLibException
	 */
	public static FilterFactory create(FilterFactory filter)
			throws SearchLibException {
		return (FilterFactory) ClassFactory.create(filter);
	}

	/**
	 * Create a new filter with default parameters
	 * 
	 * @param config
	 * @param className
	 * @return
	 * @throws SearchLibException
	 */
	public static FilterFactory create(Config config, String className)
			throws SearchLibException {
		return (FilterFactory) ClassFactory.create(config, FILTER_PACKAGE,
				className);
	}

}
