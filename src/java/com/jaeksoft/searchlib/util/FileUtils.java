/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
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

package com.jaeksoft.searchlib.util;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.junit.Test;

public class FileUtils {

	public static class DirectoryOnly implements FileFilter {

		public boolean accept(File file) {
			return file.isDirectory();
		}

	}

	final public static void stream2file(InputStream is, File target)
			throws IOException {
		FileOutputStream fos = null;
		try {
			target.createNewFile();
			fos = new FileOutputStream(target);
			byte[] buffer = new byte[4096];
			int b;
			while ((b = is.read(buffer)) != -1)
				if (b > 0)
					fos.write(buffer, 0, b);
		} finally {
			if (fos != null)
				fos.close();
		}
	}

	final public static String getFileNameExtension(String fileName) {
		if (fileName == null)
			return null;
		int i = fileName.lastIndexOf('.');
		if (i == -1)
			return null;
		return fileName.substring(i + 1);
	}

	@Test
	public void test_path_multiple_dots() {
		String ext = getFileNameExtension("/dev.net/acro.bat/pdfs/pdf_open_parameters.pdf");
		assertTrue("pdf".equals(ext));
	}

	@Test
	public void test_path_multiple_one_dot() {
		String ext = getFileNameExtension("/devnet/acrobat/pdfs/pdf_open_parameters.pdf");
		assertTrue("pdf".equals(ext));
	}

	@Test
	public void test_path_no_dot() {
		String ext = getFileNameExtension("/devnet/acrobat/pdfs/pdf_open_parameters");
		assertTrue(ext == null);
	}

	@Test
	public void test_path_empty() {
		String ext = getFileNameExtension("");
		assertTrue(ext == null);
	}

	@Test
	public void test_path_null() {
		String ext = getFileNameExtension(null);
		assertTrue(ext == null);
	}

	public static String rewriteUTF8(String path)
			throws UnsupportedEncodingException {
		if (path == null)
			return null;

		return URLEncoder.encode(path, "UTF-8");
	}
}
