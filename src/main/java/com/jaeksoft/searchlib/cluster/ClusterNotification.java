/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.cluster;

import java.io.File;
import java.net.URISyntaxException;

import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.web.ActionServlet;

public class ClusterNotification {

	public enum Type {
		RELOAD_USER, CLOSE_CLIENT, RELOAD_DATA;
	}

	public final Type type;
	public final File indexDir;

	public ClusterNotification(Type type) {
		this.type = type;
		this.indexDir = null;
	}

	public ClusterNotification(Type type, File indexDir) {
		this.type = type;
		this.indexDir = indexDir;
	}

	public void send(ClusterInstance clusterInstance)
			throws SearchLibException, URISyntaxException {
		if (clusterInstance == null)
			return;
		new NotificationThread(clusterInstance);
	}

	public class NotificationThread implements Runnable {

		private final ClusterInstance clusterInstance;

		private NotificationThread(ClusterInstance clusterInstance) {
			this.clusterInstance = clusterInstance;
			new Thread(ClientCatalog.getThreadGroup(), this).start();
		}

		@Override
		public void run() {
			try {
				switch (type) {
				case CLOSE_CLIENT:
					ActionServlet.close(clusterInstance.getUri(),
							indexDir.getName(), clusterInstance.getLogin(),
							clusterInstance.getApiKey());
					break;
				case RELOAD_DATA:
					ActionServlet.reload(clusterInstance.getUri(),
							indexDir.getName(), clusterInstance.getLogin(),
							clusterInstance.getApiKey());
					break;
				default:
					Logging.warn("Nothing to do");
				}
			} catch (Throwable t) {
				Logging.error(t);
			}
		}
	}
}
