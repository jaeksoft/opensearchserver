/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012-2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.common.database;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.FieldMap;
import com.jaeksoft.searchlib.join.JoinItem;
import com.jaeksoft.searchlib.join.JoinItem.OuterCollector;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.result.AbstractResultSearch;
import com.jaeksoft.searchlib.scheduler.TaskAbstract;
import com.jaeksoft.searchlib.scheduler.TaskLog;
import com.jaeksoft.searchlib.schema.Indexed;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.schema.SchemaFieldList;
import com.jaeksoft.searchlib.util.map.SourceField;
import com.jaeksoft.searchlib.util.map.TargetField;

public abstract class AbstractManager {

	protected Client targetClient;
	protected Client dbClient;

	private Set<TaskAbstract> taskToCheck;
	private TaskLog currentTaskLog;

	protected AbstractManager() {
		currentTaskLog = null;
		targetClient = null;
		dbClient = null;
		taskToCheck = new HashSet<TaskAbstract>(0);
	}

	final protected void init(Client targetClient, Client dbClient) {
		this.targetClient = targetClient;
		this.dbClient = dbClient;
	}

	final public void free() {
		if (dbClient != null) {
			dbClient.close();
			dbClient = null;
		}
	}

	final public Client getDbClient() {
		return dbClient;
	}

	public void setCurrentTaskLog(TaskLog taskLog) throws SearchLibException {
		synchronized (this) {
			if (currentTaskLog != null)
				throw new SearchLibException("A task is already running: "
						+ currentTaskLog.getTask().getName());
			if (taskLog != null)
				synchronized (taskToCheck) {
					taskToCheck.add(taskLog.getTask());
				}
			currentTaskLog = taskLog;
		}
	}

	protected final String findIndexedFieldOfTargetIndex(FieldMap fieldMap,
			String sourceField) throws SearchLibException {

		List<TargetField> mappedPath = fieldMap.getLinks(new SourceField(
				sourceField));

		if (mappedPath == null || mappedPath.isEmpty())
			return null;

		SchemaFieldList targetSchemaFieldList = targetClient.getSchema()
				.getFieldList();
		SchemaField targetUniqueField = targetSchemaFieldList.getUniqueField();
		for (TargetField targetField : mappedPath) {
			SchemaField field = targetSchemaFieldList
					.get(targetField.getName());
			if (field.getIndexed() != Indexed.YES)
				continue;
			if (targetUniqueField != null)
				if (field.getName().equals(targetUniqueField.getName()))
					return field.getName();
			if (field.getIndexAnalyzer() == null)
				return field.getName();
		}
		return null;
	}

	public void resetCurrentTaskLog() {
		synchronized (this) {
			currentTaskLog = null;
		}
	}

	public TaskLog getCurrentTaskLog() {
		synchronized (this) {
			return currentTaskLog;
		}
	}

	public boolean isCurrentTaskLogExists() {
		return getCurrentTaskLog() != null;
	}

	public boolean isNoCurrentTaskLogExists() {
		return !isCurrentTaskLogExists();
	}

	public boolean waitForTask(TaskAbstract taskAbstract, long secTimeOut)
			throws InterruptedException {
		long timeOut = System.currentTimeMillis() + 1000 * secTimeOut;
		while (timeOut > System.currentTimeMillis()) {
			synchronized (taskToCheck) {
				if (taskToCheck.remove(taskAbstract))
					return true;
			}
			Thread.sleep(1000);
		}
		Logging.warn("Wait for task " + taskAbstract.getName());
		return false;
	}

	public static Date getPastDate(long fetchInterval, String intervalUnit) {
		return new TimeInterval(intervalUnit, fetchInterval).getPastDate(System
				.currentTimeMillis());
	}

	private class SynchronizedOuterCollector implements OuterCollector {

		private long total;

		private final String field;

		private final int buffer;

		private final List<String> deletionList;

		private final TaskLog taskLog;

		private SynchronizedOuterCollector(String field, int buffer,
				TaskLog taskLog) {
			this.total = 0;
			this.field = field;
			this.buffer = buffer;
			this.taskLog = taskLog;
			this.deletionList = new ArrayList<String>(buffer);
		}

		@Override
		final public void collect(final int id, final String value) {
			deletionList.add(value);
			if (deletionList.size() >= buffer)
				delete();
		}

		final public void delete() {
			if (deletionList.isEmpty())
				return;
			try {
				total += targetClient.deleteDocuments(field, deletionList);
			} catch (SearchLibException e) {
				taskLog.setError(e);
			}
			taskLog.setInfo(total + " document(s) deleted");
			deletionList.clear();
		}

	}

	protected long synchronizeIndex(AbstractSearchRequest searchRequest,
			String targetField, String dbField, int bufferSize, TaskLog taskLog)
			throws SearchLibException {
		setCurrentTaskLog(taskLog);
		try {
			if (targetField == null)
				throw new SearchLibException("The primary field is not mapped");
			SynchronizedOuterCollector outerCollector = new SynchronizedOuterCollector(
					targetField, bufferSize, taskLog);
			searchRequest = (AbstractSearchRequest) searchRequest.duplicate();
			JoinItem joinItem = new JoinItem();
			joinItem.setIndexName(targetClient.getIndexName());
			joinItem.setForeignField(dbField);
			joinItem.setLocalField(targetField);
			joinItem.setOuterCollector(outerCollector);
			searchRequest.getJoinList().add(joinItem);
			AbstractResultSearch result = (AbstractResultSearch) dbClient
					.request(searchRequest);
			outerCollector.delete();
			if (taskLog != null) {
				taskLog.setInfo("URLs: (Found / Deleted: "
						+ result.getNumFound() + " / " + outerCollector.total);
			}
			return outerCollector.total;
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		} finally {
			resetCurrentTaskLog();
		}
	}

	final public void reload(boolean optimize, TaskLog taskLog)
			throws SearchLibException {
		setCurrentTaskLog(taskLog);
		try {
			if (optimize) {
				dbClient.reload();
				dbClient.optimize();
			}
			targetClient.reload();
		} finally {
			resetCurrentTaskLog();
		}
	}

	final public long getSize() throws SearchLibException {
		try {
			return dbClient.getStatistics().getNumDocs();
		} catch (IOException e) {
			throw new SearchLibException(e);
		}
	}

	final public void deleteAll(TaskLog taskLog) throws SearchLibException {
		setCurrentTaskLog(taskLog);
		try {
			dbClient.deleteAll();
		} finally {
			resetCurrentTaskLog();
		}
	}

}
