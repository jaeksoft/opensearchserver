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

package com.jaeksoft.searchlib.web.converter;

import java.text.DecimalFormat;

import org.zkoss.bind.BindContext;
import org.zkoss.bind.Converter;
import org.zkoss.zk.ui.Component;

public class PercentConverter implements Converter<Object, Object, Component> {

	@Override
	public Object coerceToBean(Object value, Component component,
			BindContext ctx) {
		return IGNORED_VALUE;
	}

	@Override
	public Object coerceToUi(Object value, Component component, BindContext ctx) {
		String result = null;
		DecimalFormat format = (DecimalFormat) DecimalFormat.getInstance();
		format.setMaximumFractionDigits(1);
		if (value instanceof Double) {
			Double d = (Double) value;
			if (d == null || d.equals(Double.NaN))
				return IGNORED_VALUE;
			else
				result = format.format(d) + " %";
		} else if (value instanceof Float) {
			Float f = (Float) value;
			if (f == null || f.equals(Float.NaN))
				return IGNORED_VALUE;
			else
				result = format.format(f) + " %";
		}
		return result;
	}
}
