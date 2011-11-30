/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.web.converter;

import java.text.DateFormat;
import java.util.Date;

import org.zkoss.zk.ui.Component;
import org.zkoss.zkplus.databind.TypeConverter;

public class DateConverter implements TypeConverter {

	@Override
	public Object coerceToBean(Object value, Component component) {
		return null;
	}

	@Override
	public Object coerceToUi(Object value, Component component) {
		if (value == null)
			return "Unknown";
		long l = -1;
		if (value instanceof Date)
			l = ((Date) value).getTime();
		else if (value instanceof Long)
			l = (Long) value;
		if (l == -1)
			return "Unknown";
		return DateFormat.getDateTimeInstance(DateFormat.SHORT,
				DateFormat.MEDIUM).format(new Date(l));
	}
}
