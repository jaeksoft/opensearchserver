/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2015 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.webservice.replication;

import javax.ws.rs.core.Response.Status;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.replication.ReplicationItem;
import com.jaeksoft.searchlib.replication.ReplicationList;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.webservice.CommonListResult;
import com.jaeksoft.searchlib.webservice.CommonResult;
import com.jaeksoft.searchlib.webservice.CommonServices;

public class ReplicationImpl extends CommonServices implements RestReplication {

	@Override
	public CommonResult replicationGet(String login, String key,
			String index_name, String replication_name) {
		try {
			Client client = getLoggedClientAnyRole(index_name, login, key,
					Role.GROUP_INDEX);
			ClientFactory.INSTANCE.properties.checkApi();
			if (StringUtils.isEmpty(replication_name))
				return new CommonListResult<ReplicationResult>(
						ReplicationResult.toArray(client.getReplicationList()
								.getArray()));
			ReplicationItem replicationItem = client.getReplicationList().get(
					replication_name);
			if (replicationItem == null)
				throw new CommonServiceException(Status.NOT_FOUND,
						"Replication item not found");
			return new ReplicationResult(true, replicationItem);
		} catch (Exception e) {
			if (e instanceof CommonServiceException)
				throw (CommonServiceException) e;
			else
				throw new CommonServiceException(e);
		}
	}

	@Override
	public CommonResult replicationSet(String login, String key,
			String index_name, ReplicationResult replication) {
		try {
			Client client = getLoggedClient(index_name, login, key,
					Role.INDEX_UPDATE);
			ClientFactory.INSTANCE.properties.checkApi();
			ReplicationList replicationList = client.getReplicationList();
			ReplicationItem newItem = replication.getReplicationItem(client
					.getReplicationMaster());
			ReplicationItem oldItem = replicationList.get(newItem.getName());
			replicationList.save(oldItem, newItem);
			client.saveReplicationList();
			String message = oldItem == null ? "Item created: "
					: "Item updated: ";
			return new CommonResult(true, message + newItem.getName());
		} catch (Exception e) {
			if (e instanceof CommonServiceException)
				throw (CommonServiceException) e;
			else
				throw new CommonServiceException(e);
		}
	}

	@Override
	public CommonResult replicationRun(String login, String key,
			String index_name, String replication_name) {
		try {
			Client client = getLoggedClient(index_name, login, key,
					Role.INDEX_UPDATE);
			ClientFactory.INSTANCE.properties.checkApi();
			ReplicationList replicationList = client.getReplicationList();
			ReplicationItem replicationItem = replicationList
					.get(replication_name);
			if (replicationItem == null)
				throw new CommonServiceException(Status.NOT_FOUND,
						"Replication item not found");
			client.getReplicationMaster().execute(client, replicationItem,
					false, null, null);
			return new CommonResult(true, "Item started: " + replication_name);
		} catch (Exception e) {
			if (e instanceof CommonServiceException)
				throw (CommonServiceException) e;
			else
				throw new CommonServiceException(e);
		}
	}

	@Override
	public CommonResult replicationDelete(String login, String key,
			String index_name, String replication_name) {
		try {
			Client client = getLoggedClient(index_name, login, key,
					Role.INDEX_UPDATE);
			ClientFactory.INSTANCE.properties.checkApi();
			ReplicationList replicationList = client.getReplicationList();
			ReplicationItem replicationItem = replicationList
					.get(replication_name);
			if (replicationItem == null)
				throw new CommonServiceException(Status.NOT_FOUND,
						"Replication item not found");
			replicationList.save(replicationItem, null);
			client.saveReplicationList();
			return new CommonResult(true, "Item " + replication_name
					+ " deleted.");
		} catch (Exception e) {
			if (e instanceof CommonServiceException)
				throw (CommonServiceException) e;
			else
				throw new CommonServiceException(e);
		}
	}

}
