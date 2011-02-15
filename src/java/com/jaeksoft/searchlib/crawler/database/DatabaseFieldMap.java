/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2010-2011 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.database;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.TreeSet;

import org.apache.commons.lang.StringEscapeUtils;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.FieldMapGeneric;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.parser.Parser;
import com.jaeksoft.searchlib.parser.ParserSelector;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.jaeksoft.searchlib.util.map.GenericLink;

public class DatabaseFieldMap extends FieldMapGeneric<DatabaseFieldTarget> {

	@Override
	protected DatabaseFieldTarget loadTarget(String targetName, Node node) {
		return new DatabaseFieldTarget(targetName, node);
	}

	@Override
	protected void writeTarget(XmlWriter xmlWriter, DatabaseFieldTarget target)
			throws SAXException {
		target.writeXml(xmlWriter);
	}

	public void mapResultSet(ParserSelector parserSelector,
			ResultSet resultSet, IndexDocument target) throws SQLException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException, SearchLibException, MalformedURLException {
		ResultSetMetaData metaData = resultSet.getMetaData();
		TreeSet<String> columns = new TreeSet<String>();
		int columnCount = metaData.getColumnCount();
		for (int i = 1; i <= columnCount; i++)
			columns.add(metaData.getColumnLabel(i));
		for (GenericLink<String, DatabaseFieldTarget> link : getList()) {
			String columnName = link.getSource();
			if (!columns.contains(columnName))
				continue;
			String content = resultSet.getString(columnName);
			if (content == null)
				continue;
			DatabaseFieldTarget dfTarget = link.getTarget();
			if (dfTarget.isFilePath()) {
				File file = new File(dfTarget.getFilePathPrefix() + content);
				if (file.exists()) {
					Parser parser = parserSelector.getParser(file.getName(),
							null);
					if (parser != null) {
						try {
							parser.parseContent(file);
						} catch (IOException e) {
							Logging.logger.warn(e.getMessage(), e);
						}
						parser.populate(target);
					}
				}
			}

			if (dfTarget.isConvertHtmlEntities())
				content = StringEscapeUtils.unescapeHtml(content);
			if (dfTarget.isRemoveTag())
				content = StringUtils.removeTag(content);
			target.add(dfTarget.getName(), new FieldValueItem(content));
		}
	}
}
