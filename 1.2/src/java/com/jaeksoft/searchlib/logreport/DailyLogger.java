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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.io.IOUtils;

import com.jaeksoft.searchlib.SearchLibException;

public class DailyLogger {

	final private ReentrantLock lock = new ReentrantLock();

	final private SimpleDateFormat dailyFormat = new SimpleDateFormat(
			"yyyy-MM-dd");

	final private SimpleDateFormat timeStampFormat;

	final private File parentDir;

	final private String filePrefix;

	private long timeLimit;

	private String currentLogFileName;

	private PrintWriter printWriter = null;

	private FileWriter fileWriter = null;

	public DailyLogger(File parentDir, String filePrefix,
			SimpleDateFormat timeStampFormat) {
		this.parentDir = parentDir;
		this.filePrefix = filePrefix;
		this.timeStampFormat = timeStampFormat;
		setTimeLimit(System.currentTimeMillis());
	}

	private void setTimeLimit(long millis) {
		lock.lock();
		try {
			close();
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(millis);
			cal.set(Calendar.HOUR, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			StringBuffer sb = new StringBuffer(filePrefix);
			sb.append('.');
			sb.append(dailyFormat.format(cal.getTime()));
			currentLogFileName = sb.toString();
			cal.add(Calendar.DAY_OF_MONTH, 1);
			timeLimit = cal.getTimeInMillis();
		} finally {
			lock.unlock();
		}
	}

	final private void open() throws IOException {
		fileWriter = new FileWriter(new File(parentDir, currentLogFileName),
				true);
		printWriter = new PrintWriter(fileWriter);
	}

	final public void close() {
		lock.lock();
		try {
			if (printWriter != null) {
				IOUtils.closeQuietly(printWriter);
				printWriter = null;
			}
			if (fileWriter != null) {
				IOUtils.closeQuietly(fileWriter);
				fileWriter = null;
			}
		} finally {
			lock.unlock();
		}
	}

	final protected void log(String message) throws SearchLibException {
		lock.lock();
		try {
			long time = System.currentTimeMillis();
			if (time >= timeLimit)
				setTimeLimit(time);
			if (printWriter == null)
				open();
			if (timeStampFormat != null)
				printWriter.print(timeStampFormat.format(time));
			printWriter.println(message);
			printWriter.flush();
		} catch (IOException e) {
			close();
			throw new SearchLibException(e);
		} finally {
			lock.unlock();
		}
	}
}
