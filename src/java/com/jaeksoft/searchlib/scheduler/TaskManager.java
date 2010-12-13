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

package com.jaeksoft.searchlib.scheduler;

import java.text.ParseException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import com.jaeksoft.searchlib.SearchLibException;

public class TaskManager {

	private final static Lock lock = new ReentrantLock();

	private static Scheduler scheduler = null;

	public static void start() throws SearchLibException {
		lock.lock();
		try {
			scheduler = StdSchedulerFactory.getDefaultScheduler();
			scheduler.start();
		} catch (SchedulerException e) {
			throw new SearchLibException(e);
		} finally {
			lock.unlock();
		}
	}

	public static void stop() throws SearchLibException {
		lock.lock();
		try {
			if (scheduler != null)
				scheduler.shutdown();
		} catch (SchedulerException e) {
			throw new SearchLibException(e);
		} finally {
			lock.unlock();
		}
	}

	public static void checkJob(String indexName, String jobName,
			TaskCronExpression cron) throws SearchLibException {
		lock.lock();
		try {
			// TODO CHECK IF IT ALREADY EXIST
			JobDetail job = new JobDetail(jobName, indexName, TaskManager.class);
			try {
				Trigger trigger = new CronTrigger(jobName, indexName,
						cron.getStringExpression());
				scheduler.scheduleJob(job, trigger);
			} catch (ParseException e) {
				throw new SearchLibException(e);
			} catch (SchedulerException e) {
				throw new SearchLibException(e);
			}
		} finally {
			lock.unlock();
		}
	}

	public static String[] getActiveJobs(String indexName)
			throws SearchLibException {
		lock.lock();
		try {
			return scheduler.getJobNames(indexName);
		} catch (SchedulerException e) {
			throw new SearchLibException(e);
		} finally {
			lock.unlock();
		}
	}

	public static void removeJob(String indexName, String jobName)
			throws SearchLibException {
		lock.lock();
		try {
			scheduler.deleteJob(indexName, jobName);
		} catch (SchedulerException e) {
			throw new SearchLibException(e);
		} finally {
			lock.unlock();
		}
	}
}
