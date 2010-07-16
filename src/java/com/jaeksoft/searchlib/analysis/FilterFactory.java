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

import java.util.Properties;

import org.apache.lucene.analysis.TokenStream;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.util.XmlWriter;

public abstract class FilterFactory extends ClassFactory {

	final private static String FILTER_PACKAGE = "com.jaeksoft.searchlib.analysis.filter";

	public abstract TokenStream create(TokenStream tokenStream);

	public void writeXmlConfig(XmlWriter writer) throws SAXException {
		writer.startElement("filter", "class", className);
		writer.endElement();
	}

	public static FilterFactory getDefaultFilter(Config config)
			throws SearchLibException {
		return (FilterFactory) ClassFactory.create(config, FILTER_PACKAGE,
				FilterEnum.StandardFilter.name(), null);
	}

	public static FilterFactory create(Config config, String className,
			Properties properties) throws SearchLibException {
		return (FilterFactory) ClassFactory.create(config, FILTER_PACKAGE,
				className, properties);
	}

	protected static FilterFactory create(FilterFactory filter)
			throws SearchLibException {
		return (FilterFactory) ClassFactory.create(filter);
	}

}
