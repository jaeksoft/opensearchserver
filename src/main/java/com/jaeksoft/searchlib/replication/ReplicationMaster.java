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

package com.jaeksoft.searchlib.replication;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.process.ThreadItem;
import com.jaeksoft.searchlib.process.ThreadMasterAbstract;
import com.jaeksoft.searchlib.util.InfoCallback;
import com.jaeksoft.searchlib.util.Variables;

public class ReplicationMaster extends
		ThreadMasterAbstract<ReplicationMaster, ReplicationThread> {

	public ReplicationMaster(Config config) {
		super(config);
	}

	@Override
	protected ReplicationThread getNewThread(Client client,
			ThreadItem<?, ReplicationThread> replicationItem,
			Variables variables, InfoCallback infoCallback)
			throws SearchLibException {
		return new ReplicationThread(client, this,
				(ReplicationItem) replicationItem, infoCallback);
	}

	@Override
	protected ReplicationThread[] getNewArray(int size) {
		return new ReplicationThread[size];
	}

}
