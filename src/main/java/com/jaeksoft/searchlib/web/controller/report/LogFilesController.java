/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.web.controller.report;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.zul.Messagebox;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.util.FileUtils;
import com.jaeksoft.searchlib.web.controller.AlertController;

public class LogFilesController extends ReportsController {

	public static class ReportFile {

		private String name;

		private Date date;

		private ReportFile(File file) {
			name = file.getName();
			date = new Date(file.lastModified());
		}

		public String getName() {
			return name;
		}

		public Date getDate() {
			return date;
		}

	}

	public LogFilesController() throws SearchLibException {
		super();
		reset();
	}

	@Override
	protected void reset() throws SearchLibException {
	}

	public ReportFile[] getReportList() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		File[] files = client.getReportsManager().getReportsList();
		FileUtils.sortByLastModified(files, true);
		ReportFile[] reportFile = new ReportFile[files.length];
		int i = 0;
		for (File file : files)
			reportFile[i++] = new ReportFile(file);
		return reportFile;
	}

	@Command
	public void onLoad(@BindingParam("reportFile") ReportFile reportFile)
			throws InterruptedException, UnsupportedEncodingException,
			IOException, SearchLibException {
		int noOfLine = getReportsManager().loadReportFile(reportFile.getName());
		new AlertController("The file has been Loaded with " + noOfLine
				+ " of Lines");
		reload();
	}

	@Command
	public void onArchive(@BindingParam("reportFile") ReportFile reportFile)
			throws SearchLibException, InterruptedException, IOException {
		getLogReportManager().archiveFile(reportFile.getName());
		new AlertController("Log file archived successfully");
		reload();
	}

	@Command
	public void onDelete(@BindingParam("reportFile") ReportFile reportFile)
			throws SearchLibException, InterruptedException {
		new DeleteReportFileAlert(reportFile.getName());
	}

	private class DeleteReportFileAlert extends AlertController {

		private transient String reportFileName;

		protected DeleteReportFileAlert(String reportFileName)
				throws InterruptedException {
			super("Please, confirm that you want to delete the reports file "
					+ reportFileName
					+ ". The File will be deleted from the filesystem",
					Messagebox.YES | Messagebox.NO, Messagebox.QUESTION);
			this.reportFileName = reportFileName;
		}

		@Override
		protected void onYes() throws SearchLibException, InterruptedException {
			try {
				getLogReportManager().deleteFile(reportFileName);
				new AlertController("Log file has been successfully deleted");
				reload();
			} catch (IOException e) {
				throw new SearchLibException(e);
			}
		}

	}
}