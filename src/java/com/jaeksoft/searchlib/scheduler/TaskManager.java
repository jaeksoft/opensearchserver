/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.scheduler;

import java.text.ParseException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.naming.NamingException;

import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;

public class TaskManager implements Job {

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
			if (scheduler != null) {
				scheduler.shutdown();
				scheduler = null;
			}
		} catch (SchedulerException e) {
			throw new SearchLibException(e);
		} finally {
			lock.unlock();
		}
	}

	private static void checkSchedulerAvailable() throws SearchLibException {
		if (scheduler == null)
			throw new SearchLibException("The scheduler is not availalbe.");
	}

	public static void cronJob(String indexName, String jobName,
			TaskCronExpression cron) throws SearchLibException {
		lock.lock();
		try {
			checkSchedulerAvailable();
			Trigger trigger = new CronTrigger(jobName, indexName,
					cron.getStringExpression());

			// CHECK IF IT ALREADY EXIST
			JobDetail jobDetail = scheduler.getJobDetail(jobName, indexName);
			if (jobDetail != null) {
				Trigger[] triggers = scheduler.getTriggersOfJob(jobName,
						indexName);
				if (triggers != null) {
					for (Trigger tr : triggers) {
						if (tr instanceof CronTrigger) {
							CronTrigger ctr = (CronTrigger) tr;
							if (ctr.getCronExpression().equals(
									cron.getStringExpression()))
								return;
						}
					}
				}
				scheduler.deleteJob(jobName, indexName);
			}

			JobDetail job = new JobDetail(jobName, indexName, TaskManager.class);
			scheduler.scheduleJob(job, trigger);
		} catch (ParseException e) {
			throw new SearchLibException(e);
		} catch (SchedulerException e) {
			throw new SearchLibException(e);
		} finally {
			lock.unlock();
		}
	}

	public static void executeJob(String indexName, String jobName)
			throws SearchLibException {
		lock.lock();
		try {
			checkSchedulerAvailable();
			Trigger trigger = new SimpleTrigger(jobName, indexName);
			JobDetail job = new JobDetail(jobName, indexName, TaskManager.class);
			scheduler.scheduleJob(job, trigger);
		} catch (SchedulerException e) {
			throw new SearchLibException(e);
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
			scheduler.deleteJob(jobName, indexName);
		} catch (SchedulerException e) {
			throw new SearchLibException(e);
		} finally {
			lock.unlock();
		}
	}

	public static void removeJobs(String indexName) throws SearchLibException {
		lock.lock();
		try {
			String[] jobNames = scheduler.getJobNames(indexName);
			for (String jobName : jobNames)
				scheduler.deleteJob(jobName, indexName);
		} catch (SchedulerException e) {
			throw new SearchLibException(e);
		} finally {
			lock.unlock();
		}

	}

	@Override
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		JobDetail detail = context.getJobDetail();
		String indexName = detail.getGroup();
		String jobName = detail.getName();
		try {
			Client client = ClientCatalog.getClient(indexName);
			if (client == null) {
				removeJobs(indexName);
				return;
			}
			JobList jobList = client.getJobList();
			JobItem jobItem = jobList.get(jobName);
			if (jobItem != null)
				jobItem.run(client);
		} catch (SearchLibException e) {
			Logging.error(e);
		} catch (NamingException e) {
			Logging.error(e);
		}

	}
}
