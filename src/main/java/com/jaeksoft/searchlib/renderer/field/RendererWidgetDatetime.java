/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2014-2015 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.renderer.field;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.jaeksoft.searchlib.util.FormatUtils.ThreadSafeDateFormat;

public class RendererWidgetDatetime extends RendererWidget {

	private ThreadSafeDateFormat inputDateFormat;
	private ThreadSafeDateFormat outputDateFormat;

	private final static String defaultInputDateFormat = "yyyyMMddHHmmssSSS";
	private final static String defaultOutputDateFormat = "yyyy/MM/dd HH:mm:ss";

	@Override
	void init(String properties) throws IOException {
		super.init(properties);
		final String inputDateFormatText;
		final String outputDateFormatText;
		if (this.properties != null) {
			inputDateFormatText = this.properties.getProperty("inputformat", defaultInputDateFormat);
			outputDateFormatText = this.properties.getProperty("outputformat", defaultOutputDateFormat);
		} else {
			inputDateFormatText = defaultInputDateFormat;
			outputDateFormatText = defaultOutputDateFormat;
		}
		inputDateFormat = new ThreadSafeDateFormat(new SimpleDateFormat(inputDateFormatText));
		outputDateFormat = new ThreadSafeDateFormat(new SimpleDateFormat(outputDateFormatText));
	}

	@Override
	public String getValue(String value) {
		if (value == null)
			return value;
		Date date;
		try {
			date = inputDateFormat.parse(value);
		} catch (ParseException e) {
			return value;
		}
		return outputDateFormat.format(date);
	}
}
