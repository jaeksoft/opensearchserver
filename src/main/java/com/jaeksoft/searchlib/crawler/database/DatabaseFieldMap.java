/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.database;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.FieldMapContext;
import com.jaeksoft.searchlib.crawler.FieldMapGeneric;
import com.jaeksoft.searchlib.crawler.common.database.CommonFieldTarget;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.jaeksoft.searchlib.util.map.GenericLink;
import com.jaeksoft.searchlib.util.map.SourceField;

public class DatabaseFieldMap extends FieldMapGeneric<SourceField, CommonFieldTarget> {

	@Override
	protected CommonFieldTarget loadTarget(String targetName, Node node) {
		return new CommonFieldTarget(targetName, node);
	}

	@Override
	protected SourceField loadSource(String source) {
		return new SourceField(source);
	}

	@Override
	protected void writeTarget(XmlWriter xmlWriter, CommonFieldTarget target) throws SAXException {
		target.writeXml(xmlWriter);
	}

	public boolean isUrl() {
		for (GenericLink<SourceField, CommonFieldTarget> link : getList()) {
			CommonFieldTarget dfTarget = link.getTarget();
			if (dfTarget.isCrawlUrl())
				return true;
		}
		return false;
	}

	final public void mapResultSet(FieldMapContext context, ResultSet resultSet, Set<String> columns,
			IndexDocument target, Set<String> filePathSet) throws SQLException, InstantiationException,
					IllegalAccessException, ClassNotFoundException, SearchLibException, ParseException, IOException,
					SyntaxError, URISyntaxException, InterruptedException {
		for (GenericLink<SourceField, CommonFieldTarget> link : getList()) {
			String columnName = link.getSource().getUniqueName();
			if (!columns.contains(columnName))
				continue;
			String content = resultSet.getString(columnName);
			if (content == null)
				continue;
			mapFieldTarget(context, link.getTarget(), content, target, filePathSet);
		}
	}

}
