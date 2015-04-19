/**
 * License Agreement for OpenSearchServer
 * 
 * Copyright (C) 2014 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of OpenSearchServer.
 * 
 * OpenSearchServer is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * OpenSearchServer is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * OpenSearchServer. If not, see <http://www.gnu.org/licenses/>.
 **/
package com.jaeksoft.searchlib.util;

public class NativeOss {

	public final static String NATIVE_OSS_LIBNAME = "nativeoss";
	public final static String NATIVE_OSS_MAPPED_LIBNAME = System
			.mapLibraryName(NATIVE_OSS_LIBNAME);

	private static Boolean LOADED = null;

	public synchronized final static boolean loaded() {
		if (LOADED != null)
			return LOADED;
		try {
			System.loadLibrary(NATIVE_OSS_LIBNAME);
			System.out.println("Native OSS loaded ("
					+ NATIVE_OSS_MAPPED_LIBNAME + ")");
			LOADED = true;
		} catch (Throwable t) {
			System.err.println("Native OSS not found ("
					+ (NATIVE_OSS_MAPPED_LIBNAME) + "): " + t.getMessage()
					+ " - " + t.getClass().getSimpleName());
			LOADED = false;
		}
		return LOADED;
	}

}
