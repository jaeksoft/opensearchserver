/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012-2015 Emmanuel Keller / Jaeksoft
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

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.util.ExceptionUtils;
import com.jaeksoft.searchlib.util.FormatUtils.ThreadSafeDateFormat;
import com.jaeksoft.searchlib.util.FormatUtils.ThreadSafeSimpleDateFormat;

public class ErrorParserLogger {

	private final static ThreadSafeDateFormat timeStampFormat = new ThreadSafeSimpleDateFormat(
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

	public static class ErrorInfo {

		public final String errorMessage;
		public final String causeMessage;
		public final String codeLocation;

		public ErrorInfo(Throwable throwable) {
			this.errorMessage = throwable.getMessage();
			String causeMessage = null;
			String codeLocation = null;
			while (throwable != null) {
				causeMessage = throwable.getMessage();
				String cl = ExceptionUtils.getLocation(throwable
						.getStackTrace());
				if (cl != null)
					codeLocation = cl;
				throwable = throwable.getCause();
			}
			this.causeMessage = causeMessage;
			this.codeLocation = codeLocation;
		}

		public String toString(CharSequence separator) {
			StringBuilder sb = new StringBuilder();
			sb.append(errorMessage);
			if (causeMessage != null && !causeMessage.equals(errorMessage)) {
				sb.append(separator);
				sb.append(causeMessage);
			}
			if (codeLocation != null) {
				sb.append(separator);
				sb.append(codeLocation);
			}
			return sb.toString();
		}

		@Override
		public String toString() {
			return toString(" - ");
		}
	}

	public final static void log(String url, String filename, String filePath,
			Throwable t) throws SearchLibException {
		StringBuilder sb = new StringBuilder('\t');
		if (url != null)
			sb.append(url);
		else if (filePath != null)
			sb.append(filePath);
		else if (filename != null)
			sb.append(filename);
		sb.append('\t');
		sb.append(new ErrorInfo(t).toString("\t"));
		logger.log(sb.toString());
	}
}
