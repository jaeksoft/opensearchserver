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

package com.jaeksoft.searchlib.web.controller.schema;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.ClientCatalogItem;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.authentication.AuthManager;
import com.jaeksoft.searchlib.web.controller.CommonController;

@AfterCompose(superclass = true)
public class AuthComposer extends CommonController {

	public AuthComposer() throws SearchLibException {
		super();
	}

	@Override
	protected void reset() throws SearchLibException {
	}

	@Override
	@Command
	public void reload() throws SearchLibException {
		super.reload();
	}

	public AuthManager getManager() throws SearchLibException, IOException {
		Client client = getClient();
		if (client == null)
			return null;
		return client.getAuthManager();
	}

	public List<String> getIndexList() throws SearchLibException {
		List<String> indexes = new ArrayList<String>();
		Set<ClientCatalogItem> clientCatalog = ClientCatalog
				.getClientCatalog(getLoggedUser());
		if (clientCatalog != null)
			for (ClientCatalogItem item : clientCatalog)
				indexes.add(item.getIndexName());
		return indexes;
	}

	@Override
	public List<String> getIndexedFieldList() throws SearchLibException,
			IOException {
		AuthManager authManager = getManager();
		if (authManager == null)
			return null;
		String index = authManager.getIndex();
		List<String> fields = new ArrayList<String>();
		Client client = StringUtils.isEmpty(index) ? getClient()
				: ClientCatalog.getClient(index);
		if (client == null)
			return fields;
		client.getSchema().getFieldList().getIndexedFields(fields);
		return fields;
	}

}
