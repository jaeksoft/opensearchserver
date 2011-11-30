/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2011 Emmanuel Keller / Jaeksoft
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
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParamBean;
import org.apache.http.util.EntityUtils;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.Monitor;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.scheduler.TaskAbstract;
import com.jaeksoft.searchlib.scheduler.TaskProperties;
import com.jaeksoft.searchlib.scheduler.TaskPropertyDef;
import com.jaeksoft.searchlib.scheduler.TaskPropertyType;

public class TaskUploadMonitor extends TaskAbstract {

	final private TaskPropertyDef propUrl = new TaskPropertyDef(
			TaskPropertyType.textBox, "Url", 100);

	final private TaskPropertyDef propLogin = new TaskPropertyDef(
			TaskPropertyType.textBox, "Login", 50);

	final private TaskPropertyDef propPassword = new TaskPropertyDef(
			TaskPropertyType.password, "Password", 20);

	final private TaskPropertyDef propInstanceId = new TaskPropertyDef(
			TaskPropertyType.textBox, "Instance ID", 80);

	final private TaskPropertyDef[] taskPropertyDefs = { propUrl, propLogin,
			propPassword, propInstanceId };

	@Override
	public String getName() {
		return "Monitoring upload";
	}

	@Override
	public TaskPropertyDef[] getPropertyList() {
		return taskPropertyDefs;
	}

	@Override
	public String[] getPropertyValues(Config config, TaskPropertyDef propertyDef)
			throws SearchLibException {
		return null;
	}

	@Override
	public String getDefaultValue(Config config, TaskPropertyDef propertyDef) {
		if (propertyDef == propUrl)
			return "http://www.open-search-server.com/oss-monitor/";
		return null;
	}

	@Override
	public void execute(Client client, TaskProperties properties)
			throws SearchLibException {
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
		HttpParams params = new BasicHttpParams();
		HttpProtocolParamBean paramsBean = new HttpProtocolParamBean(params);
		paramsBean.setVersion(HttpVersion.HTTP_1_1);
		paramsBean.setContentCharset("UTF-8");
		HttpClientParams.setRedirecting(params, true);
		DefaultHttpClient httpClient = new DefaultHttpClient(params);
		CredentialsProvider credential = httpClient.getCredentialsProvider();
		if (login != null && login.length() > 0 && password != null
				&& password.length() > 0)
			credential.setCredentials(
					new AuthScope(uri.getHost(), uri.getPort()),
					new UsernamePasswordCredentials(login, password));
		else
			credential.clear();

		HttpPost httpPost = new HttpPost(uri);
		MultipartEntity reqEntity = new MultipartEntity();
		new Monitor().writeToPost(reqEntity);
		try {
			reqEntity.addPart("instanceId", new StringBody(instanceId));
		} catch (UnsupportedEncodingException e) {
			throw new SearchLibException(e);
		}
		httpPost.setEntity(reqEntity);
		try {
			HttpResponse httpResponse = httpClient.execute(httpPost);
			HttpEntity resEntity = httpResponse.getEntity();
			StatusLine statusLine = httpResponse.getStatusLine();
			EntityUtils.consume(resEntity);
			if (statusLine.getStatusCode() != 200)
				throw new SearchLibException("Wrong code status:"
						+ statusLine.getStatusCode() + " "
						+ statusLine.getReasonPhrase());
		} catch (ClientProtocolException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			ClientConnectionManager connectionManager = httpClient
					.getConnectionManager();
			if (connectionManager != null)
				connectionManager.shutdown();
		}

	}
}
