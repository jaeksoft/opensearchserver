/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.test.rest;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.tools.ant.util.StringUtils;

public abstract class CommonRestAPI {

	protected class Target {

		private final WebClient client;
		private String path;

		private Target(String address) {
			client = WebClient.create(address);
		}

		protected Target path(String path) {
			this.path = path;
			return this;
		}

		protected Target pathParam(String name, String value) {
			path = StringUtils.replace(path, '{' + name + '}', value);
			return this;
		}

		protected Request request(String type) {
			return new Request(type);
		}

		protected class Request {

			private final String type;

			private Request(String type) {
				this.type = type;
			}

			protected <T> T delete(Class<T> entityType) {
				return client.path(path).accept(type).delete()
						.readEntity(entityType);
			}

			protected <T> T post(Object body, Class<T> entityType) {
				return client.path(path).accept(type).post(body)
						.readEntity(entityType);
			}

			protected <T> T get(Class<T> entityType) {
				return client.path(path).accept(type).get()
						.readEntity(entityType);
			}

			public <T> T put(Object body, Class<T> entityType) {
				return client.path(path).accept(type).put(body)
						.readEntity(entityType);
			}
		}
	}

	protected Target getTarget(String path) {
		Target target = new Target(AllRestAPITests.SERVER_URL);
		return target.path(path);
	}

	protected Target getIndexTarget(String path) {
		return getTarget(path).pathParam("index_name",
				AllRestAPITests.INDEX_NAME);
	}
}
