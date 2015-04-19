/**   
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

package com.jaeksoft.searchlib.util;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FormatUtils {

	public static class ThreadSafeDateFormat {

		private final SimpleLock lock = new SimpleLock();

		private final DateFormat dateFormat;

		public ThreadSafeDateFormat(DateFormat dateFormat) {
			this.dateFormat = dateFormat;
		}

		public Date parse(String text) throws ParseException {
			lock.rl.lock();
			try {
				return dateFormat.parse(text);
			} finally {
				lock.rl.unlock();
			}
		}

		public String format(Date date) {
			lock.rl.lock();
			try {
				return dateFormat.format(date);
			} finally {
				lock.rl.unlock();
			}
		}

		public String format(long time) {
			lock.rl.lock();
			try {
				return dateFormat.format(time);
			} finally {
				lock.rl.unlock();
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

		private final SimpleLock lock = new SimpleLock();

		private final DecimalFormat decimalFormat;

		public ThreadSafeDecimalFormat(DecimalFormat decimalFormat) {
			this.decimalFormat = decimalFormat;
		}

		public ThreadSafeDecimalFormat(String format) {
			this(new DecimalFormat(format));
		}

		public Number parse(String text) throws ParseException {
			lock.rl.lock();
			try {
				return decimalFormat.parse(text);
			} finally {
				lock.rl.unlock();
			}
		}

		public String format(double number) {
			lock.rl.lock();
			try {
				return decimalFormat.format(number);
			} finally {
				lock.rl.unlock();
			}
		}

		public String format(long number) {
			lock.rl.lock();
			try {
				return decimalFormat.format(number);
			} finally {
				lock.rl.unlock();
			}
		}

	}

}
