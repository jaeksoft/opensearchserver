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

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class Sequence {

	private ReadWriteLock rwl = new ReadWriteLock();

	private long counter = 0;

	private final File file;

	private final int radix;

	public Sequence(File file, int radix) throws NumberFormatException,
			IOException {
		this.radix = radix;
		this.file = file;
		if (file.exists())
			if (file.length() > 0)
				counter = Long.parseLong(FileUtils.readFileToString(file, "UTF-8"),
						radix);
	}

	public String next() throws IOException {
		rwl.w.lock();
		try {
			long next = counter + 1;
			String txt = Long.toString(next, radix);
			FileUtils.write(file, txt, "UTF-8");
			counter = next;
			return txt;
		} finally {
			rwl.w.unlock();
		}
	}
}
