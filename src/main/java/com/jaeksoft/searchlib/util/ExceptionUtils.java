/*
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2015-2017 Emmanuel Keller / Jaeksoft
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
 */
package com.jaeksoft.searchlib.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;

public class ExceptionUtils extends
		org.apache.commons.lang3.exception.ExceptionUtils {

	public final static String getLocation(StackTraceElement[] stackTrace) {
		for (StackTraceElement element : stackTrace)
			if (element.getClassName().startsWith("com.jaeksoft"))
				return element.toString();
		return null;
	}

	public final static String getFirstLocation(StackTraceElement[] stackTrace) {
		for (StackTraceElement element : stackTrace) {
			String ele = element.toString();
			if (ele != null && ele.length() > 0)
				return ele;
		}
		return null;
	}

	public final static String getFullStackTrace(StackTraceElement[] stackTrace) {
		StringWriter sw = null;
		PrintWriter pw = null;
		try {
			sw = new StringWriter();
			pw = new PrintWriter(sw);
			for (StackTraceElement element : stackTrace)
				pw.println(element);
			return sw.toString();
		} finally {
			IOUtils.close(pw, sw);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T extends Exception> T throwException(Exception exception,
			Class<T> exceptionClass) throws T {
		if (exception == null)
			return null;
		if (exceptionClass.isInstance(exception))
			throw (T) exception;
		try {
			return (T) exceptionClass.getConstructor(Exception.class)
					.newInstance(exception);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		}
	}
}
