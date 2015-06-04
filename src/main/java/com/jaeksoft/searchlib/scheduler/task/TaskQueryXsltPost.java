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

import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.StringEntity;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.web.database.CredentialItem;
import com.jaeksoft.searchlib.crawler.web.database.CredentialItem.CredentialType;
import com.jaeksoft.searchlib.crawler.web.database.HeaderItem;
import com.jaeksoft.searchlib.crawler.web.spider.DownloadItem;
import com.jaeksoft.searchlib.crawler.web.spider.HttpDownloader;
import com.jaeksoft.searchlib.render.RenderSearchXml;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.request.RequestTypeEnum;
import com.jaeksoft.searchlib.result.AbstractResultSearch;
import com.jaeksoft.searchlib.scheduler.TaskAbstract;
import com.jaeksoft.searchlib.scheduler.TaskLog;
import com.jaeksoft.searchlib.scheduler.TaskProperties;
import com.jaeksoft.searchlib.scheduler.TaskPropertyDef;
import com.jaeksoft.searchlib.scheduler.TaskPropertyType;
import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.IOUtils;
import com.jaeksoft.searchlib.util.Variables;

public class TaskQueryXsltPost extends TaskAbstract {

	final private TaskPropertyDef propSearchTemplate = new TaskPropertyDef(
			TaskPropertyType.comboBox, "Search template", "SearchTemplate",
			"The search query to use", 50);

	final private TaskPropertyDef propQueryString = new TaskPropertyDef(
			TaskPropertyType.textBox, "Query string", "QueryString",
			"The query string to pass to the search template", 50);

	final private TaskPropertyDef propXsl = new TaskPropertyDef(
			TaskPropertyType.multilineTextBox, "XSL", "XSL",
			"An optional XSL stylesheet", 100, 30);

	final private TaskPropertyDef propUrl = new TaskPropertyDef(
			TaskPropertyType.textBox, "URL", "URL",
			"The URL of the remote server", 100);

	final private TaskPropertyDef propHttpLogin = new TaskPropertyDef(
			TaskPropertyType.textBox, "HTTP Login", "HttpLogin",
			"HTTP credential: login", 50);

	final private TaskPropertyDef propHttpPassword = new TaskPropertyDef(
			TaskPropertyType.password, "HTTP Password", "HttpPassword",
			"HTTP credential: password", 50);

	final private TaskPropertyDef propHttpContentType = new TaskPropertyDef(
			TaskPropertyType.textBox, "Content-Type", "ContentType",
			"The content-type header", 50);

	final private TaskPropertyDef propUseProxy = new TaskPropertyDef(
			TaskPropertyType.comboBox, "Use proxy", "UseProxy", null, 20);

	final private TaskPropertyDef[] taskPropertyDefs = { propSearchTemplate,
			propQueryString, propXsl, propUrl, propHttpLogin, propHttpPassword,
			propHttpContentType, propUseProxy };

	@Override
	public String getName() {
		return "Query XSL POST";
	}

	@Override
	public TaskPropertyDef[] getPropertyList() {
		return taskPropertyDefs;
	}

	@Override
	public String[] getPropertyValues(Config config,
			TaskPropertyDef propertyDef, TaskProperties taskProperties)
			throws SearchLibException {
		List<String> nameList = new ArrayList<String>(0);
		if (propSearchTemplate == propertyDef)
			config.getRequestMap().getNameList(nameList,
					RequestTypeEnum.SearchFieldRequest,
					RequestTypeEnum.SearchRequest);
		else if (propertyDef == propUseProxy)
			return ClassPropertyEnum.BOOLEAN_LIST;
		if (nameList.size() == 0)
			return null;
		return nameList.toArray(new String[nameList.size()]);
	}

	@Override
	public String getDefaultValue(Config config, TaskPropertyDef propertyDef) {
		if (propertyDef == propUseProxy)
			return Boolean.FALSE.toString();
		return null;
	}

	@Override
	public void execute(Client client, TaskProperties properties,
			Variables variables, TaskLog taskLog) throws SearchLibException {

		taskLog.setInfo("Query check");

		String searchTemplate = properties.getValue(propSearchTemplate);
		String queryString = properties.getValue(propQueryString);
		String xsl = properties.getValue(propXsl);
		String url = properties.getValue(propUrl);
		String httpLogin = properties.getValue(propHttpLogin);
		String httpPassword = properties.getValue(propHttpPassword);
		String contentType = properties.getValue(propHttpContentType);
		boolean useProxy = Boolean.TRUE.toString().equals(
				properties.getValue(propUseProxy));

		URI uri;
		try {
			uri = new URI(url);
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		}

		AbstractSearchRequest searchRequest = (AbstractSearchRequest) client
				.getNewRequest(searchTemplate);
		if (searchRequest == null)
			throw new SearchLibException("Request template  " + searchTemplate
					+ " not found");
		searchRequest.setQueryString(queryString);
		taskLog.setInfo("Execute request " + searchTemplate);
		@SuppressWarnings("unchecked")
		AbstractResultSearch<AbstractSearchRequest> resultSearch = (AbstractResultSearch<AbstractSearchRequest>) client
				.request(searchRequest);

		if (resultSearch == null || resultSearch.getNumFound() == 0) {
			taskLog.setInfo("Empty result");
			return;
		}

		StringWriter sw = null;
		PrintWriter pw = null;
		StringReader sr = null;

		try {

			sw = new StringWriter();
			pw = new PrintWriter(sw);
			taskLog.setInfo("Render XML");
			new RenderSearchXml<AbstractSearchRequest>(resultSearch).render(pw);
			pw.close();
			pw = null;
			sw.close();
			String content = sw.toString();
			sw = null;

			sr = new StringReader(content);

			if (!StringUtils.isEmpty(xsl)) {
				taskLog.setInfo("XSL transformation");
				content = DomUtils.xslt(new StreamSource(sr), xsl);
				if (content == null)
					throw new SearchLibException("XSL transformation failed");
			}

			CredentialItem credentialItem = null;
			if (!StringUtils.isEmpty(httpLogin)
					&& !StringUtils.isEmpty(httpPassword))
				credentialItem = new CredentialItem(
						CredentialType.BASIC_DIGEST, null, httpLogin,
						httpPassword, null, null);
			HttpDownloader downloader = client.getWebCrawlMaster()
					.getNewHttpDownloader(true, null, useProxy);

			List<HeaderItem> headerItems = null;
			if (!StringUtils.isEmpty(contentType)) {
				headerItems = new ArrayList<HeaderItem>(1);
				headerItems.add(new HeaderItem("Content-Type", contentType));
			}

			String size = FileUtils.byteCountToDisplaySize(content.length());
			taskLog.setInfo("Uploading " + size);
			DownloadItem downloadItem = downloader.post(uri, credentialItem,
					headerItems, null, new StringEntity(content));
			downloadItem.checkNoErrorRange(200, 201);
			taskLog.setInfo("Done " + size);
		} catch (Exception e) {
			throw new SearchLibException(e);
		} finally {
			IOUtils.close(pw, sw, sr);
		}

	}
}
