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
import java.util.ArrayList;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.ForwardEvent;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.report.ReportList;
import com.jaeksoft.searchlib.report.ReportsManager;
import com.jaeksoft.searchlib.util.FilesUtils;
import com.jaeksoft.searchlib.web.controller.AlertController;

public class LogFilesController extends ReportsController {

	private static final long serialVersionUID = -3080076527069942361L;

	private String date;
	private String[] dates;
	private String filename;

	public LogFilesController() throws SearchLibException {
		super();
		reset();
	}

	@Override
	public void doAfterCompose(Component component) throws Exception {
		super.doAfterCompose(component);
	}

	@Override
	protected void reset() throws SearchLibException {
		date = null;
		dates = null;
		filename = null;
	}

	public ListModelList<ReportList> getReportList() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		List<ReportList> logfileList = new ArrayList<ReportList>();
		File[] files = client.getReportsManager().getReportsList();
		FilesUtils.sortByLastModified(files, true);
		for (File logfile : files)
			logfileList.add(new ReportList(logfile.getName(), getDate(logfile
					.getName())));
		ListModelList<ReportList> listModel = new ListModelList<ReportList>();
		listModel.addAll(logfileList);
		return listModel;
	}

	public String getDate(String filename) {
		dates = filename.split("\\.");
		if (dates.length > 2)
			date = dates[2];
		return date;
	}

	private Event getOrigin(Event event) {
		Event origin;
		if (event instanceof ForwardEvent) {
			origin = Events.getRealOrigin((ForwardEvent) event);
		} else {
			origin = event;
		}
		return origin;
	}

	public void onClick$loadReport(Event event) throws InterruptedException,
			SearchLibException, UnsupportedEncodingException, IOException {
		Event origin = getOrigin(event);
		filename = (String) origin.getTarget().getAttribute("reportid");
		int noOfLine = getReportsManager().loadReportFile(filename);
		new AlertController("The file has been Loaded with " + noOfLine
				+ " of Lines");
	}

	private String getCurrentReportFileName(Event event)
			throws SearchLibException {
		ReportsManager reports = getReportsManager();
		if (reports == null)
			return null;
		Event origin;
		if (event instanceof ForwardEvent)
			origin = Events.getRealOrigin((ForwardEvent) event);
		else
			origin = event;
		return (String) origin.getTarget().getAttribute("reportid");
	}

	public void onClick$archiveReport(Event event) throws SearchLibException,
			InterruptedException, IOException {
		String filename = getCurrentReportFileName(event);
		if (getLogReportManager().archiveFile(filename)) {
			new AlertController("Log file archived successfully");
			reloadReportsList();
		}
	}

	private void reloadReportsList() {
		reloadPage();
	}

	public void onClick$deleteReport(Event event) throws SearchLibException,
			InterruptedException {
		new DeleteReportFileAlert(getCurrentReportFileName(event));
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
			boolean isDeleted = getLogReportManager()
					.deleteFile(reportFileName);
			if (isDeleted) {
				new AlertController("Log file has been successfully deleted");
				reloadReportsList();
			} else
				new AlertController(
						"File cannot be deleted please check the file permission");
		}

	}
}