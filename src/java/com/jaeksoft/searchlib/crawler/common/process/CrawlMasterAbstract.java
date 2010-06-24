/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.common.process;

import java.lang.Thread.State;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.plugin.IndexPluginList;

public abstract class CrawlMasterAbstract extends CrawlThreadAbstract {

	private final LinkedHashSet<CrawlThreadAbstract> crawlThreads;

	private CrawlThreadAbstract[] crawlThreadArray;

	private final LinkedList<CrawlStatistics> statistics;

	protected CrawlQueueAbstract crawlQueue;

	private IndexPluginList indexPluginList;

	protected CrawlMasterAbstract(Config config) {
		super(config, null);
		crawlThreadArray = null;
		statistics = new LinkedList<CrawlStatistics>();
		crawlThreadArray = null;
		crawlQueue = null;
		crawlThreads = new LinkedHashSet<CrawlThreadAbstract>();
	}

	public void start() {
		if (isRunning())
			return;
		try {
			setStatus(CrawlStatus.STARTING);
			indexPluginList = new IndexPluginList(config
					.getIndexPluginTemplateList());
		} catch (SearchLibException e) {
			e.printStackTrace();
			setStatus(CrawlStatus.ERROR);
			setInfo(e.getMessage());
			return;
		}
		execute();
	}

	@Override
	public void abort() {
		synchronized (this) {
			synchronized (crawlThreads) {
				for (CrawlThreadAbstract crawlThread : crawlThreads)
					crawlThread.abort();
			}
			super.abort();
		}
	}

	protected int crawlThreadsSize() {
		synchronized (crawlThreads) {
			return crawlThreads.size();
		}
	}

	protected void add(CrawlThreadAbstract crawlThread) {
		synchronized (crawlThreads) {
			crawlThreads.add(crawlThread);
			crawlThreadArray = null;
		}
		crawlThread.execute();
	}

	public void remove(CrawlThreadAbstract crawlThread) {
		synchronized (crawlThreads) {
			crawlThreads.remove(crawlThread);
			crawlThreadArray = null;
		}
	}

	public CrawlThreadAbstract[] getCrawlThreads() {
		synchronized (crawlThreads) {
			if (crawlThreadArray != null)
				return crawlThreadArray;
			crawlThreadArray = new CrawlThreadAbstract[crawlThreads.size()];
			return crawlThreads.toArray(crawlThreadArray);
		}
	}

	protected void waitForChild() {
		while (crawlThreadsSize() > 0) {
			try {
				synchronized (this) {
					wait(5000);
				}
				// Remove terminated thread
				synchronized (crawlThreads) {
					boolean remove = false;
					Iterator<CrawlThreadAbstract> it = crawlThreads.iterator();
					while (it.hasNext()) {
						CrawlThreadAbstract crawlThread = it.next();
						if (crawlThread.getThreadState() == State.TERMINATED) {
							it.remove();
							remove = true;
						} else if (crawlThread.getCrawlTimeOutExhausted(300)) {
							crawlThread.abort();
							it.remove();
							remove = true;
						}
					}
					if (remove)
						crawlThreadArray = null;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			sleepSec(1);
		}
	}

	protected void addStatistics(CrawlStatistics stats) {
		synchronized (statistics) {
			if (statistics.size() >= 10)
				statistics.removeLast();
			statistics.addFirst(stats);
		}
	}

	public List<CrawlStatistics> getStatistics() {
		return statistics;
	}

	public IndexPluginList getIndexPluginList() {
		return indexPluginList;
	}

	@Override
	public void release() {
	}

	public CrawlQueueAbstract getCrawlQueue() {
		return crawlQueue;
	}

	@Override
	public String getCurrentInfo() {
		return "";
	}

}
