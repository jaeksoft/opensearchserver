/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2011 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.web.controller.runtime;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zul.Filedownload;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.web.controller.CommonController;

public class LogsController extends CommonController {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5390275562492575445L;

	private String selectedFile;

	private String currentLog;

	private String[] logFileList;

	public LogsController() throws SearchLibException {
		super();
		reset();
	}

	@Override
	protected void reset() throws SearchLibException {
		selectedFile = null;
		currentLog = null;
		logFileList = null;
	}

	private final static String[] buildOrderedLogFiles()
			throws SearchLibException {
		File[] files = Logging.getLogFiles();
		if (files == null)
			return null;
		Arrays.sort(files, new Comparator<File>() {
			@Override
			public int compare(File f1, File f2) {
				Long l1 = f1.lastModified();
				Long l2 = f2.lastModified();
				return l2.compareTo(l1);
			}
		});
		String[] names = new String[files.length];
		int i = 0;
		for (File file : files)
			names[i++] = file.getName();
		return names;
	}

	public String[] getLogFiles() throws SearchLibException {
		if (logFileList == null)
			logFileList = buildOrderedLogFiles();
		return logFileList;
	}

	public String getCurrentLog() throws IOException, SearchLibException {
		if (currentLog == null && selectedFile != null)
			currentLog = Logging.readLogs(10000, selectedFile);
		return currentLog;
	}

	public void setSelectedFile(String file) throws WrongValueException,
			IOException {
		this.selectedFile = file;
		this.currentLog = null;
		reloadComponent("logview");
	}

	public String getSelectedFile() {
		return selectedFile;
	}

	public boolean isFileNotSelected() {
		return selectedFile == null;
	}

	public void onReloadFileList() throws SearchLibException {
		reset();
		reloadPage();
	}

	public void onReloadCurrentLog() {
		currentLog = null;
		reloadComponent("logview");
	}

	public void onDownload() throws IOException, SearchLibException {
		String filePath = Logging.getLogDirectory() + File.separator
				+ getSelectedFile();
		Filedownload.save(new File(filePath), "text/plain");
	}

}
