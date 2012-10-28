/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.jaeksoft.searchlib.web.StartStopListener;

public class Logging {

	private static Logger logger = null;

	public static boolean isDebug = System.getenv("OPENSEARCHSERVER_DEBUG") != null;;

	private static void configure() {

		Properties props = new Properties();
		FileReader fileReader = null;
		try {
			File configLog = new File(
					StartStopListener.OPENSEARCHSERVER_DATA_FILE,
					"log4j.properties");
			if (!configLog.exists()) {
				PropertyConfigurator.configure(getLoggerProperties());
				return;
			}
			fileReader = new FileReader(configLog);
			props.load(fileReader);
			PropertyConfigurator.configure(props);
		} catch (FileNotFoundException e) {
			BasicConfigurator.configure();
			e.printStackTrace();
		} catch (IOException e) {
			BasicConfigurator.configure();
			e.printStackTrace();
		} catch (SearchLibException e) {
			BasicConfigurator.configure();
			e.printStackTrace();
		} finally {
			if (fileReader != null)
				try {
					fileReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}

	}

	public final static File getLogDirectory() {
		return new File(StartStopListener.OPENSEARCHSERVER_DATA_FILE, "logs");
	}

	public final static File[] getLogFiles() {
		File dirLog = getLogDirectory();
		if (!dirLog.exists())
			return null;
		return dirLog.listFiles();
	}

	private final static Properties getLoggerProperties()
			throws SearchLibException {
		File dirLog = getLogDirectory();
		if (!dirLog.exists())
			dirLog.mkdir();
		Properties props = new Properties();
		if (isDebug)
			props.put("log4j.rootLogger", "DEBUG, R");
		else
			props.put("log4j.rootLogger", "INFO, R");
		props.put("log4j.appender.R",
				"org.apache.log4j.DailyRollingFileAppender");
		props.put("log4j.appender.R.File", new File(
				StartStopListener.OPENSEARCHSERVER_DATA_FILE, "logs"
						+ File.separator + "oss.log").getAbsolutePath());
		props.put("log4j.appender.R.DatePattern", "'.'yyyy-MM-dd");
		props.put("log4j.appender.R.layout", "org.apache.log4j.PatternLayout");
		props.put("log4j.appender.R.layout.ConversionPattern",
				"%d{HH:mm:ss,SSS} %c - %m%n");
		return props;
	}

	public static void initLogger() {
		configure();
		logger = Logger.getRootLogger();
	}

	private final static boolean noLogger(PrintStream ps, Object msg,
			Exception e) {
		if (logger != null)
			return false;
		if (msg != null)
			ps.println(msg);
		if (e != null)
			e.printStackTrace();
		return true;
	}

	public final static void error(Object msg, Exception e) {
		if (noLogger(System.err, msg, e))
			return;
		logger.error(msg, e);
	}

	public final static void error(Object msg) {
		if (noLogger(System.err, msg, null))
			return;
		logger.error(msg);
	}

	public final static void error(Exception e) {
		if (noLogger(System.out, e.getMessage(), e))
			return;
		logger.error(e.getMessage(), e);
	}

	public final static void warn(Object msg, Exception e) {
		if (noLogger(System.err, msg, e))
			return;
		logger.warn(msg, e);
	}

	public final static void warn(Object msg) {
		if (noLogger(System.err, msg, null))
			return;
		logger.warn(msg);
	}

	public final static void warn(String msg, StackTraceElement[] stackTrace) {
		logger.warn(msg);
		for (StackTraceElement element : stackTrace)
			if (element.getClassName().startsWith("com.jaeksoft"))
				logger.warn(element.toString());
	}

	public final static void warn(Exception e) {
		if (noLogger(System.err, e.getMessage(), e))
			return;
		logger.warn(e.getMessage(), e);
	}

	public final static void info(Object msg, Exception e) {
		if (noLogger(System.out, msg, e))
			return;
		logger.info(msg, e);
	}

	public final static void info(Object msg) {
		if (noLogger(System.out, msg, null))
			return;
		logger.info(msg);
	}

	public final static void info(Exception e) {
		if (noLogger(System.out, e.getMessage(), e))
			return;
		logger.info(e.getMessage(), e);
	}

	public final static void debug(Object msg, Exception e) {
		if (noLogger(System.out, msg, e))
			return;
		logger.debug(msg, e);
	}

	public final static void debug(Object msg) {
		if (noLogger(System.out, msg, null))
			return;
		logger.debug(msg);
	}

	public final static void debug(Exception e) {
		if (noLogger(System.out, e.getMessage(), e))
			return;
		logger.debug(e.getMessage(), e);
	}

	public final static String readLogs(int lines, String fileName)
			throws IOException {
		if (fileName == null)
			return null;
		File logFile = new File(getLogDirectory(), fileName);
		if (!logFile.exists())
			return null;
		FileReader fr = null;
		BufferedReader br = null;
		StringWriter sw = null;
		PrintWriter pw = null;
		LinkedList<String> list = new LinkedList<String>();
		try {
			fr = new FileReader(logFile);
			br = new BufferedReader(fr);
			String line = null;
			int size = 0;
			while ((line = br.readLine()) != null) {
				list.add(line);
				if (size++ > lines)
					list.remove();
			}
			sw = new StringWriter();
			pw = new PrintWriter(sw);
			for (String l : list)
				pw.println(StringEscapeUtils.escapeJava(l));
			return sw.toString();
		} finally {
			if (br != null)
				IOUtils.closeQuietly(br);
			if (fr != null)
				IOUtils.closeQuietly(fr);
			if (pw != null)
				IOUtils.closeQuietly(pw);
			if (sw != null)
				IOUtils.closeQuietly(sw);
		}
	}

}
