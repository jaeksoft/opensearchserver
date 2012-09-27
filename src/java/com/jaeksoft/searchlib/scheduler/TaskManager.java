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

import java.io.IOException;
import java.text.ParseException;

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
import com.jaeksoft.searchlib.util.SimpleLock;

public class TaskManager implements Job {

	private final static SimpleLock lock = new SimpleLock();

	private static Scheduler scheduler = null;

	public static void start() throws SearchLibException {
		lock.rl.lock();
		try {
			scheduler = StdSchedulerFactory.getDefaultScheduler();
			scheduler.start();
		} catch (SchedulerException e) {
			throw new SearchLibException(e);
		} finally {
			lock.rl.unlock();
		}
	}

	public static void stop() throws SearchLibException {
		lock.rl.lock();
		try {
			if (scheduler != null) {
				scheduler.shutdown();
				scheduler = null;
			}
		} catch (SchedulerException e) {
			throw new SearchLibException(e);
		} finally {
			lock.rl.unlock();
		}
	}

	private static void checkSchedulerAvailable() throws SearchLibException {
		if (scheduler == null)
			throw new SearchLibException("The scheduler is not availalbe.");
	}

	public static void cronJob(String indexName, String jobName,
			TaskCronExpression cron) throws SearchLibException {
		lock.rl.lock();
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
			lock.rl.unlock();
		}
	}

	public static void executeJob(String indexName, String jobName)
			throws SearchLibException {
		lock.rl.lock();
		try {
			checkSchedulerAvailable();
			JobDetail jobDetail = new ImmediateTaskDetail(indexName, jobName,
					TaskManager.class);
			Trigger trigger = new SimpleTrigger(jobDetail.getName(),
					jobDetail.getGroup());
			scheduler.scheduleJob(jobDetail, trigger);
		} catch (SchedulerException e) {
			throw new SearchLibException(e);
		} finally {
			lock.rl.unlock();
		}
	}

	public static void executeTask(Client client, TaskItem taskItem)
			throws SearchLibException {
		lock.rl.lock();
		try {
			checkSchedulerAvailable();
			JobDetail jobDetail = new ImmediateTaskDetail(
					client.getIndexName(), taskItem, TaskManager.class);
			Trigger trigger = new SimpleTrigger(jobDetail.getName(),
					jobDetail.getGroup());
			scheduler.scheduleJob(jobDetail, trigger);
		} catch (SchedulerException e) {
			throw new SearchLibException(e);
		} finally {
			lock.rl.unlock();
		}
	}

	public static String[] getActiveJobs(String indexName)
			throws SearchLibException {
		lock.rl.lock();
		try {
			return scheduler.getJobNames(indexName);
		} catch (SchedulerException e) {
			throw new SearchLibException(e);
		} finally {
			lock.rl.unlock();
		}
	}

	public static void removeJob(String indexName, String jobName)
			throws SearchLibException {
		lock.rl.lock();
		try {
			scheduler.deleteJob(jobName, indexName);
		} catch (SchedulerException e) {
			throw new SearchLibException(e);
		} finally {
			lock.rl.unlock();
		}
	}

	public static void removeJobs(String indexName) throws SearchLibException {
		lock.rl.lock();
		try {
			String[] jobNames = scheduler.getJobNames(indexName);
			for (String jobName : jobNames)
				scheduler.deleteJob(jobName, indexName);
		} catch (SchedulerException e) {
			throw new SearchLibException(e);
		} finally {
			lock.rl.unlock();
		}

	}

	@Override
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		JobDetail jobDetail = context.getJobDetail();
		String indexName = jobDetail.getGroup();
		String jobName = null;
		TaskItem taskItem = null;
		if (jobDetail instanceof ImmediateTaskDetail) {
			jobName = ((ImmediateTaskDetail) jobDetail).getJobName();
			taskItem = ((ImmediateTaskDetail) jobDetail).getTaskItem();
		} else
			jobName = jobDetail.getName();
		try {
			Client client = ClientCatalog.getClient(indexName);
			if (client == null) {
				removeJobs(indexName);
				return;
			}
			if (jobName != null) {
				JobList jobList = client.getJobList();
				JobItem jobItem = jobList.get(jobName);
				if (jobItem == null)
					throw new SearchLibException("Job not found: " + jobName);
				jobItem.run(client);
			} else if (taskItem != null) {
				TaskLog taskLog = new TaskLog(taskItem, false);
				taskItem.run(client, taskLog);
			}
		} catch (SearchLibException e) {
			Logging.error(e);
		} catch (NamingException e) {
			Logging.error(e);
		} catch (IOException e) {
			Logging.error(e);
		}

	}
}
