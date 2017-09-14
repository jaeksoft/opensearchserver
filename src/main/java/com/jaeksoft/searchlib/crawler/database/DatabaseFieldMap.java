/*
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2010-2017 Emmanuel Keller / Jaeksoft
 * <p>
 * http://www.open-search-server.com
 * <p>
 * This file is part of OpenSearchServer.
 * <p>
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.jaeksoft.searchlib.crawler.database;

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
import com.qwazr.utils.IOUtils;
import com.qwazr.utils.StringUtils;
import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;
import java.util.Set;

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

	final public void mapResultSet(FieldMapContext context, ResultSet resultSet, Map<String, Integer> columns,
			IndexDocument target, Set<String> filePathSet)
			throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException,
			SearchLibException, ParseException, IOException, SyntaxError, URISyntaxException, InterruptedException {

		for (GenericLink<SourceField, CommonFieldTarget> link : getList()) {
			final String columnName = link.getSource().getUniqueName();
			if (!columns.containsKey(columnName))
				continue;
			final String content;
			final Path binaryPath;
			final CommonFieldTarget targetField = link.getTarget();
			if (targetField.isCrawlFile() && isBlob(columns.get(columnName))) {
				final String filePath = resultSet.getString(targetField.getFilePathPrefix());
				if (StringUtils.isBlank(filePath))
					continue;
				final String fileName = FilenameUtils.getName(filePath);
				binaryPath = Files.createTempFile("oss", fileName);
				try (final InputStream input = resultSet.getBinaryStream(columnName)) {
					IOUtils.copy(input, binaryPath.toFile());
				}
				content = binaryPath.toString();
			} else {
				content = resultSet.getString(columnName);
				binaryPath = null;
			}
			if (content != null)
				mapFieldTarget(context, link.getTarget(), binaryPath != null, content, target, filePathSet);
			if (binaryPath != null)
				Files.deleteIfExists(binaryPath);
		}

	}

	private boolean isBlob(Integer sqlType) {
		switch (sqlType) {
		case Types.BLOB:
		case Types.VARBINARY:
		case Types.BINARY:
		case Types.LONGVARBINARY:
			return true;
		default:
			return false;
		}
	}

}
