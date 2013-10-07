/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2013 Emmanuel Keller / Jaeksoft
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import com.jaeksoft.searchlib.Logging;

public class FilesUtils {

	public final static class LastModifiedAscComparator implements
			Comparator<File> {
		@Override
		final public int compare(File f1, File f2) {
			Long l1 = f1.lastModified();
			Long l2 = f2.lastModified();
			return l1.compareTo(l2);
		}
	}

	public final static class LastModifiedDescComparator implements
			Comparator<File> {
		@Override
		final public int compare(File f1, File f2) {
			Long l1 = f1.lastModified();
			Long l2 = f2.lastModified();
			return l2.compareTo(l1);
		}
	}

	public final static File[] sortByLastModified(File[] files, boolean desc) {
		if (desc)
			Arrays.sort(files, new LastModifiedDescComparator());
		else
			Arrays.sort(files, new LastModifiedAscComparator());
		return files;
	}

	public static String systemPathToUnix(String filePath) {
		if ("\\".equals(File.separator))
			filePath = FilenameUtils.separatorsToUnix(filePath);
		return filePath;
	}

	public static String unixToSystemPath(String filePath) {
		if ("\\".equals(File.separator))
			filePath = FilenameUtils.separatorsToWindows(filePath);
		return filePath;
	}

	public final static boolean isSubDirectory(File base, File child)
			throws IOException {
		base = base.getCanonicalFile();
		child = child.getCanonicalFile();
		File parent = child;
		while (parent != null) {
			if (base.equals(parent))
				return true;
			parent = parent.getParentFile();
		}
		return false;
	}

	public final static String computeMd5(File file) {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			return DigestUtils.md5Hex(fis);
		} catch (IOException e) {
			Logging.warn(e);
			return null;
		} finally {
			if (fis != null)
				IOUtils.closeQuietly(fis);
		}
	}

}
