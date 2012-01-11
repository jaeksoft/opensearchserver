/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2011 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.web.controller.scheduler;

import java.util.List;

import javax.naming.NamingException;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Tab;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.scheduler.JobItem;
import com.jaeksoft.searchlib.scheduler.JobList;
import com.jaeksoft.searchlib.scheduler.TaskEnumItem;
import com.jaeksoft.searchlib.scheduler.TaskItem;
import com.jaeksoft.searchlib.web.controller.AlertController;
import com.jaeksoft.searchlib.web.controller.CommonController;
import com.jaeksoft.searchlib.web.controller.PushEvent;

public class SchedulerEditController extends CommonController {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5736529335058096440L;

	private transient JobItem selectedJob;

	private transient JobItem currentJob;

	private transient TaskEnumItem selectedTask;

	private transient TaskItem currentTask;

	private class DeleteAlert extends AlertController {

		private transient JobItem deleteJob;

		protected DeleteAlert(JobItem deleteJob) throws InterruptedException {
			super("Please, confirm that you want to delete the job: "
					+ deleteJob.getName(), Messagebox.YES | Messagebox.NO,
					Messagebox.QUESTION);
			this.deleteJob = deleteJob;
		}

		@Override
		protected void onYes() throws SearchLibException {
			Client client = getClient();
			client.getJobList().remove(deleteJob.getName());
			client.saveJobs();
			onCancel();
		}
	}

	public SchedulerEditController() throws SearchLibException, NamingException {
		super();
	}

	@Override
	protected void reset() throws SearchLibException {
		selectedJob = null;
		currentJob = new JobItem("New job");
		Client client = getClient();
		if (client != null)
			setSelectedTask(client.getJobTaskEnum().getFirst());
	}

	@Override
	public void eventJobEdit(JobItem job) throws SearchLibException {
		if (job == null)
			return;
		this.selectedJob = job;
		currentJob.copy(job);
		reloadPage();
	}

	/**
	 * 
	 * @return the current JobItem
	 */
	public JobItem getCurrentJob() {
		return currentJob;
	}

	/**
	 * 
	 * @return the current TaskItem
	 */
	public TaskItem getCurrentTask() {
		return currentTask;
	}

	public String getCurrentEditMode() throws SearchLibException {
		return selectedJob == null ? "Create a new job"
				: "Edit the selected job";
	}

	public boolean selected() {
		return selectedJob != null;
	}

	public boolean notSelected() {
		return !selected();
	}

	public List<TaskEnumItem> getTaskEnum() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		return client.getJobTaskEnum().getList();
	}

	/**
	 * @param selectedTask
	 *            the selectedTask to set
	 * @throws SearchLibException
	 */
	public void setSelectedTask(TaskEnumItem selectedTask)
			throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return;
		this.selectedTask = selectedTask;
		this.currentTask = new TaskItem(client, selectedTask.getTask());
		reloadPage();
	}

	/**
	 * @return the selectedTask
	 */
	public TaskEnumItem getSelectedTask() {
		return selectedTask;
	}

	/**
	 * Add a new task
	 * 
	 * @throws SearchLibException
	 */
	public void onTaskAdd() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return;
		currentJob.taskAdd(currentTask);
		this.currentTask = new TaskItem(client, selectedTask.getTask());
		reloadPage();
	}

	/**
	 * Return the taskItem row
	 * 
	 * @param component
	 * @return
	 */
	private TaskItem getTaskItem(Component component) {
		return (TaskItem) component.getParent().getAttribute("taskitem");
	}

	/**
	 * Move the task up
	 * 
	 * @param component
	 */
	public void onTaskUp(Component component) {
		TaskItem taskItem = getTaskItem(component);
		currentJob.taskUp(taskItem);
		reloadPage();
	}

	/**
	 * Move the task down
	 * 
	 * @param component
	 */
	public void onTaskDown(Component component) {
		TaskItem taskItem = getTaskItem(component);
		currentJob.taskDown(taskItem);
		reloadPage();
	}

	/**
	 * Remove the task
	 * 
	 * @param component
	 */
	public void onTaskRemove(Component component) {
		TaskItem taskItem = getTaskItem(component);
		currentJob.taskRemove(taskItem);
		reloadPage();
	}

	public void onCancel() throws SearchLibException {
		reset();
		reloadPage();
		Tab tab = (Tab) getFellow("tabSchedulerList", true);
		tab.setSelected(true);
		PushEvent.JOB_EDIT.publish();
	}

	public void onDelete() throws SearchLibException, InterruptedException {
		if (selectedJob == null)
			return;
		new DeleteAlert(selectedJob);
		onCancel();
	}

	public void onSave() throws InterruptedException, SearchLibException {
		Client client = getClient();
		if (client == null)
			return;
		JobList jobList = client.getJobList();
		if (selectedJob == null) {
			if (jobList.get(currentJob.getName()) != null) {
				new AlertController("The name is already used");
				return;
			}
			jobList.add(currentJob);
		} else
			selectedJob.copy(currentJob);
		client.saveJobs();
		onCancel();
	}

}
