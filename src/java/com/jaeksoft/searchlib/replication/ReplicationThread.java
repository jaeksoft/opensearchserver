/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.replication;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.process.ThreadAbstract;

public class ReplicationThread extends ThreadAbstract {

	private Client client;

	private ReplicationItem replicationItem;

	private double totalSize;

	private double sendSize;

	protected ReplicationThread(Client client,
			ReplicationMaster replicationMaster, ReplicationItem replicationItem) {
		super(client, replicationMaster);
		this.replicationItem = replicationItem;
		this.client = client;
		totalSize = 0;
		sendSize = 0;
	}

	public int progress() {
		if (sendSize == 0 || totalSize == 0)
			return 0;
		return (int) ((sendSize / totalSize) * 100);
	}

	@Override
	public void runner() throws Exception {
		setInfo("Running");
	}

	@Override
	public void release() {
		Exception e = getException();
		if (e != null)
			setInfo("Error: " + e.getMessage() != null ? e.getMessage() : e
					.toString());
		else if (isAborted())
			setInfo("Aborted");
		else
			setInfo("Completed");
	}

	public ReplicationItem getReplicationItem() {
		return replicationItem;
	}

}
