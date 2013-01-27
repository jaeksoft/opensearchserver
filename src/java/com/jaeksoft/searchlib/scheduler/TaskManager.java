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

package com.jaeksoft.searchlib.scheduler;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobKey.jobKey;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.impl.matchers.GroupMatcher.jobGroupEquals;

import java.util.List;
import java.util.Set;

import javax.naming.NamingException;

import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.DirectSchedulerFactory;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.util.SimpleLock;
import com.jaeksoft.searchlib.web.StartStopListener;

public class TaskManager implements Job {

	private final static SimpleLock lock = new SimpleLock();

	private static Scheduler scheduler = null;

	private static DirectSchedulerFactory schedulerFactory = null;

	public static void start() throws SearchLibException {
		lock.rl.lock();
		try {
			if (scheduler == null) {
				if (schedulerFactory == null)
					schedulerFactory = DirectSchedulerFactory.getInstance();
				schedulerFactory.createVolatileScheduler(50);
				scheduler = schedulerFactory.getScheduler();
			}
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
			Trigger trigger = newTrigger().withIdentity(jobName, indexName)
					.withSchedule(cronSchedule(cron.getStringExpression()))
					.build();

			// CHECK IF IT ALREADY EXIST
			JobKey jobKey = jobKey(jobName, indexName);
			if (scheduler.checkExists(jobKey)) {
				List<? extends Trigger> triggers = scheduler
						.getTriggersOfJob(jobKey);
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
				scheduler.deleteJob(jobKey);
			}

			JobDetail job = newJob(TaskManager.class).withIdentity(jobName,
					indexName).build();
			scheduler.scheduleJob(job, trigger);
		} catch (SchedulerException e) {
			throw new SearchLibException(e);
		} finally {
			lock.rl.unlock();
		}
	}

	/**
	 * Synchronous execution of a full Job
	 * 
	 * @param indexName
	 * @param jobName
	 * @throws SearchLibException
	 * @throws NamingException
	 */
	public static void executeJob(String indexName, String jobName)
			throws SearchLibException, NamingException {
		Client client = ClientCatalog.getClient(indexName);
		if (client == null)
			throw new SearchLibException("Client not found: " + indexName);
		JobList jobList = client.getJobList();
		JobItem jobItem = jobList.get(jobName);
		if (jobItem == null)
			throw new SearchLibException("Job not found: " + jobName);
		jobItem.run(client);
	}

	/**
	 * Asynchronous execution of a full Job
	 * 
	 * @param client
	 * @param jobItem
	 * @return
	 * @throws InterruptedException
	 */
	public static ImmediateExecution executeJob(Client client, JobItem jobItem)
			throws InterruptedException {
		ImmediateExecution execution = new ImmediateExecution(client, jobItem);
		client.getThreadPool().execute(execution);
		jobItem.waitForStart(600);
		return execution;
	}

	/**
	 * Asynchronous execution of a Task
	 * 
	 * @param client
	 * @param taskItem
	 * @param taskLog
	 * @return
	 * @throws InterruptedException
	 */
	public static ImmediateExecution executeTask(Client client,
			TaskItem taskItem, TaskLog taskLog) throws InterruptedException {
		if (taskLog == null)
			taskLog = new TaskLog(taskItem, false);
		ImmediateExecution execution = new ImmediateExecution(client, taskItem,
				taskLog);
		client.getThreadPool().execute(execution);
		taskItem.waitForStart(600);
		return execution;
	}

	public static void removeJob(String indexName, String jobName)
			throws SearchLibException {
		lock.rl.lock();
		try {
			scheduler.deleteJob(jobKey(jobName, indexName));
		} catch (SchedulerException e) {
			throw new SearchLibException(e);
		} finally {
			lock.rl.unlock();
		}
	}

	public static void removeJobs(String indexName) throws SearchLibException {
		lock.rl.lock();
		try {
			Set<JobKey> jobKeySet = scheduler
					.getJobKeys(jobGroupEquals(indexName));
			for (JobKey jobKey : jobKeySet)
				scheduler.deleteJob(jobKey);
		} catch (SchedulerException e) {
			throw new SearchLibException(e);
		} finally {
			lock.rl.unlock();
		}
	}

	public static String[] getActiveJobs(String indexName)
			throws SchedulerException {
		lock.rl.lock();
		try {
			Set<JobKey> jobKeySet = scheduler
					.getJobKeys(jobGroupEquals(indexName));
			String[] jobs = new String[jobKeySet.size()];
			int i = 0;
			for (JobKey jobKey : jobKeySet)
				jobs[i++] = jobKey.getName();
			return jobs;
		} finally {
			lock.rl.unlock();
		}
	}

	@Override
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		if (StartStopListener.isShutdown())
			throw new JobExecutionException("Aborted (application stopped)");
		JobDetail jobDetail = context.getJobDetail();
		JobKey jobKey = jobDetail.getKey();
		try {
			executeJob(jobKey.getGroup(), jobKey.getName());
		} catch (SearchLibException e) {
			Logging.error(e);
		} catch (NamingException e) {
			Logging.error(e);
		}
	}
}
