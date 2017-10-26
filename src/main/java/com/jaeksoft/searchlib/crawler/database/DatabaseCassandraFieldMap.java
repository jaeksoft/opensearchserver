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

import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.Row;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.FieldMapContext;
import com.jaeksoft.searchlib.crawler.common.database.CommonFieldTarget;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.util.map.GenericLink;
import com.jaeksoft.searchlib.util.map.SourceField;
import com.qwazr.utils.StringUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;

class DatabaseCassandraFieldMap extends DatabaseFieldMap {

	final void mapRow(FieldMapContext context, Row row, ColumnDefinitions columns, IndexDocument target,
			Set<String> filePathSet)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, SearchLibException,
			ParseException, IOException, SyntaxError, URISyntaxException, InterruptedException {

		for (GenericLink<SourceField, CommonFieldTarget> link : getList()) {
			final String columnName = link.getSource().getUniqueName();
			if (!columns.contains(columnName))
				continue;
			final DataType columnType = columns.getType(columnName);
			final CommonFieldTarget targetField = link.getTarget();
			if (targetField.isCrawlFile() && columnType.getName().isCompatibleWith(DataType.Name.BLOB)) {
				handleBlob(context, row, columns, target, filePathSet, columnName, targetField);
				continue;
			}
			final Object rowValue = row.getObject(columnName);
			if (rowValue != null) {
				if (rowValue instanceof Collection) {
					for (Object value : ((Collection) rowValue))
						if (value != null)
							mapFieldTarget(context, link.getTarget(), false, value.toString(), target, filePathSet);
				} else
					mapFieldTarget(context, link.getTarget(), false, rowValue.toString(), target, filePathSet);
			}
		}

	}

	private boolean doBlob(Row row, File binaryPath, String columnName) throws IOException {
		final ByteBuffer byteBuffer = row.getBytes(columnName);
		if (byteBuffer == null)
			return false;
		try (final FileChannel out = new FileOutputStream(binaryPath).getChannel()) {
			out.write(byteBuffer);
			return true;
		}
	}

	private void handleBlob(FieldMapContext context, Row row, ColumnDefinitions columns, IndexDocument target,
			Set<String> filePathSet, String columnName, CommonFieldTarget targetField)
			throws IOException, SearchLibException, InterruptedException, ParseException, SyntaxError,
			InstantiationException, URISyntaxException, IllegalAccessException, ClassNotFoundException {
		final String filePath = columns.contains(targetField.getFilePathPrefix()) ?
				row.getString(targetField.getFilePathPrefix()) :
				null;
		if (StringUtils.isBlank(filePath))
			return;
		final String fileName = FilenameUtils.getName(filePath);
		Path binaryPath = null;
		try {
			binaryPath = Files.createTempFile("oss", fileName);
			File binaryFile = binaryPath.toFile();
			if (!doBlob(row, binaryFile, columnName))
				return;
			mapFieldTarget(context, targetField, true, binaryPath.toString(), target, filePathSet);
		} finally {
			if (binaryPath != null)
				Files.deleteIfExists(binaryPath);
		}
	}
}
