/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.scheduler.task;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.ClientCatalogItem;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.scheduler.TaskAbstract;
import com.jaeksoft.searchlib.scheduler.TaskLog;
import com.jaeksoft.searchlib.scheduler.TaskPropertyDef;
import com.jaeksoft.searchlib.scheduler.TaskPropertyType;
import com.jaeksoft.searchlib.schema.SchemaField;

public abstract class TaskPullAbstract extends TaskAbstract {

	final protected TaskPropertyDef propSourceIndex = new TaskPropertyDef(
			TaskPropertyType.comboBox, "Index source", 100);

	final protected TaskPropertyDef propLogin = new TaskPropertyDef(
			TaskPropertyType.textBox, "Login (Index target)", 20);

	final protected TaskPropertyDef propApiKey = new TaskPropertyDef(
			TaskPropertyType.password, "API Key (Index target)", 50);

	protected void populateSourceIndexValues(Config config, List<String> values)
			throws SearchLibException {
		for (ClientCatalogItem item : ClientCatalog.getClientCatalog(null)) {
			String v = item.getIndexName();
			if (!v.equals(config.getIndexName()))
				values.add(v);
		}
	}

	protected void populateFieldValues(Config config, List<String> values) {
		for (SchemaField field : config.getSchema().getFieldList().getList()) {
			if (field.isIndexed())
				values.add(field.getName());
		}
	}

	protected String[] toValueArray(List<String> values) {
		if (values.size() == 0)
			return null;
		String[] valueArray = new String[values.size()];
		values.toArray(valueArray);
		return valueArray;
	}

	final protected static int indexBuffer(int totalCount,
			List<IndexDocument> buffer, Client target, TaskLog taskLog)
			throws SearchLibException, NoSuchAlgorithmException, IOException,
			URISyntaxException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		if (buffer.size() == 0)
			return totalCount;
		totalCount += target.updateDocuments(buffer);
		buffer.clear();
		taskLog.setInfo(totalCount + " document(s) indexed");
		return totalCount;
	}
}
