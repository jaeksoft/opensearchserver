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

import java.util.Collections;
import java.util.List;

import javax.naming.NamingException;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zul.Messagebox;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.scheduler.JobItem;
import com.jaeksoft.searchlib.scheduler.JobList;
import com.jaeksoft.searchlib.scheduler.TaskEnumItem;
import com.jaeksoft.searchlib.scheduler.TaskItem;
import com.jaeksoft.searchlib.web.controller.AlertController;

@AfterCompose(superclass = true)
public class SchedulerEditController extends SchedulerController {

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
		super.reset();
		currentJob = getJobItemEdit();
		currentTask = null;
		selectedTask = null;
		selectedJobTask = null;
		Client client = getClient();
		if (client != null)
			setSelectedTask(client.getJobTaskEnum().getFirst());
	}

	@Command
	@Override
	public void reload() throws SearchLibException {
		currentJob = getJobItemEdit();
		super.reload();
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

		List<TaskEnumItem> list = client.getJobTaskEnum().getList();
		Collections.sort(list);
		return list;
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
		reload();
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
	@Command
	public void onTaskAdd() throws SearchLibException {
		currentJob.taskAdd(currentTask);
		onTaskCancel();
	}

	@Command
	public void onTaskSave() throws SearchLibException {
		selectedJobTask.setProperties(currentTask.getProperties().getArray());
		onTaskCancel();
	}

	@Command
	public void onTaskCancel() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return;
		selectedJobTask = null;
		this.currentTask = new TaskItem(client, selectedTask.getTask());
		reload();
	}

	/**
	 * Move the task up
	 * 
	 * @param component
	 * @throws SearchLibException
	 */
	@Command
	public void onTaskUp(@BindingParam("taskitem") TaskItem taskItem)
			throws SearchLibException {
		currentJob.taskUp(taskItem);
		reload();
	}

	@Command
	@NotifyChange("currentTask")
	public void onRefreshProperties() {
	}

	/**
	 * Move the task down
	 * 
	 * @param component
	 * @throws SearchLibException
	 */
	@Command
	public void onTaskDown(@BindingParam("taskitem") TaskItem taskItem)
			throws SearchLibException {
		currentJob.taskDown(taskItem);
		reload();
	}

	/**
	 * Remove the task
	 * 
	 * @param component
	 * @throws SearchLibException
	 */
	@Command
	public void onTaskRemove(@BindingParam("taskitem") TaskItem taskItem)
			throws SearchLibException {
		currentJob.taskRemove(taskItem);
		reload();
	}

	@Override
	@GlobalCommand
	@NotifyChange("*")
	public void eventClientChange() throws SearchLibException {
		onCancel();
	}

	@Command
	@NotifyChange("*")
	public void onCancel() throws SearchLibException {
		reset();
		setJobItemEdit(null);
	}

	@Command
	public void onDelete() throws SearchLibException, InterruptedException {
		JobItem selectedJob = getJobItemSelected();
		if (selectedJob == null)
			return;
		new DeleteAlert(selectedJob);
	}

	@Command
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

	public boolean isNotSelectionJobTask() {
		return getSelectedJobTask() == null;
	}

	public boolean isSelectionJobTask() {
		return !isNotSelectionJobTask();
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
		reload();
	}

}
