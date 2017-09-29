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

import com.jaeksoft.searchlib.Logging;
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Array;
import java.sql.Blob;
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
			final int columnType = columns.get(columnName);
			final CommonFieldTarget targetField = link.getTarget();
			if (targetField.isCrawlFile() && isBinary(columnType)) {
				handleBinary(context, resultSet, target, filePathSet, columnName, targetField);
				continue;
			}
			if (columnType == Types.ARRAY) {
				handleArray(context, resultSet, target, filePathSet, columnName, targetField);
				continue;
			}
			final String content = resultSet.getString(columnName);
			if (content != null)
				mapFieldTarget(context, link.getTarget(), false, content, target, filePathSet);
		}

	}

	private boolean tryBinaryStream(ResultSet resultSet, File binaryPath, String columnName) throws IOException {
		try (final InputStream input = resultSet.getBinaryStream(columnName)) {
			if (input == null)
				return false;
			IOUtils.copy(input, binaryPath);
			return true;
		} catch (SQLException e) {
			Logging.warn(e);
			return false;
		}
	}

	private boolean tryBlob(ResultSet resultSet, File binaryPath, String columnName) throws IOException {
		try {
			final Blob blob = resultSet.getBlob(columnName);
			if (blob == null)
				return false;
			try (final InputStream input = blob.getBinaryStream()) {
				if (input == null)
					return false;
				IOUtils.copy(input, binaryPath);
				return true;
			} finally {
				blob.free();
			}
		} catch (SQLException e) {
			Logging.warn(e);
			return false;
		}
	}

	private void handleBinary(FieldMapContext context, ResultSet resultSet, IndexDocument target,
			Set<String> filePathSet, String columnName, CommonFieldTarget targetField)
			throws SQLException, IOException, SearchLibException, InterruptedException, ParseException, SyntaxError,
			InstantiationException, URISyntaxException, IllegalAccessException, ClassNotFoundException {
		final String filePath = resultSet.getString(targetField.getFilePathPrefix());
		if (StringUtils.isBlank(filePath))
			return;
		final String fileName = FilenameUtils.getName(filePath);
		Path binaryPath = null;
		try {
			binaryPath = Files.createTempFile("oss", fileName);
			File binaryFile = binaryPath.toFile();
			if (!tryBinaryStream(resultSet, binaryFile, columnName))
				if (!tryBlob(resultSet, binaryFile, columnName))
					return;
			mapFieldTarget(context, targetField, true, binaryPath.toString(), target, filePathSet);
		} finally {
			if (binaryPath != null)
				Files.deleteIfExists(binaryPath);
		}
	}

	private boolean isBinary(Integer sqlType) {
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

	private void handleArray(FieldMapContext context, ResultSet resultSet, IndexDocument target,
			Set<String> filePathSet, String columnName, CommonFieldTarget targetField)
			throws SQLException, IOException, SearchLibException, InterruptedException, ParseException, SyntaxError,
			InstantiationException, URISyntaxException, IllegalAccessException, ClassNotFoundException {
		final Array array = resultSet.getArray(columnName);
		if (array == null)
			return;
		try (final ResultSet arrayResultSet = array.getResultSet()) {
			if (arrayResultSet.getMetaData().getColumnCount() < 1)
				return;
			while (arrayResultSet.next()) {
				final String content = arrayResultSet.getString(0);
				if (content != null)
					mapFieldTarget(context, targetField, false, content, target, filePathSet);
			}
		}
	}

}
