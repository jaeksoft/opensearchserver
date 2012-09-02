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
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.List;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.request.AbstractRequest;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.AbstractResult;
import com.jaeksoft.searchlib.result.AbstractResultSearch;
import com.jaeksoft.searchlib.util.Timer;
import com.jaeksoft.searchlib.web.StartStopListener;

public class LogReportManager {

	final private DailyLogger logger;

	private SimpleDateFormat timeStampFormat = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ssZ");

	public LogReportManager(String indexName) throws IOException {
		File dirLog = new File(StartStopListener.OPENSEARCHSERVER_DATA_FILE,
				"logs");
		if (!dirLog.exists())
			dirLog.mkdir();
		logger = new DailyLogger(dirLog, "report." + indexName, timeStampFormat);
	}

	public void close() {
		logger.close();
	}

	final public void log(AbstractRequest request, Timer timer,
			AbstractResult<?> result) throws SearchLibException {
		if (request == null)
			return;
		if (!request.isLogReport())
			return;
		try {
			SearchRequest searchRequest = request instanceof SearchRequest ? (SearchRequest) request
					: null;
			StringBuffer sb = new StringBuffer();
			sb.append('\u0009');
			if (searchRequest != null)
				sb.append(URLEncoder.encode(searchRequest.getQueryString(),
						"UTF-8"));
			sb.append('\u0009');
			if (timer != null)
				sb.append(timer.duration());
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
