/**
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2010-2012 Emmanuel Keller / Jaeksoft
 * <p>
 * http://www.open-search-server.com
 * <p>
 * This file is part of OpenSearchServer.
 * <p>
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.
 * If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.replication;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.process.ThreadAbstract;
import com.jaeksoft.searchlib.scheduler.TaskLog;
import com.jaeksoft.searchlib.util.*;
import com.jaeksoft.searchlib.web.PushServlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReplicationThread extends ThreadAbstract<ReplicationThread> implements RecursiveDirectoryBrowser.CallBack {

	final private ReadWriteLock rwl = new ReadWriteLock();

	private volatile Client client;

	private volatile double totalSize;

	private volatile int filesSent;

	private volatile long bytesSent;

	private volatile double checkedSize;

	private volatile InfoCallback infoCallback;

	private volatile List<File> filesNotPushed;

	private volatile List<File> dirsNotPushed;

	private volatile File sourceDirectory;

	private final ReplicationType replicationType;

	private final long initVersion;

	protected ReplicationThread(Client client, ReplicationMaster replicationMaster, ReplicationItem replicationItem,
			InfoCallback infoCallback) throws SearchLibException {
		super(client, replicationMaster, replicationItem, infoCallback);
		this.sourceDirectory = replicationItem.getDirectory(client);
		this.replicationType = replicationItem.getReplicationType();
		this.client = client;
		initVersion = client.getIndex().getVersion();
		totalSize = 0;
		filesSent = 0;
		checkedSize = 0;
		filesNotPushed = null;
		dirsNotPushed = null;
		this.infoCallback = infoCallback;
	}

	public int getProgress() {
		rwl.r.lock();
		try {
			if (checkedSize == 0 || totalSize == 0)
				return 0;
			int p = (int) ((checkedSize / totalSize) * 100);
			return p;
		} finally {
			rwl.r.unlock();
		}
	}

	private void incFilesSent(long bytesSent) {
		rwl.w.lock();
		try {
			filesSent++;
			this.bytesSent += bytesSent;
		} finally {
			rwl.w.unlock();
		}
	}

	public ReplicationItem getReplicationItem() {
		return (ReplicationItem) getThreadItem();
	}

	private void initNotPushedList() {
		filesNotPushed = new ArrayList<>(0);
		dirsNotPushed = new ArrayList<>(0);
		getReplicationItem().getReplicationType().addNotPushedPath(sourceDirectory, filesNotPushed, dirsNotPushed);
	}

	@Override
	public void runner() throws Exception {
		setInfo("Running");
		if (replicationType == ReplicationType.MAIN_DATA_COPY)
			throw new Exception("Not yet implemented");
		else {
			initNotPushedList();
			client.push(this);
		}
	}

	@Override
	public void release() {
		Exception e = getException();
		if (e != null)
			setInfo("Error: " + e.getMessage() != null ? e.getMessage() : e.toString());
		else if (isAborted())
			setInfo("Aborted");
		else
			setInfo("Completed");
	}

	public void push() throws SearchLibException {
		ReplicationItem replicationItem = getReplicationItem();
		try {
			setTotalSize(new LastModifiedAndSize(sourceDirectory, false).getSize());
			addCheckedSize(sourceDirectory.length());
			PushServlet.call_init(getReplicationItem());
			new RecursiveDirectoryBrowser(sourceDirectory, this);
			checkVersion();
			switch (replicationItem.getReplicationType().getFinalMode()) {
			case MERGE:
				PushServlet.call_merge(replicationItem);
				break;
			case SWITCH:
				PushServlet.call_switch(replicationItem);
				break;
			}
		} catch (Exception e) {
			PushServlet.call_abort(replicationItem);
			if (e instanceof SearchLibException)
				throw (SearchLibException) e;
			else
				throw new SearchLibException(e);
		}
	}

	private void setTotalSize(long size) {
		rwl.w.lock();
		try {
			totalSize = size;
		} finally {
			rwl.w.unlock();
		}
	}

	private void addCheckedSize(long length) {
		rwl.w.lock();
		try {
			checkedSize += length;
		} finally {
			rwl.w.unlock();
		}
	}

	final private boolean checkFilePush(final File file) throws IOException {
		if (!checkDirPush(file))
			return false;
		for (File fileNotPushed : filesNotPushed)
			if (file.equals(fileNotPushed))
				return false;
		return true;
	}

	final private boolean checkDirPush(final File dir) throws IOException {
		for (File dirNotPushed : dirsNotPushed) {
			if (dir.equals(dirNotPushed))
				return false;
			if (FileUtils.isSubDirectory(dirNotPushed, dir))
				return false;
		}
		return true;
	}

	public String getStatInfo() {
		rwl.r.lock();
		try {
			return getProgress() + "% completed - " + filesSent + " file(s) sent - " + FileUtils
					.byteCountToDisplaySize(bytesSent) + " sent";
		} finally {
			rwl.r.unlock();
		}
	}

	private void checkVersion() throws SearchLibException {
		if (initVersion != client.getIndex().getVersion())
			throw new SearchLibException("Replication process aborted. The index has changed.");
	}

	@Override
	public void file(File file) throws SearchLibException {
		try {
			checkVersion();
			ReplicationItem replicationItem = getReplicationItem();
			long length = file.length();
			if (file.isFile()) {
				if (checkFilePush(file)) {
					if (!PushServlet.call_file_exist(client, replicationItem, file)) {
						PushServlet.call_file(client, replicationItem, file);
						incFilesSent(length);
					}
				}
			} else {
				if (replicationType.isNotPushedFolder(file))
					dirsNotPushed.add(file);
				if (checkDirPush(file))
					PushServlet.call_directory(client, replicationItem, file);
			}
			addCheckedSize(length);
			if (infoCallback != null) {
				infoCallback.setInfo(getStatInfo());
				if (infoCallback instanceof TaskLog)
					if (((TaskLog) infoCallback).isAbortRequested())
						throw new SearchLibException.AbortException();
			}
		} catch (IllegalStateException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		}
	}
}
