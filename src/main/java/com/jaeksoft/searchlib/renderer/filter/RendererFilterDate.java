/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.renderer.filter;

import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import com.jaeksoft.searchlib.result.AbstractResultSearch;
import com.jaeksoft.searchlib.util.FormatUtils.ThreadSafeDateFormat;
import com.jaeksoft.searchlib.util.StringUtils;

public class RendererFilterDate implements RendererFilterInterface {

	private ThreadSafeDateFormat dateFormat;
	private String fieldName;
	private final List<Item> items = new ArrayList<Item>();

	@Override
	public void populate(AbstractResultSearch facetResult,
			List<RendererFilterItem> filterItem) {
		long time = System.currentTimeMillis();
		for (Item item : items)
			filterItem.add(new RendererFilterItem(item.getFilterQuery(time),
					item.label));
	}

	private final String defaultProperties = "1.label=Any time"
			+ StringUtils.LF + "1.range=anytime" + StringUtils.LF
			+ "2.label=Past 24 hours" + StringUtils.LF + "2.range=pastday"
			+ StringUtils.LF + "3.label=Past week" + StringUtils.LF
			+ "3.range=pastweek" + StringUtils.LF + "4.label=Past month"
			+ StringUtils.LF + "4.range=pastmonth" + StringUtils.LF
			+ "5.label=Past year" + StringUtils.LF + "5.range=pastyear"
			+ StringUtils.LF + "dateformat=yyyyMMddHHmmssSSS";

	@Override
	public String getDefaultProperties() {
		return defaultProperties;
	}

	@Override
	public void init(String fieldName, String properties) throws IOException {
		this.fieldName = fieldName;
		Properties props = new Properties();
		props.load(new StringReader(properties));
		dateFormat = new ThreadSafeDateFormat(new SimpleDateFormat(
				props.getProperty("dateformat", "yyyyMMddHHmmssSSS")));
		int i = 1;
		items.clear();
		for (;;) {
			String label = props.getProperty(i + ".label");
			if (label == null)
				break;
			Range range = Range.valueOf(props.getProperty(i + ".range"));
			items.add(new Item(label, range));
			i++;
		}
	}

	private enum Range {
		anytime, pastday, pastweek, pastmonth, pastyear;
	}

	private class Item {

		private final String label;
		private final Range range;

		private Item(String label, Range range) {
			this.label = label;
			this.range = range;
		}

		public String getFilterQuery(long time) {
			String from = "*";
			switch (range) {
			case anytime:
				return StringUtils.EMPTY;
			case pastday:
				from = dateFormat.format(time - 86400000);
				break;
			case pastweek:
				from = dateFormat.format(time - 604800000);
				break;
			case pastmonth:
				Calendar cal = Calendar.getInstance();
				cal.roll(Calendar.MONTH, -1);
				from = dateFormat.format(cal.getTime());
				break;
			case pastyear:
				cal = Calendar.getInstance();
				cal.roll(Calendar.YEAR, -1);
				from = dateFormat.format(cal.getTime());
				break;
			}
			return StringUtils.fastConcat(fieldName, ":[", from, " TO ", "*]");
		}
	}

}
