/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2010 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.file.process;

import java.io.File;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.common.process.CrawlMasterAbstract;
import com.jaeksoft.searchlib.crawler.common.process.CrawlStatistics;
import com.jaeksoft.searchlib.crawler.common.process.CrawlStatus;
import com.jaeksoft.searchlib.crawler.common.process.CrawlThreadAbstract;
import com.jaeksoft.searchlib.crawler.file.database.FileCrawlQueue;
import com.jaeksoft.searchlib.crawler.file.database.FilePropertyManager;

public class CrawlFileMaster extends CrawlMasterAbstract {

	private int delayBetweenAccess;

	private CrawlFileIterator crawlFileIterator;

	public CrawlFileMaster(Config config) throws SearchLibException {
		super(config);
		if (config.getFilePropertyManager().getCrawlEnabled().getValue()) {
			System.out.println("Filecrawler is starting for "
					+ config.getIndexDirectory().getName());
			start();
		}
	}

	protected File getNextFile() {
		synchronized (crawlFileIterator) {
			sleepMs(delayBetweenAccess);
			return crawlFileIterator.next();
		}
	}

	@Override
	public void runner() throws Exception {
		Config config = getConfig();
		FilePropertyManager filePropertyManager = config
				.getFilePropertyManager();

		while (!isAborted()) {

			crawlQueue = new FileCrawlQueue(config, filePropertyManager);

			currentStats = new CrawlStatistics();
			addStatistics(currentStats);
			crawlQueue.setStatistiques(currentStats);

			int threadNumber = filePropertyManager.getMaxThreadNumber()
					.getValue();
			delayBetweenAccess = filePropertyManager.getDelayBetweenAccesses()
					.getValue();

			crawlFileIterator = new CrawlFileIterator(
					config.getFilePathManager());

			while (!isAborted()) {

				if (!crawlFileIterator.hasNext())
					break;

				CrawlThreadAbstract crawlThread = new CrawlFileThread(config,
						this, currentStats);
				add(crawlThread);

				while (getThreadsCount() >= threadNumber && !isAborted())
					sleepSec(5);
			}

			waitForChild(600);
			setStatus(CrawlStatus.INDEXATION);
			crawlQueue.index(true);
			if (currentStats.getUrlCount() > 0) {
				setStatus(CrawlStatus.OPTMIZING_INDEX);
				config.getUrlManager().reload(
						filePropertyManager.getOptimizeAfterSession()
								.getValue());
			}

			sleepSec(5);
		}
		crawlQueue.index(true);
		setStatus(CrawlStatus.NOT_RUNNING);
	}

}
