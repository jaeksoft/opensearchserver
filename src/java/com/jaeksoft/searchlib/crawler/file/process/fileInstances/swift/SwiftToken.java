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

package com.jaeksoft.searchlib.crawler.file.process.fileInstances.swift;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.spider.DownloadItem;
import com.jaeksoft.searchlib.crawler.web.spider.HttpDownloader;

public class SwiftToken {

	public enum AuthType {
		KEYSTONE("Keystone"), IAM("IAM");

		private final String label;

		private AuthType(String label) {
			this.label = label;
		}

		public static AuthType find(String type) {
			for (AuthType authType : values())
				if (authType.name().equalsIgnoreCase(type))
					return authType;
			return null;
		}

		public String getLabel() {
			return label;
		}
	}

	public static final String X_Auth_Token = "X-Auth-Token";

	private final String internalURL;
	private final String publicURL;
	private final String authToken;

	public SwiftToken(HttpDownloader httpDownloader, String authUrl,
			String username, String password, AuthType authType, String tenant)
			throws URISyntaxException, ClientProtocolException, IOException,
			JSONException, SearchLibException {

		DownloadItem downloadItem = null;
		switch (authType) {
		case KEYSTONE:
			downloadItem = keystoneRequest(httpDownloader, authUrl, username,
					tenant, password);
			break;
		case IAM:
			downloadItem = iamRequest(httpDownloader, authUrl, username, tenant);
			break;
		}
		if (downloadItem == null)
			throw new SearchLibException("Authentication failed");

		JSONObject json = new JSONObject(downloadItem.getContentInputStream());
		JSONObject jsonAccess = json.getJSONObject("access");
		json = jsonAccess.getJSONObject("token");
		authToken = json.getString("id");
		JSONArray jsonServices = jsonAccess.getJSONArray("serviceCatalog");
		String intUrl = null;
		String pubUrl = null;
		for (int i = 0; i < jsonServices.length(); i++) {
			JSONObject jsonService = jsonServices.getJSONObject(i);
			String type = jsonService.getString("type");
			String name = jsonService.getString("name");
			if ("object-store".equals(type) && "swift".equals(name)) {
				JSONArray jsonEndpoints = jsonAccess.getJSONArray("endpoints");
				for (int j = 0; j < jsonEndpoints.length(); j++) {
					JSONObject jsonEndpoint = jsonEndpoints.getJSONObject(i);
					intUrl = jsonEndpoint.getString("internalURL");
					pubUrl = jsonEndpoint.getString("publicURL");
					if (intUrl != null && pubUrl != null)
						break;
				}
				break;
			}
		}
		internalURL = intUrl;
		publicURL = pubUrl;
	}

	private DownloadItem keystoneRequest(HttpDownloader httpDownloader,
			String authUrl, String username, String tenantName, String password)
			throws JSONException, URISyntaxException, ClientProtocolException,
			UnsupportedEncodingException, IOException {
		JSONObject jsonPasswordCredentials = new JSONObject();
		jsonPasswordCredentials.put("username", username);
		jsonPasswordCredentials.put("password", password);
		JSONObject jsonAuth = new JSONObject();
		jsonAuth.put("passwordCredentials", jsonPasswordCredentials);
		jsonAuth.put("tenantName", tenantName);
		URI uri = new URI(authUrl);
		System.out.println(jsonAuth.toString());
		return httpDownloader.post(uri, null,
				new StringEntity(jsonAuth.toString(),
						ContentType.APPLICATION_JSON));
	}

	private DownloadItem iamRequest(HttpDownloader httpDownloader,
			String authUrl, String username, String tenantname)
			throws URISyntaxException, ClientProtocolException, IOException {
		username = URLEncoder.encode(username, "UTF-8");
		StringBuffer u = new StringBuffer(authUrl);
		u.append("/users/");
		u.append(username);
		u.append("/credentials/openstack?tenantname=");
		u.append(tenantname);
		URI uri = new URI(u.toString());
		return httpDownloader.get(uri, null);
	}

	public void putAuthTokenHeader(List<Header> headerList) {
		headerList.add(new BasicHeader(X_Auth_Token, authToken));
	}

	public String getInternalURL() {
		return internalURL;
	}

	public String getPublicURL() {
		return publicURL;
	}
}
