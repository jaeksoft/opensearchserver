/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2014 Emmanuel Keller / Jaeksoft
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GhostScript {

	private final File gsExeFile;

	public GhostScript(String path) throws IOException {
		File f = checkPath(path);
		if (f == null)
			f = checkPath("/usr/bin/gs");
		if (f == null)
			f = checkPath("/usr/local/bin/gs");
		if (f == null)
			throw new IOException("GhostScript (gs) executable not found: "
					+ path);
		gsExeFile = f;
	}

	private File checkPath(String path) {
		if (StringUtils.isEmpty(path))
			return null;
		File f = new File(path);
		return f.exists() && f.isFile() && f.canExecute() ? f : null;
	}

	private List<String> getArgs(String device, Integer page,
			String pdfPassword, File outputFile, File pdfFile) {
		List<String> args = new ArrayList<String>();
		args.add(gsExeFile.getAbsolutePath());
		args.add("-dNOPAUSE");
		args.add("-dBATCH");
		args.add("-dSAFER");
		args.add("-sDEVICE=" + device);
		args.add("-dINTERPOLATE");
		args.add("-dNumRenderingThreads=8");
		if (page != null) {
			args.add("-dFirstPage=" + page);
			args.add("-dLastPage=" + page);
		}
		if (pdfPassword != null)
			args.add("-sPDFPassword=" + pdfPassword);
		args.add("-sOutputFile=" + outputFile.getAbsolutePath());
		return args;
	}

	private void execute(List<String> args, File pdfFile,
			StringBuilder returnedText) throws IOException,
			InterruptedException {
		args.add(pdfFile.getAbsolutePath());
		ExecuteUtils.run(args, 180, returnedText, 0);
	}

	public void generateImage(String password, Integer page, File pdfFile,
			int resolution, File imageFile) throws IOException,
			InterruptedException {
		List<String> args = getArgs("png16m", page, password, imageFile,
				pdfFile);
		args.add("-dTextAlphaBits=4");
		args.add("-dGraphicsAlphaBits=4");
		args.add("-r" + resolution);
		StringBuilder returnedText = new StringBuilder();
		execute(args, pdfFile, returnedText);
		if (imageFile.length() == 0)
			throw new IOException("Ghosscript did not generate the image: "
					+ returnedText);
	}

	public void extractText(String pdfPassword, File pdfFile, File textFile)
			throws IOException, InterruptedException {
		List<String> args = getArgs("txtwrite", null, pdfPassword, textFile,
				pdfFile);
		args.add("-dTextFormat=3");
		execute(args, pdfFile, null);
	}
}
