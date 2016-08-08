/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.common.database;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;

public class TimeInterval {

	public static enum IntervalUnit {

		days, hours, minutes;

		public final static long getInterval(long fetchInterval,
				IntervalUnit intervalUnit) {
			if (intervalUnit == null)
				intervalUnit = days;
			switch (intervalUnit) {
			case hours:
				return fetchInterval * 1000 * 3600;
			case minutes:
				return fetchInterval * 1000 * 60;
			default:
			case days:
				return fetchInterval * 1000 * 86400;
			}
		}

		public static IntervalUnit find(String intervalUnit) {
			if (intervalUnit == null)
				return days;
			for (IntervalUnit unit : values())
				if (intervalUnit.equalsIgnoreCase(unit.name()))
					return unit;
			return days;
		}
	}

	private IntervalUnit unit;

	private long interval;

	private long offset;

	public TimeInterval(IntervalUnit unit, long interval) {
		this.unit = unit;
		this.interval = interval;
		computeOffset();
	}

	public TimeInterval() {
		this(IntervalUnit.days, 0);
	}

	public TimeInterval(String intervalUnit, long interval) {
		this(IntervalUnit.find(intervalUnit), interval);
	}

	public TimeInterval(String byText) {
		setByText(byText);
	}

	public TimeInterval(TimeInterval source) {
		this(source.unit, source.interval);
	}

	private void computeOffset() {
		offset = IntervalUnit.getInterval(interval, unit);
	}

	/**
	 * @return the unit
	 */
	public IntervalUnit getUnit() {
		return unit;
	}

	/**
	 * @param unit
	 *            the unit to set
	 */
	public void setUnit(IntervalUnit unit) {
		this.unit = unit;
		computeOffset();
	}

	/**
	 * @return the interval
	 */
	public long getInterval() {
		return interval;
	}

	/**
	 * @param interval
	 *            the interval to set
	 */
	public void setInterval(long interval) {
		this.interval = interval;
		computeOffset();
	}

	public final long getPastTime(long origin) {
		return origin - offset;
	}

	public final long getFutureTime(long origin) {
		return origin + offset;
	}

	public final Date getPastDate(long origin) {
		return new Date(getPastTime(origin));
	}

	public final Date getFutureDate(long origin) {
		return new Date(getFutureTime(origin));
	}

	public final void setByText(String text) {
		if (text == null)
			return;
		if (text.length() == 0)
			return;
		String[] part = StringUtils.split(text);
		if (part == null)
			return;
		if (part.length < 2)
			return;
		interval = Long.parseLong(part[0]);
		unit = IntervalUnit.find(part[1]);
		computeOffset();
	}

	public final String getByText() {
		StringBuilder sb = new StringBuilder();
		sb.append(interval);
		if (unit != null) {
			sb.append(' ');
			sb.append(unit.name());
		}
		return sb.toString();
	}

	public void set(TimeInterval source) {
		this.interval = source.interval;
		this.unit = source.unit;
		this.offset = source.offset;
	}

}
