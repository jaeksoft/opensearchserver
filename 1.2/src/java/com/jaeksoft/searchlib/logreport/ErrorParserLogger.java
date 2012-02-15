/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
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

import java.text.SimpleDateFormat;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;

public class ErrorParserLogger {

	private final static SimpleDateFormat timeStampFormat = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ssZ");

	private static DailyLogger logger = null;

	public final static void init() {
		logger = new DailyLogger(Logging.getLogDirectory(), "parser.error",
				timeStampFormat);
	}

	public final static void close() {
		if (logger != null)
			logger.close();
	}

	public final static void log(String url, String filename, String filePath,
			Throwable t) throws SearchLibException {
		StringBuffer sb = new StringBuffer('\t');
		if (url != null)
			sb.append(url);
		else if (filePath != null)
			sb.append(filePath);
		else if (filename != null)
			sb.append(filename);
		sb.append('\t');
		sb.append(t.getMessage());
		String codeLocation = null;
		while (t != null) {
			for (StackTraceElement element : t.getStackTrace()) {
				if (element.getClassName().startsWith("com.jaeksoft")) {
					codeLocation = element.toString();
					break;
				}
			}
			t = t.getCause();
		}
		if (codeLocation != null) {
			sb.append('\t');
			sb.append(codeLocation);
		}
		logger.log(sb.toString());
	}
}
