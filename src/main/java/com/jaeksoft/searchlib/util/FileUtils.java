/*
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2011-2017 Emmanuel Keller / Jaeksoft
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
 */

package com.jaeksoft.searchlib.util;

import com.jaeksoft.searchlib.Logging;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Comparator;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class FileUtils extends org.apache.commons.io.FileUtils {

	public final static class LastModifiedAscComparator implements Comparator<File> {
		@Override
		final public int compare(File f1, File f2) {
			Long l1 = f1.lastModified();
			Long l2 = f2.lastModified();
			return l1.compareTo(l2);
		}
	}

	public final static class LastModifiedDescComparator implements Comparator<File> {
		@Override
		final public int compare(File f1, File f2) {
			Long l1 = f1.lastModified();
			Long l2 = f2.lastModified();
			return l2.compareTo(l1);
		}
	}

	public static File[] sortByLastModified(File[] files, boolean desc) {
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

	public static boolean isSubDirectory(File base, File child) throws IOException {
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

	public static String computeMd5(File file) {
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

	public static File createTempDirectory(String prefix, String suffix) throws IOException {
		final File temp;

		temp = File.createTempFile(prefix, suffix);

		if (!temp.delete())
			throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());

		if (!temp.mkdir())
			throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());

		return temp;
	}

	public static void deleteDirectoryQuietly(File directory) {
		try {
			deleteDirectory(directory);
		} catch (IOException e) {
			Logging.warn(e);
		}
	}

	public static OutputStream writeToGzipFile(final File file) throws IOException {
		return new GZIPOutputStream(new FileOutputStream(file), 65536);
	}

	public static int writeToGzipFile(final InputStream input, final File file) throws IOException {
		try (final OutputStream output = writeToGzipFile(file)) {
			return IOUtils.copy(input, output);
		}
	}

	public static int writeStringToGzipFile(final String data, final Charset charset, final File file)
			throws IOException {
		try (final InputStream stream = new ByteArrayInputStream(data.getBytes(charset))) {
			return writeToGzipFile(stream, file);
		}
	}

	public static InputStream readFromGzipFile(final File file) throws IOException {
		return new GZIPInputStream(new FileInputStream(file), 65536);
	}

	public static String readStringFromGzipFile(final Charset charset, final File file) throws IOException {
		try (final InputStream input = readFromGzipFile(file)) {
			return IOUtils.toString(input, charset);
		}
	}

}
