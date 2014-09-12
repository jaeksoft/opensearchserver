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

package com.jaeksoft.searchlib.scheduler.task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.web.database.CredentialItem;
import com.jaeksoft.searchlib.crawler.web.database.CredentialItem.CredentialType;
import com.jaeksoft.searchlib.crawler.web.spider.DownloadItem;
import com.jaeksoft.searchlib.crawler.web.spider.HttpDownloader;
import com.jaeksoft.searchlib.request.DocumentsRequest;
import com.jaeksoft.searchlib.scheduler.TaskAbstract;
import com.jaeksoft.searchlib.scheduler.TaskLog;
import com.jaeksoft.searchlib.scheduler.TaskProperties;
import com.jaeksoft.searchlib.scheduler.TaskPropertyDef;
import com.jaeksoft.searchlib.scheduler.TaskPropertyType;
import com.jaeksoft.searchlib.util.IOUtils;
import com.jaeksoft.searchlib.util.Variables;

public class TaskDeleteSync extends TaskAbstract {

	final private TaskPropertyDef propUri = new TaskPropertyDef(
			TaskPropertyType.textBox, "URI", "Uri", null, 100);

	final private TaskPropertyDef propLogin = new TaskPropertyDef(
			TaskPropertyType.textBox, "Login", "Login", null, 50);

	final private TaskPropertyDef propPassword = new TaskPropertyDef(
			TaskPropertyType.password, "Password", "Password", null, 20);

	final private TaskPropertyDef propUserAgent = new TaskPropertyDef(
			TaskPropertyType.textBox, "User agent", "UserAgent", null, 20);

	final private TaskPropertyDef propIndexedField = new TaskPropertyDef(
			TaskPropertyType.textBox, "Indexed field", "IndexedField", null, 30);

	final private TaskPropertyDef[] taskPropertyDefs = { propUri, propLogin,
			propPassword, propUserAgent, propIndexedField };

	@Override
	public String getName() {
		return "Delete sync";
	}

	@Override
	public TaskPropertyDef[] getPropertyList() {
		return taskPropertyDefs;
	}

	@Override
	public String[] getPropertyValues(Config config,
			TaskPropertyDef propertyDef, TaskProperties taskProperties)
			throws SearchLibException {
		List<String> values = new ArrayList<String>();
		if (propertyDef == propIndexedField) {
			config.getSchema().getFieldList().getIndexedFields(values);
		}
		return CollectionUtils.isEmpty(values) ? null : values
				.toArray(new String[values.size()]);
	}

	@Override
	public String getDefaultValue(Config config, TaskPropertyDef propertyDef) {
		if (propertyDef == propUserAgent)
			try {
				return config.getWebPropertyManager().getUserAgent().getValue();
			} catch (SearchLibException e) {
				Logging.error(e);
			}
		return null;
	}

	@Override
	public void execute(Client client, TaskProperties properties,
			Variables variables, TaskLog taskLog) throws SearchLibException {
		String uriString = properties.getValue(propUri);
		String login = properties.getValue(propLogin);
		String password = properties.getValue(propPassword);
		String userAgent = properties.getValue(propUserAgent);
		String field = properties.getValue(propIndexedField);

		HttpDownloader httpDownloader = client.getWebCrawlMaster()
				.getNewHttpDownloader(true, userAgent);
		InputStream is = null;
		InputStreamReader isr = null;
		BufferedReader br = null;

		try {
			URI uri = new URI(uriString);
			CredentialItem credentialItem = null;
			if (login != null && password != null)
				credentialItem = new CredentialItem(
						CredentialType.BASIC_DIGEST, null, login, password,
						null, null);
			DownloadItem downloadItem = httpDownloader.get(uri, credentialItem);
			downloadItem.checkNoErrorList(200);

			if (StringUtils.isEmpty(field))
				field = client.getSchema().getDefaultField();
			if (field == null)
				throw new SearchLibException("No field given");
			Collection<String> values = new ArrayList<String>();
			is = downloadItem.getContentInputStream();
			isr = new InputStreamReader(is);
			br = new BufferedReader(isr);
			taskLog.setInfo("Extracting values");
			String line;
			while ((line = br.readLine()) != null)
				values.add(line);
			DocumentsRequest documentsRequest = new DocumentsRequest(client,
					field, values, true);
			taskLog.setInfo("Deletion request:" + values.size()
					+ " value(s) given");
			long i = client.deleteDocuments(documentsRequest);
			taskLog.setInfo(i + " document(s) deleted");
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		} finally {
			IOUtils.close(br, isr, is);
			httpDownloader.release();
		}
	}
}
