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

package com.jaeksoft.searchlib.join;

import java.io.IOException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.authentication.AuthManager;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.request.SearchFieldRequest;

public class AuthJoinItem extends JoinItem {

	public AuthJoinItem(Config config) throws SearchLibException, IOException {
		AuthManager authManager = config.getAuthManager();
		setIndexName(authManager.getIndex());
		Client foreignClient = getFogeignClient();
		setLocalField(config.getSchema().getUniqueField());
		if (foreignClient == null)
			throw new SearchLibException("The index " + getIndexName()
					+ " cannot be found");
		setForeignField(foreignClient.getSchema().getUniqueField());
	}

	@Override
	protected AbstractSearchRequest getForeignSearchRequest(
			final Client foreignClient) throws SearchLibException {
		AbstractSearchRequest foreignRequest = new SearchFieldRequest(
				foreignClient);
		foreignRequest.getFilterList().addAuthFilter();
		return foreignRequest;
	}

}
