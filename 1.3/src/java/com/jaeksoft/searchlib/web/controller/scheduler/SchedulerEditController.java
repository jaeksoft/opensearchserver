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

package com.jaeksoft.searchlib.web.controller.scheduler;

import java.util.List;

import javax.naming.NamingException;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Messagebox;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.scheduler.JobItem;
import com.jaeksoft.searchlib.scheduler.JobList;
import com.jaeksoft.searchlib.scheduler.TaskEnumItem;
import com.jaeksoft.searchlib.scheduler.TaskItem;
import com.jaeksoft.searchlib.web.controller.AlertController;

public class SchedulerEditController extends SchedulerController {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5736529335058096440L;

	private transient JobItem currentJob;

	private transient TaskEnumItem selectedTask;

	private transient TaskItem currentTask;

	private transient TaskItem selectedJobTask;

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
		currentJob = null;
		selectedJobTask = null;
		Client client = getClient();
		if (client != null)
			setSelectedTask(client.getJobTaskEnum().getFirst());
	}

	@Override
	public void reloadPage() throws SearchLibException {
		JobItem jobItem = getJobItemEdit();
		currentJob = jobItem;
		super.reloadPage();
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
		return isNoJobItemSelected() ? "Create a new job"
				: "Edit the selected job";
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
		currentJob.taskAdd(currentTask);
		onTaskCancel();
	}

	public void onTaskSave() throws SearchLibException {
		selectedJobTask.setProperties(currentTask.getProperties());
		onTaskCancel();
	}

	public void onTaskCancel() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return;
		selectedJobTask = null;
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
	 * @throws SearchLibException
	 */
	public void onTaskUp(Component component) throws SearchLibException {
		TaskItem taskItem = getTaskItem(component);
		currentJob.taskUp(taskItem);
		reloadPage();
	}

	/**
	 * Move the task down
	 * 
	 * @param component
	 * @throws SearchLibException
	 */
	public void onTaskDown(Component component) throws SearchLibException {
		TaskItem taskItem = getTaskItem(component);
		currentJob.taskDown(taskItem);
		reloadPage();
	}

	/**
	 * Remove the task
	 * 
	 * @param component
	 * @throws SearchLibException
	 */
	public void onTaskRemove(Component component) throws SearchLibException {
		TaskItem taskItem = getTaskItem(component);
		currentJob.taskRemove(taskItem);
		reloadPage();
	}

	public void onCancel() throws SearchLibException {
		reset();
		setJobItemEdit(null);
		reloadSchedulerPages();
	}

	public void onDelete() throws SearchLibException, InterruptedException {
		JobItem selectedJob = getJobItemSelected();
		if (selectedJob == null)
			return;
		new DeleteAlert(selectedJob);
	}

	public void onSave() throws InterruptedException, SearchLibException {
		Client client = getClient();
		if (client == null)
			return;
		JobList jobList = client.getJobList();
		JobItem selectedJob = getJobItemSelected();
		if (selectedJob == null) {
			if (jobList.get(currentJob.getName()) != null) {
				new AlertController("The name is already used");
				return;
			}
			jobList.add(currentJob);
		} else
			selectedJob.copyFrom(currentJob);
		client.saveJobs();
		onCancel();
	}

	public boolean isNotSelectedJobTask() {
		return getSelectedJobTask() == null;
	}

	public boolean isSelectedJobTask() {
		return !isNotSelectedJobTask();
	}

	/**
	 * @return the selectedJobTask
	 */
	public TaskItem getSelectedJobTask() {
		return selectedJobTask;
	}

	/**
	 * @param selectedJobTask
	 *            the selectedJobTask to set
	 * @throws SearchLibException
	 */
	public void setSelectedJobTask(TaskItem selectedJobTask)
			throws SearchLibException {
		this.selectedJobTask = selectedJobTask;
		if (selectedJobTask != null)
			this.currentTask = new TaskItem(selectedJobTask);
		reloadPage();
	}

}
