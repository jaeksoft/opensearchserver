/**
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2010-2016 Emmanuel Keller / Jaeksoft
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
import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.index.UpdateInterfaces;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Collection;

public class SilentBackupReplication implements UpdateInterfaces.After, UpdateInterfaces.Delete {

	private final Client client;
	private final ReplicationMaster replicationMaster;
	private final ReplicationItem replicationItem;

	public SilentBackupReplication(final Client client, final String url) throws SearchLibException {
		try {
			this.client = client;
			this.replicationMaster = client.getReplicationMaster();
			this.replicationItem = new ReplicationItem(replicationMaster, client.getIndexName(), url);
		} catch (MalformedURLException | URISyntaxException e) {
			throw new SearchLibException(e);
		}
	}

	public void doReplication() throws SearchLibException {
		try {
			synchronized (this) {
				Exception exception =
						replicationMaster.execute(client, replicationItem, true, null, null).getException();
				if (exception != null)
					Logging.warn("Silent replication error: " + exception.getMessage(), exception);
			}
		} catch (InterruptedException e) {
			throw new SearchLibException(e);
		}
	}

	@Override
	public void update(IndexDocument document) throws SearchLibException {
		doReplication();
	}

	@Override
	public void update(Collection<IndexDocument> documents) throws SearchLibException {
		doReplication();
	}

	@Override
	public void delete(String field, String value) throws SearchLibException {
		doReplication();
	}

	@Override
	public void delete(String field, Collection<String> values) throws SearchLibException {
		doReplication();
	}
}
