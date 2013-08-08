/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.scheduler.task;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.logreport.LogReportManager;
import com.jaeksoft.searchlib.report.ReportsManager;
import com.jaeksoft.searchlib.scheduler.TaskAbstract;
import com.jaeksoft.searchlib.scheduler.TaskLog;
import com.jaeksoft.searchlib.scheduler.TaskProperties;
import com.jaeksoft.searchlib.scheduler.TaskPropertyDef;
import com.jaeksoft.searchlib.scheduler.TaskPropertyType;
import com.jaeksoft.searchlib.util.FormatUtils.ThreadSafeDateFormat;
import com.jaeksoft.searchlib.util.FormatUtils.ThreadSafeSimpleDateFormat;
import com.jaeksoft.searchlib.utils.Variables;

public class TaskReportLoadLogFile extends TaskAbstract {

	final private TaskPropertyDef propLogFile = new TaskPropertyDef(
			TaskPropertyType.comboBox, "Log file selection",
			"Log file selection", null, 60);

	final private TaskPropertyDef propArchive = new TaskPropertyDef(
			TaskPropertyType.listBox, "Archive", "Archive", null, 10);

	final private TaskPropertyDef[] taskPropertyDefs = { propLogFile,
			propArchive };

	private String[] logSelectionValues = { "Today's file", "Yesterday's file",
			"All file previous to today" };

	private String[] archiveValues = { "Yes", "No" };

	@Override
	public String getName() {
		return "Reports - load log file";
	}

	@Override
	public TaskPropertyDef[] getPropertyList() {
		return taskPropertyDefs;
	}

	@Override
	public String[] getPropertyValues(Config config, TaskPropertyDef property,
			TaskProperties taskProperties) throws SearchLibException {
		if (propLogFile == property) {
			return logSelectionValues;
		} else if (propArchive == property)
			return archiveValues;
		return null;
	}

	@Override
	public String getDefaultValue(Config arg0, TaskPropertyDef property) {
		if (propLogFile == property) {
			return logSelectionValues[0];
		} else if (propArchive == property)
			return archiveValues[0];
		return null;
	}

	@Override
	public void execute(Client client, TaskProperties properties,
			Variables variables, TaskLog taskLog) throws SearchLibException {

		Calendar currentDate = Calendar.getInstance();

		try {
			boolean archive = false;
			String archiveOrNot = properties.getValue(propArchive);
			String logSelection = properties.getValue(propLogFile);
			if (archiveOrNot.equalsIgnoreCase("Yes")) {
				archive = true;
			} else if (archiveOrNot.equalsIgnoreCase("No")) {
				archive = false;
			}

			if (logSelection.equalsIgnoreCase("Today's file")) {

				executeSchedulerTask(currentDate, client, archive, false,
						taskLog);

			} else if (logSelection.equalsIgnoreCase("Yesterday's file")) {
				currentDate.add(Calendar.DATE, -1);
				executeSchedulerTask(currentDate, client, archive, false,
						taskLog);
			} else if (logSelection
					.equalsIgnoreCase("All file previous to today")) {
				executeSchedulerTask(currentDate, client, archive, true,
						taskLog);
			}
		} catch (ParseException e) {

			Logging.error(e);
		} catch (IOException e) {

			Logging.error(e);
		}
	}

	private final static ThreadSafeDateFormat dateFormatter = new ThreadSafeSimpleDateFormat(
			"yyyy-MM-dd");

	public void executeSchedulerTask(Calendar date, Client client,
			boolean archive, boolean allfiles, TaskLog taskLog)
			throws ParseException, SearchLibException, IOException {
		LogReportManager logReport = client.getLogReportManager();
		ReportsManager reports = client.getReportsManager();
		File[] fileLists = reports.getReportsList();
		Calendar reportFileDate = Calendar.getInstance();
		String replaceString = "report." + client.getIndexName() + ".";
		for (File logfile : fileLists) {
			String reportFileName = logfile.getName();
			String reportFileDateString = logfile.getName().replace(
					replaceString, "");
			taskLog.setInfo("Working on " + reportFileName);
			if (taskLog.isAbortRequested())
				throw new SearchLibException.AbortException();
			Date fromReportDate = dateFormatter.parse(reportFileDateString);
			reportFileDate.setTime(fromReportDate);
			boolean sameDay = date.get(Calendar.YEAR) == reportFileDate
					.get(Calendar.YEAR)
					&& date.get(Calendar.DAY_OF_YEAR) == reportFileDate
							.get(Calendar.DAY_OF_YEAR);
			if (allfiles && !sameDay)
				continue;
			if (allfiles) {
				if (sameDay)
					continue;
			} else if (!sameDay)
				continue;
			reports.loadReportFile(reportFileName);
			if (archive)
				logReport.archiveFile(reportFileName);
		}
	}
}
