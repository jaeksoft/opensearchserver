/**
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2008-2016 Emmanuel Keller / Jaeksoft
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
 **/

package com.jaeksoft.searchlib.crawler.file.process;

import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.common.process.CrawlMasterAbstract;
import com.jaeksoft.searchlib.crawler.common.process.CrawlQueueAbstract;
import com.jaeksoft.searchlib.crawler.common.process.CrawlStatistics;
import com.jaeksoft.searchlib.crawler.common.process.CrawlStatus;
import com.jaeksoft.searchlib.crawler.file.database.*;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.scheduler.TaskManager;
import com.jaeksoft.searchlib.util.InfoCallback;
import org.apache.http.HttpException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;

public class CrawlFileMaster extends CrawlMasterAbstract<CrawlFileMaster, CrawlFileThread> {

	private FileCrawlQueue fileCrawlQueue;

	private final LinkedList<FilePathItem> filePathList;

	public CrawlFileMaster(Config config) throws IOException {
		super(config);
		FilePropertyManager filePropertyManager = config.getFilePropertyManager();
		fileCrawlQueue = new FileCrawlQueue(config);
		filePathList = new LinkedList<>();
		if (filePropertyManager.getCrawlEnabled().getValue()) {
			Logging.info("The file crawler is starting for " + config.getIndexName());
			start(false);
		}
	}

	@Override
	public void runner() throws Exception {
		Config config = getConfig();
		FilePropertyManager propertyManager = config.getFilePropertyManager();

		fileCrawlQueue.setMaxBufferSize(propertyManager.getIndexDocumentBufferSize().getValue());

		if (ClientFactory.INSTANCE.properties.isDisableFileCrawler()) {
			abort();
			propertyManager.getCrawlEnabled().setValue(false);
			throw new InterruptedException("The webcrawler is disabled.");
		}

		while (!isAborted()) {

			currentStats = new CrawlStatistics();
			addStatistics(currentStats);
			fileCrawlQueue.setStatistiques(currentStats);

			int threadNumber = propertyManager.getMaxThreadNumber().getValue();
			String schedulerJobName = propertyManager.getSchedulerAfterSession().getValue();

			synchronized (filePathList) {
				filePathList.clear();
			}

			extractFilePathList();

			while (!isAborted()) {

				FilePathItem filePathItem = getNextFilePathItem();
				if (filePathItem == null)
					break;

				CrawlFileThread crawlThread = new CrawlFileThread(config, this, currentStats, filePathItem, null);
				add(crawlThread);

				while (getThreadsCount() >= threadNumber && !isAborted())
					sleepSec(5);
			}
			setStatus(CrawlStatus.WAITING_CHILD);
			while (getThreadsCount() > 0) {
				waitForChild(1800);
				if (isAborted())
					break;
			}
			setStatus(CrawlStatus.INDEXATION);
			fileCrawlQueue.index(true);

			if (schedulerJobName != null && schedulerJobName.length() > 0) {
				setStatus(CrawlStatus.EXECUTE_SCHEDULER_JOB);
				TaskManager.getInstance().executeJob(config.getIndexName(), schedulerJobName);
			}

			if (isOnce())
				break;
			sleepSec(5);
		}
		fileCrawlQueue.index(true);
		setStatus(CrawlStatus.NOT_RUNNING);
	}

	public void crawlDirectory(final FilePathItem filePathItem, final String path, final InfoCallback infoCallback)
			throws SearchLibException, NoSuchAlgorithmException, InstantiationException, IllegalAccessException,
			ClassNotFoundException, URISyntaxException, IOException, HttpException, InterruptedException {
		final Config config = getConfig();
		final FilePropertyManager propertyManager = config.getFilePropertyManager();
		fileCrawlQueue.setMaxBufferSize(propertyManager.getIndexDocumentBufferSize().getValue());
		final CrawlFileThread crawlThread = new CrawlFileThread(getConfig(), this, null, filePathItem, infoCallback);
		FileInstanceAbstract fileInstance = FileInstanceAbstract.create(filePathItem, null, path);
		if (fileInstance.getFileType() != FileTypeEnum.directory)
			return;
		crawlThread.browse(fileInstance, 1);
		fileCrawlQueue.index(true);
	}

	private void extractFilePathList()
			throws IOException, ParseException, SyntaxError, URISyntaxException, ClassNotFoundException,
			InterruptedException, SearchLibException, InstantiationException, IllegalAccessException {
		Config config = getConfig();
		setStatus(CrawlStatus.EXTRACTING_FILEPATHLIST);

		FilePathManager filePathManager = config.getFilePathManager();

		filePathManager.getFilePathsToFetch(filePathList);
		currentStats.addHostListSize(filePathList.size());
	}

	private FilePathItem getNextFilePathItem() {
		synchronized (filePathList) {
			int s = filePathList.size();
			if (s == 0)
				return null;
			FilePathItem filePathItem = filePathList.remove(0);
			if (filePathItem == null)
				return null;
			currentStats.incHostCount();
			return filePathItem;
		}
	}

	public CrawlQueueAbstract getCrawlQueue() {
		return fileCrawlQueue;
	}

	@Override
	protected CrawlFileThread[] getNewArray(int size) {
		return new CrawlFileThread[size];
	}

}
