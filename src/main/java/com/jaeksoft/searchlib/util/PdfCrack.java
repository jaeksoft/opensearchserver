/**   
 * License Agreement for OpenSearchServer
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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import org.apache.commons.exec.ExecuteException;

public class PdfCrack {

	private final static String FOUND_USER_PASSWORD = "found user-password: '";

	public final static String findPassword(String pdfCrackCommandLine,
			File file) throws ExecuteException, IOException {
		ByteArrayOutputStream baos = null;
		BufferedReader br = null;
		try {
			baos = new ByteArrayOutputStream();
			ExecuteUtils.command(null, pdfCrackCommandLine, false, baos,
					3600000L, "-f",
					StringUtils.fastConcat("\"", file.getAbsolutePath(), "\""));
			br = new BufferedReader(new StringReader(baos.toString()));
			String line;
			int start = FOUND_USER_PASSWORD.length();
			while ((line = br.readLine()) != null) {
				if (line.startsWith(FOUND_USER_PASSWORD)) {
					int end = line.length() - 1;
					return end == start ? "" : line.substring(start, end);
				}
			}
			return null;
		} finally {
			IOUtils.close(baos, br);
		}
	}
}
