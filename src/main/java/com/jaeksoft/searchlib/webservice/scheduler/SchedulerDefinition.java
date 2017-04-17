package com.jaeksoft.searchlib.webservice.scheduler;

import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.scheduler.*;

import java.util.*;

/**
 * Created by aureliengiudici on 22/04/2016.
 */
public class SchedulerDefinition {
	public String name;
	public String type;
	public TaskCronExpression cron;
	public String mailRecipients;
	public boolean active;
	public boolean emailNotification;
	public LinkedHashMap<String, Map<String, String>> tasks;
	private Config config;
	private List<TaskItem> tasksList = new ArrayList<>();

	SchedulerDefinition() {
	}

	public void initialize() {
		TaskItem taskItem;
		TaskAbstract taskAbstract;

		for (Map.Entry<String, Map<String, String>> item : tasks.entrySet()) {
			taskAbstract = config.getJobTaskEnum().findClass(item.getKey());
			taskItem = new TaskItem(this.config, taskAbstract);
			for (Map.Entry<String, String> prop : item.getValue().entrySet())
				taskItem.getProperties()
						.setValue(taskAbstract.findPropertyByConfigName(prop.getKey()), prop.getValue());
			this.tasksList.add(new TaskItem(taskItem));
		}
	}

	public void setConfig(Config conf) {
		this.config = conf;
	}

	public Config getConfig() {
		return this.config;
	}

	public List<TaskItem> getTasksList() {
		return this.tasksList;
	}
}
