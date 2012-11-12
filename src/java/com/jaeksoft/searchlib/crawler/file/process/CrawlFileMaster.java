/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.file.process;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedList;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.common.process.CrawlMasterAbstract;
import com.jaeksoft.searchlib.crawler.common.process.CrawlQueueAbstract;
import com.jaeksoft.searchlib.crawler.common.process.CrawlStatistics;
import com.jaeksoft.searchlib.crawler.common.process.CrawlStatus;
import com.jaeksoft.searchlib.crawler.common.process.CrawlThreadAbstract;
import com.jaeksoft.searchlib.crawler.file.database.FileCrawlQueue;
import com.jaeksoft.searchlib.crawler.file.database.FilePathItem;
import com.jaeksoft.searchlib.crawler.file.database.FilePathManager;
import com.jaeksoft.searchlib.crawler.file.database.FilePropertyManager;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.scheduler.TaskManager;

public class CrawlFileMaster extends CrawlMasterAbstract {

	private FileCrawlQueue fileCrawlQueue;

	private final LinkedList<FilePathItem> filePathList;

	public CrawlFileMaster(Config config) throws SearchLibException {
		super(config);
		FilePropertyManager filePropertyManager = config
				.getFilePropertyManager();
		fileCrawlQueue = new FileCrawlQueue(config, filePropertyManager);
		filePathList = new LinkedList<FilePathItem>();
		if (filePropertyManager.getCrawlEnabled().getValue()) {
			Logging.info("The file crawler is starting for "
					+ config.getIndexName());
			start(false);
		}
	}

	@Override
	public void runner() throws Exception {
		Config config = getConfig();
		FilePropertyManager propertyManager = config.getFilePropertyManager();

		while (!isAborted()) {

			currentStats = new CrawlStatistics();
			addStatistics(currentStats);
			fileCrawlQueue.setStatistiques(currentStats);

			int threadNumber = propertyManager.getMaxThreadNumber().getValue();
			String schedulerJobName = propertyManager
					.getSchedulerAfterSession().getValue();

			synchronized (filePathList) {
				filePathList.clear();
			}

			extractFilePathList();

			while (!isAborted()) {

				FilePathItem filePathItem = getNextFilePathItem();
				if (filePathItem == null)
					break;

				CrawlThreadAbstract crawlThread = new CrawlFileThread(config,
						this, currentStats, filePathItem);
				add(crawlThread);

				while (getThreadsCount() >= threadNumber && !isAborted())
					sleepSec(5);
			}

			waitForChild(600);
			setStatus(CrawlStatus.INDEXATION);
			fileCrawlQueue.index(true);
			if (fileCrawlQueue.hasContainedData()) {
				setStatus(CrawlStatus.OPTIMIZATION);
				config.getFileManager().reload(true, null);
			}

			if (schedulerJobName != null && schedulerJobName.length() > 0) {
				setStatus(CrawlStatus.EXECUTE_SCHEDULER_JOB);
				TaskManager.executeJob(config.getIndexName(), schedulerJobName);
			}

			if (isOnce())
				break;
			sleepSec(5);
		}
		fileCrawlQueue.index(true);
		setStatus(CrawlStatus.NOT_RUNNING);
	}

	private void extractFilePathList() throws IOException, ParseException,
			SyntaxError, URISyntaxException, ClassNotFoundException,
			InterruptedException, SearchLibException, InstantiationException,
			IllegalAccessException {
		Config config = getConfig();
		setStatus(CrawlStatus.EXTRACTING_FILEPATHLIST);

		FilePathManager filePathManager = config.getFilePathManager();

		filePathManager.getFilePathsToFetch(filePathList);
		currentStats.addOldHostListSize(filePathList.size());
	}

	private FilePathItem getNextFilePathItem() {
		synchronized (filePathList) {
			int s = filePathList.size();
			if (s == 0)
				return null;
			FilePathItem filePathItem = filePathList.remove(0);
			if (filePathItem == null)
				return null;
			currentStats.incNewHostCount();
			return filePathItem;
		}
	}

	@Override
	public CrawlQueueAbstract getCrawlQueue() {
		return fileCrawlQueue;
	}

}
