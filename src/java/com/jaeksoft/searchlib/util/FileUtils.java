/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft SearchLib Community.
 *
 * Jaeksoft SearchLib Community is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft SearchLib Community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft SearchLib Community. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.util;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FileUtils {

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
}
