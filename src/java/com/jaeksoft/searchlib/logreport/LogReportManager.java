/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2011 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.logreport;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.Result;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.Timer;

public class LogReportManager {

	final private ReadWriteLock rwl = new ReadWriteLock();

	private long timeLimit;

	private File dirLog;

	private PrintWriter printWriter = null;
	private FileWriter fileWriter = null;

	private String indexName;

	private SimpleDateFormat dailyFormat = new SimpleDateFormat("yyyy-MM-dd");

	private SimpleDateFormat fullFormat = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ssZ");

	public LogReportManager(String indexName) throws IOException {
		this.indexName = indexName;
		dirLog = new File(ClientCatalog.OPENSEARCHSERVER_DATA, "logs");
		if (!dirLog.exists())
			dirLog.mkdir();
		setTimeLimit(System.currentTimeMillis());
	}

	public void close() throws IOException {
		rwl.w.lock();
		try {
			if (printWriter != null) {
				printWriter.close();
				printWriter = null;
			}
			if (fileWriter != null) {
				fileWriter.close();
				fileWriter = null;
			}
		} finally {
			rwl.w.unlock();
		}
	}

	private void setTimeLimit(long millis) throws IOException {
		rwl.w.lock();
		try {
			close();
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(millis);
			cal.set(Calendar.HOUR, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			StringBuffer sb = new StringBuffer("report.");
			sb.append(indexName);
			sb.append('.');
			sb.append(dailyFormat.format(cal.getTime()));
			cal.add(Calendar.DAY_OF_MONTH, 1);
			timeLimit = cal.getTimeInMillis();
			fileWriter = new FileWriter(new File(dirLog, sb.toString()));
			printWriter = new PrintWriter(fileWriter);
		} finally {
			rwl.w.unlock();
		}
	}

	final public void log(SearchRequest searchRequest, Timer timer,
			Result result) throws SearchLibException {
		if (searchRequest == null)
			return;
		if (!searchRequest.isLogReport())
			return;
		rwl.w.lock();
		try {
			long time = System.currentTimeMillis();
			if (time >= timeLimit)
				setTimeLimit(time);
			printWriter.print(fullFormat.format(time));
			printWriter.print('\u0009');
			printWriter.print(URLEncoder.encode(searchRequest.getQueryString(),
					"UTF-8"));
			printWriter.print('\u0009');
			if (timer != null)
				printWriter.print(timer.duration());
			printWriter.print('\u0009');
			if (result != null)
				printWriter.print(result.getNumFound());
			printWriter.print('\u0009');
			printWriter.print(searchRequest.getStart());
			List<String> customLogs = searchRequest.getCustomLogs();
			if (customLogs != null) {
				for (String customLog : customLogs) {
					printWriter.print('\u0009');
					printWriter.print(URLEncoder.encode(customLog, "UTF-8"));
				}
			}
			printWriter.println();
			printWriter.flush();
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			rwl.w.unlock();
		}
	}
}
