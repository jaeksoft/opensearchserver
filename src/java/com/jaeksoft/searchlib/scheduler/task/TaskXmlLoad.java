/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
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
import java.security.NoSuchAlgorithmException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.web.database.CredentialItem;
import com.jaeksoft.searchlib.crawler.web.spider.HttpDownloader;
import com.jaeksoft.searchlib.scheduler.TaskAbstract;
import com.jaeksoft.searchlib.scheduler.TaskProperties;

public class TaskXmlLoad extends TaskAbstract {

	private enum Properties {
		URI("Uri"), LOGIN("Login"), PASSWORD("Password"), BUFFERSIZE(
				"Buffer size");

		private String label;

		private Properties(String label) {
			this.label = label;
		}

	}

	private String[] propsName = { Properties.URI.label,
			Properties.LOGIN.label, Properties.PASSWORD.label,
			Properties.BUFFERSIZE.label };

	@Override
	public String getName() {
		return "XML load";
	}

	@Override
	public String[] getPropertyList() {
		return propsName;
	}

	@Override
	public String[] getPropertyValues(Config config, String property)
			throws SearchLibException {
		return null;
	}

	@Override
	public int getPropertyCols(Config config, String name) {
		if (Properties.URI.label.equals(name))
			return 100;
		if (Properties.LOGIN.label.equals(name))
			return 50;
		if (Properties.PASSWORD.label.equals(name))
			return 20;
		return 50;
	}

	@Override
	public void execute(Client client, TaskProperties properties)
			throws SearchLibException {
		String uri = properties.getValue(Properties.URI.label);
		String login = properties.getValue(Properties.LOGIN.label);
		String password = properties.getValue(Properties.PASSWORD.label);
		String p = properties.getValue(Properties.BUFFERSIZE.label);
		int bufferSize = 50;
		if (p != null && p.length() > 0)
			bufferSize = Integer.parseInt(p);
		HttpDownloader httpDownloader = new HttpDownloader(null, false, null, 0);
		try {
			CredentialItem credentialItem = null;
			if (login != null && password != null)
				credentialItem = new CredentialItem(null, login, password);
			httpDownloader.get(new URI(uri), credentialItem);
			client.updateXmlDocuments(
					new InputSource(httpDownloader.getContent()), bufferSize);
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
		} finally {
			httpDownloader.release();
		}
	}
}
