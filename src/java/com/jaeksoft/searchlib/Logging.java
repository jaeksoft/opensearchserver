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

package com.jaeksoft.searchlib;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Logging {

	public static Logger logger = null;

	private static void configure() {

		File configLog = new File(ClientCatalog.OPENSEARCHSERVER_DATA,
				"log4j.properties");
		if (!configLog.exists()) {
			PropertyConfigurator.configure(getLoggerProperties());
			return;
		}

		Properties props = new Properties();
		FileReader fileReader = null;
		try {
			fileReader = new FileReader(configLog);
			props.load(fileReader);
			PropertyConfigurator.configure(props);
		} catch (FileNotFoundException e) {
			BasicConfigurator.configure();
			e.printStackTrace();
		} catch (IOException e) {
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

	private static Properties getLoggerProperties() {
		File dirLog = new File(ClientCatalog.OPENSEARCHSERVER_DATA, "logs");
		if (!dirLog.exists())
			dirLog.mkdir();
		Properties props = new Properties();
		props.put("log4j.rootLogger", "INFO, R");
		props.put("log4j.appender.R",
				"org.apache.log4j.DailyRollingFileAppender");
		props.put("log4j.appender.R.File", new File(
				ClientCatalog.OPENSEARCHSERVER_DATA, "logs/oss.log")
				.getAbsolutePath());
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
}
