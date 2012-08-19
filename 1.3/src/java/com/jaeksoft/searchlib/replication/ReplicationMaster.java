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

package com.jaeksoft.searchlib.replication;

import java.util.TreeMap;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.process.ThreadAbstract;
import com.jaeksoft.searchlib.process.ThreadMasterAbstract;
import com.jaeksoft.searchlib.scheduler.TaskLog;

public class ReplicationMaster extends ThreadMasterAbstract {

	private TreeMap<ReplicationItem, ReplicationThread> threadMap;

	public ReplicationMaster(Config config) {
		super(config);
		threadMap = new TreeMap<ReplicationItem, ReplicationThread>();
	}

	public boolean isReplicationThread(ReplicationItem replicationItem) {
		synchronized (threadMap) {
			return threadMap.containsKey(replicationItem);
		}
	}

	public ReplicationThread execute(Client client,
			ReplicationItem replicationItem, boolean bWaitForCompletion,
			TaskLog taskLog) throws InterruptedException, SearchLibException {
		ReplicationThread replicationThread = null;
		synchronized (threadMap) {
			if (threadMap.containsKey(replicationItem)) {
				throw new SearchLibException("The job "
						+ replicationItem.getName() + " is already running");
			}
			replicationThread = new ReplicationThread(client, this,
					replicationItem, taskLog);
			threadMap.put(replicationItem, replicationThread);
		}
		replicationItem.setReplicationThread(replicationThread);
		add(replicationThread);
		replicationThread.waitForStart(0);
		if (bWaitForCompletion)
			replicationThread.waitForEnd(0);
		return replicationThread;
	}

	@Override
	public void remove(ThreadAbstract thread) {
		super.remove(thread);
		synchronized (threadMap) {
			threadMap.remove(((ReplicationThread) thread).getReplicationItem());
		}
	}

}
