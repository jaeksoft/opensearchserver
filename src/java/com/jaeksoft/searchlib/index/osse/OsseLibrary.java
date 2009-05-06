/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.index.osse;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.WString;

public interface OsseLibrary extends Library {

	final public static int LOG_FATAL = 0, LOG_SEVERE = 1, LOG_WARNING = 2,
			LOG_INFO = 3, LOG_DEBUG = 4;

	public void logger_setLevel(int level);

	public Pointer document_new();

	public void document_delete(Pointer document);

	public void document_add(Pointer document, WString field, WString[] terms,
			int count);

	public Pointer index_new();

	public void index_delete(Pointer index);

	public long index_add(Pointer index, Pointer document);

	public void index_search(WString query, Pointer result);

	public Pointer result_new();

	void result_delete(Pointer result);

	float result_getScore(Pointer result, long pos);

	long result_getDocumentId(Pointer result, long pos);

	public void test();

	public OsseLibrary INSTANCE = (OsseLibrary) Native.loadLibrary(
			"OpenSearchServerEngine", OsseLibrary.class);

}
