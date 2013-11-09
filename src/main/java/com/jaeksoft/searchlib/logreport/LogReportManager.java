/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.logreport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.request.AbstractRequest;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.result.AbstractResult;
import com.jaeksoft.searchlib.result.AbstractResultSearch;
import com.jaeksoft.searchlib.util.FormatUtils.ThreadSafeDateFormat;
import com.jaeksoft.searchlib.util.FormatUtils.ThreadSafeSimpleDateFormat;
import com.jaeksoft.searchlib.util.Timer;
import com.jaeksoft.searchlib.web.StartStopListener;

public class LogReportManager {

	final private File dirLog;
	final private DailyLogger logger;

	private final static ThreadSafeDateFormat timeStampFormat = new ThreadSafeSimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ssZ");

	public LogReportManager(String indexName) throws IOException {
		dirLog = new File(StartStopListener.OPENSEARCHSERVER_DATA_FILE, "logs");
		if (!dirLog.exists())
			dirLog.mkdir();
		logger = new DailyLogger(getLogDirectory(), "report." + indexName,
				timeStampFormat);
	}

	public void close() {
		logger.close();
	}

	final public File getLogDirectory() {
		return dirLog;
	}

	final private File getArchiveDirectory() {
		return new File(getLogDirectory(), "archives");
	}

	final public void archiveFile(String fileName) throws IOException {
		ZipOutputStream zos = null;
		FileInputStream fis = null;
		try {
			File sourceFile = new File(getLogDirectory(), fileName);
			File destinationDir = getArchiveDirectory();
			if (!destinationDir.exists())
				destinationDir.mkdir();
			File destinationFile = new File(destinationDir, fileName + ".zip");
			zos = new ZipOutputStream(new FileOutputStream(destinationFile));
			fis = new FileInputStream(sourceFile);
			zos.putNextEntry(new ZipEntry(fileName.trim()));
			byte[] buffer = new byte[16384];
			int size;
			while ((size = fis.read(buffer)) > 0)
				zos.write(buffer, 0, size);
			zos.closeEntry();
			zos.close();
			zos = null;
			fis.close();
			fis = null;
			if (!sourceFile.delete())
				throw new IOException("Unable to delete original file "
						+ sourceFile.getAbsolutePath());
		} finally {
			if (zos != null)
				zos.closeEntry();
			if (fis != null)
				IOUtils.closeQuietly(fis);
			if (zos != null)
				IOUtils.closeQuietly(zos);
		}
	}

	public void deleteFile(String filename) throws IOException {
		File logFile = new File(getLogDirectory(), filename);
		if (!logFile.exists())
			return;
		if (!logFile.delete())
			throw new IOException("Unable to delete "
					+ logFile.getAbsolutePath());
	}

	final public void log(AbstractRequest request, Timer timer,
			AbstractResult<?> result) throws SearchLibException {
		if (request == null)
			return;
		if (!request.isLogReport())
			return;
		try {
			AbstractSearchRequest searchRequest = request instanceof AbstractSearchRequest ? (AbstractSearchRequest) request
					: null;
			StringBuilder sb = new StringBuilder();
			sb.append('\u0009');
			if (searchRequest != null)
				sb.append(URLEncoder.encode(searchRequest.getQueryString(),
						"UTF-8"));
			sb.append('\u0009');
			if (timer != null)
				sb.append(timer.getDuration());
			sb.append('\u0009');
			if (result != null && result instanceof AbstractResultSearch)
				sb.append(((AbstractResultSearch) result).getNumFound());
			sb.append('\u0009');
			if (searchRequest != null)
				sb.append(searchRequest.getStart());
			List<String> customLogs = searchRequest.getCustomLogs();
			if (customLogs != null) {
				for (String customLog : customLogs) {
					sb.append('\u0009');
					sb.append(URLEncoder.encode(customLog, "UTF-8"));
				}
			}
			logger.log(sb.toString());
		} catch (UnsupportedEncodingException e) {
			throw new SearchLibException(e);
		}
	}
}
