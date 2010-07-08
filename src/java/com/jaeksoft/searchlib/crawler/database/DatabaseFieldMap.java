/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
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
import java.sql.SQLException;

import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.FieldMapGeneric;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.parser.Parser;
import com.jaeksoft.searchlib.parser.ParserSelector;
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
		for (GenericLink<String, DatabaseFieldTarget> link : getList()) {

			String content = resultSet.getString(link.getSource());
			if (content == null)
				continue;
			DatabaseFieldTarget dfTarget = link.getTarget();
			if (dfTarget.isFilePath()) {
				File file = new File(dfTarget.getFilePathPrefix() + content);
				if (file.exists()) {
					String extension = FilenameUtils.getExtension(file
							.getName());
					Parser parser = parserSelector
							.getParserFromExtension(extension);
					if (parser != null) {
						try {
							parser.parseContent(file);
						} catch (IOException e) {
							e.printStackTrace();
						}
						parser.populate(target);
					}
				}
			}

			if (dfTarget.isRemoveTag())
				content = StringUtils.removeTag(content);
			target.add(dfTarget.getName(), content);
		}
	}
}
