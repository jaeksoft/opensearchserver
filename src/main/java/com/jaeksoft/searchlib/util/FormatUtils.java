/**
 * Copyright (C) 2013-2016 Emmanuel Keller / Jaeksoft
 * <p>
 * http://www.open-search-server.com
 * <p>
 * This file is part of OpenSearchServer.
 * <p>
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.
 * If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.util;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FormatUtils {

	public static class ThreadSafeDateFormat {

		private final DateFormat dateFormat;

		public ThreadSafeDateFormat(DateFormat dateFormat) {
			this.dateFormat = dateFormat;
		}

		public Date parse(String text) throws ParseException {
			synchronized (dateFormat) {
				return dateFormat.parse(text);
			}
		}

		public String format(Date date) {
			synchronized (dateFormat) {
				return dateFormat.format(date);
			}
		}

		public String format(long time) {
			synchronized (dateFormat) {
				return dateFormat.format(time);
			}
		}
	}

	public static class ThreadSafeSimpleDateFormat extends ThreadSafeDateFormat {

		public ThreadSafeSimpleDateFormat(String format) {
			super(new SimpleDateFormat(format));
		}

		public ThreadSafeSimpleDateFormat(String format, Locale locale) {
			super(new SimpleDateFormat(format, locale));
		}

	}

	public static class ThreadSafeDecimalFormat {

		private final DecimalFormat decimalFormat;

		public final String zero;
		public final String doubleMax;
		public final String integerMax;
		public final String floatMax;
		public final String longMax;

		public ThreadSafeDecimalFormat(DecimalFormat decimalFormat) {
			this.decimalFormat = decimalFormat;
			zero = decimalFormat.format(0);
			doubleMax = decimalFormat.format(Double.MAX_VALUE);
			integerMax = decimalFormat.format(Integer.MAX_VALUE);
			floatMax = decimalFormat.format(Float.MAX_VALUE);
			longMax = decimalFormat.format(Long.MAX_VALUE);
		}

		public ThreadSafeDecimalFormat(String format) {
			this(new DecimalFormat(format));
		}

		final public Number parse(final String text) throws ParseException {
			if (zero.equals(text))
				return 0;
			else if (integerMax.equals(text))
				return Integer.MAX_VALUE;
			else if (longMax.equals(text))
				return Long.MAX_VALUE;
			else if (floatMax.equals(text))
				return Float.MAX_VALUE;
			else if (doubleMax.equals(text))
				return Double.MAX_VALUE;
			synchronized (decimalFormat) {
				return decimalFormat.parse(text);
			}
		}

		final public String format(final double number) {
			if (number == 0)
				return zero;
			else if (number == Float.MAX_VALUE)
				return floatMax;
			else if (number == Double.MAX_VALUE)
				return doubleMax;
			synchronized (decimalFormat) {
				return decimalFormat.format(number);
			}
		}

		final public String format(final long number) {
			if (number == 0)
				return zero;
			else if (number == Integer.MAX_VALUE)
				return integerMax;
			else if (number == Long.MAX_VALUE)
				return longMax;
			synchronized (decimalFormat) {
				return decimalFormat.format(number);
			}
		}

	}

}
