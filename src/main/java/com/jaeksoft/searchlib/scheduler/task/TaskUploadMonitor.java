/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2014 Emmanuel Keller / Jaeksoft
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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.Monitor;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.web.database.CredentialItem;
import com.jaeksoft.searchlib.crawler.web.database.CredentialItem.CredentialType;
import com.jaeksoft.searchlib.crawler.web.spider.DownloadItem;
import com.jaeksoft.searchlib.crawler.web.spider.HttpDownloader;
import com.jaeksoft.searchlib.scheduler.TaskAbstract;
import com.jaeksoft.searchlib.scheduler.TaskLog;
import com.jaeksoft.searchlib.scheduler.TaskProperties;
import com.jaeksoft.searchlib.scheduler.TaskPropertyDef;
import com.jaeksoft.searchlib.scheduler.TaskPropertyType;
import com.jaeksoft.searchlib.util.Variables;

public class TaskUploadMonitor extends TaskAbstract {

	final private TaskPropertyDef propUrl = new TaskPropertyDef(TaskPropertyType.textBox, "URL", "Url", null, 100);

	final private TaskPropertyDef propLogin = new TaskPropertyDef(TaskPropertyType.textBox, "Login", "Login", null, 50);

	final private TaskPropertyDef propPassword = new TaskPropertyDef(TaskPropertyType.password, "Password", "Password",
			null, 20);

	final private TaskPropertyDef propInstanceId = new TaskPropertyDef(TaskPropertyType.textBox, "Instance ID",
			"Instance ID", null, 80);

	final private TaskPropertyDef[] taskPropertyDefs = { propUrl, propLogin, propPassword, propInstanceId };

	@Override
	public String getName() {
		return "Monitoring upload";
	}

	@Override
	public TaskPropertyDef[] getPropertyList() {
		return taskPropertyDefs;
	}

	@Override
	public String[] getPropertyValues(Config config, TaskPropertyDef propertyDef, TaskProperties taskProperties)
			throws SearchLibException {
		return null;
	}

	@Override
	public String getDefaultValue(Config config, TaskPropertyDef propertyDef) {
		if (propertyDef == propUrl)
			return "https://cloud.opensearchserver.com/oss-monitor/";
		return null;
	}

	@Override
	public void execute(Client client, TaskProperties properties, Variables variables, TaskLog taskLog)
			throws SearchLibException, IOException {
		String url = properties.getValue(propUrl);
		URI uri;
		try {
			uri = new URI(url);
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		}
		String login = properties.getValue(propLogin);
		String password = properties.getValue(propPassword);
		String instanceId = properties.getValue(propInstanceId);

		CredentialItem credentialItem = null;
		if (!StringUtils.isEmpty(login) && !StringUtils.isEmpty(password))
			credentialItem = new CredentialItem(CredentialType.BASIC_DIGEST, null, login, password, null, null);
		HttpDownloader downloader = client.getWebCrawlMaster().getNewHttpDownloader(true);
		try {

			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			nvps.add(new BasicNameValuePair("instanceId", instanceId));
			new Monitor().writeToPost(nvps);
			DownloadItem downloadItem = downloader.post(uri, credentialItem, null, null,
					new UrlEncodedFormEntity(nvps));
			if (downloadItem.getStatusCode() != 200)
				throw new SearchLibException(
						"Wrong code status:" + downloadItem.getStatusCode() + " " + downloadItem.getReasonPhrase());
			taskLog.setInfo("Monitoring data uploaded");
		} catch (ClientProtocolException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (IllegalStateException e) {
			throw new SearchLibException(e);
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		} finally {
			downloader.release();
		}

	}
}
