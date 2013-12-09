/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2013 Emmanuel Keller / Jaeksoft
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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.Logging;
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
import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.Variables;

public class TaskXmlLoad extends TaskAbstract {

	final private TaskPropertyDef propUri = new TaskPropertyDef(
			TaskPropertyType.textBox, "URI", "Uri", null, 100);

	final private TaskPropertyDef propLogin = new TaskPropertyDef(
			TaskPropertyType.textBox, "Login", "Login", null, 50);

	final private TaskPropertyDef propPassword = new TaskPropertyDef(
			TaskPropertyType.password, "Password", "Password", null, 20);

	final private TaskPropertyDef propUserAgent = new TaskPropertyDef(
			TaskPropertyType.textBox, "User agent", "UserAgent", null, 20);

	final private TaskPropertyDef propBuffersize = new TaskPropertyDef(
			TaskPropertyType.textBox, "Buffer size", "Buffer size", null, 10);

	final private TaskPropertyDef propXsl = new TaskPropertyDef(
			TaskPropertyType.multilineTextBox, "XSL", "XSL", null, 100, 30);

	final private TaskPropertyDef[] taskPropertyDefs = { propUri, propLogin,
			propPassword, propUserAgent, propBuffersize, propXsl };

	@Override
	public String getName() {
		return "XML load";
	}

	@Override
	public TaskPropertyDef[] getPropertyList() {
		return taskPropertyDefs;
	}

	@Override
	public String[] getPropertyValues(Config config,
			TaskPropertyDef propertyDef, TaskProperties taskProperties)
			throws SearchLibException {
		return null;
	}

	@Override
	public String getDefaultValue(Config config, TaskPropertyDef propertyDef) {
		if (propertyDef == propBuffersize)
			return "50";
		else if (propertyDef == propUserAgent)
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
		String uri = properties.getValue(propUri);
		String login = properties.getValue(propLogin);
		String password = properties.getValue(propPassword);
		String p = properties.getValue(propBuffersize);
		String xsl = properties.getValue(propXsl);
		String userAgent = properties.getValue(propUserAgent);
		File xmlTempResult = null;
		int bufferSize = 50;
		if (p != null && p.length() > 0)
			bufferSize = Integer.parseInt(p);
		HttpDownloader httpDownloader = client.getWebCrawlMaster()
				.getNewHttpDownloader(true, userAgent);
		try {
			CredentialItem credentialItem = null;
			if (login != null && password != null)
				credentialItem = new CredentialItem(
						CredentialType.BASIC_DIGEST, null, login, password,
						null, null);
			DownloadItem downloadItem = httpDownloader.get(new URI(uri),
					credentialItem);
			downloadItem.checkNoError(200);
			Node xmlDoc = null;
			if (xsl != null && xsl.length() > 0) {
				xmlTempResult = File.createTempFile("ossupload", ".xml");
				DomUtils.xslt(
						new StreamSource(downloadItem.getContentInputStream()),
						xsl, xmlTempResult);
				xmlDoc = DomUtils.readXml(new StreamSource(xmlTempResult),
						false);
			} else
				xmlDoc = DomUtils.readXml(
						new InputSource(downloadItem.getContentInputStream()),
						false);

			client.updateXmlDocuments(xmlDoc, bufferSize, credentialItem,
					httpDownloader, taskLog);
			client.deleteXmlDocuments(xmlDoc, bufferSize, taskLog);
		} catch (XPathExpressionException e) {
			throw new SearchLibException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new SearchLibException(e);
		} catch (ParserConfigurationException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		} catch (ClassNotFoundException e) {
			throw new SearchLibException(e);
		} catch (TransformerException e) {
			throw new SearchLibException(e);
		} finally {
			if (xmlTempResult != null)
				xmlTempResult.delete();
			httpDownloader.release();
		}
	}
}
