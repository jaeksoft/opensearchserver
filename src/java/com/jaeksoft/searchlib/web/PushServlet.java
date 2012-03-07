/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.web;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import javax.naming.NamingException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FilenameUtils;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.remote.UriWriteStream;
import com.jaeksoft.searchlib.replication.ReplicationItem;
import com.jaeksoft.searchlib.user.User;
import com.jaeksoft.searchlib.util.XPathParser;

public class PushServlet extends AbstractServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 527058083952741700L;

	@Override
	protected void doRequest(ServletTransaction transaction)
			throws ServletException {
		try {

			User user = transaction.getLoggedUser();
			if (user != null && !user.isAdmin())
				throw new SearchLibException("Not permitted");

			Client client = transaction.getClient();

			String cmd = transaction.getParameterString("cmd");
			if (CALL_XML_CMD_INIT.equals(cmd)) {
				transaction.addXmlResponse(CALL_XML_KEY_CMD, CALL_XML_CMD_INIT);
				ClientCatalog.receive_init(client);
				transaction.addXmlResponse(XML_CALL_KEY_STATUS,
						XML_CALL_KEY_STATUS_OK);
				return;
			}
			if (CALL_XML_CMD_SWITCH.equals(cmd)) {
				transaction.addXmlResponse(CALL_XML_KEY_CMD,
						CALL_XML_CMD_SWITCH);
				ClientCatalog.receive_switch(transaction.getWebApp(), client);
				transaction.addXmlResponse(XML_CALL_KEY_STATUS,
						XML_CALL_KEY_STATUS_OK);
				return;
			}

			transaction.addXmlResponse(CALL_XML_KEY_CMD, CALL_XML_CMD_FILEPATH);
			String filePath = transaction
					.getParameterString(CALL_XML_CMD_FILEPATH);
			if (FilenameUtils.getName(filePath).startsWith(".")) {
				transaction.addXmlResponse(XML_CALL_KEY_STATUS,
						XML_CALL_KEY_STATUS_OK);
				return;
			}

			if (transaction.getParameterBoolean("type", "dir", false))
				ClientCatalog.receive_dir(client, filePath);
			else
				ClientCatalog.receive_file(client, filePath,
						transaction.getInputStream());

			transaction.addXmlResponse(XML_CALL_KEY_STATUS,
					XML_CALL_KEY_STATUS_OK);

		} catch (SearchLibException e) {
			throw new ServletException(e);
		} catch (NamingException e) {
			throw new ServletException(e);
		} catch (InterruptedException e) {
			throw new ServletException(e);
		} catch (IOException e) {
			throw new ServletException(e);
		}

	}

	private final static String CALL_XML_KEY_CMD = "cmd";
	private final static String CALL_XML_CMD_INIT = "init";
	private final static String CALL_XML_CMD_SWITCH = "switch";
	private final static String CALL_XML_CMD_FILEPATH = "filePath";

	private static String getPushTargetUrl(Client client,
			ReplicationItem replicationItem, File sourceFile)
			throws UnsupportedEncodingException, SearchLibException,
			MalformedURLException {
		String dataPath = client.getDirectory().getAbsolutePath();
		String filePath = sourceFile.getAbsolutePath();
		if (!filePath.startsWith(dataPath))
			throw new SearchLibException("Bad file path " + filePath);
		filePath = filePath.substring(dataPath.length());
		return replicationItem.getCachedUrl() + "&filePath="
				+ URLEncoder.encode(filePath, "UTF-8")
				+ (sourceFile.isDirectory() ? "&type=dir" : "");
	}

	private static String getPushTargetUrl(ReplicationItem replicationItem,
			String cmd) throws UnsupportedEncodingException,
			MalformedURLException {
		return replicationItem.getCachedUrl() + "&" + CALL_XML_KEY_CMD + "="
				+ URLEncoder.encode(cmd, "UTF-8");
	}

	private static void call(URI uri, String cmd) throws SearchLibException {
		XPathParser xpp = AbstractServlet.call(uri);
		checkCallError(xpp);
		checkCallStatusOK(xpp);
		checkCallKey(xpp, CALL_XML_KEY_CMD, cmd);
	}

	private static void call(ReplicationItem replicationItem, String cmd)
			throws SearchLibException {
		try {
			call(new URI(getPushTargetUrl(replicationItem, cmd)), cmd);
		} catch (UnsupportedEncodingException e) {
			throw new SearchLibException(e);
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		} catch (MalformedURLException e) {
			throw new SearchLibException(e);
		}
	}

	public static void call_init(ReplicationItem replicationItem)
			throws SearchLibException {
		call(replicationItem, CALL_XML_CMD_INIT);
	}

	public static void call_switch(ReplicationItem replicationItem)
			throws SearchLibException {
		call(replicationItem, CALL_XML_CMD_SWITCH);
	}

	public static void call_file(Client client,
			ReplicationItem replicationItem, File file)
			throws SearchLibException {
		UriWriteStream uriWriteStream = null;
		try {
			String url = getPushTargetUrl(client, replicationItem, file);
			URI uri = new URI(url);
			uriWriteStream = new UriWriteStream(uri, file);
			XPathParser xpp = uriWriteStream.getXmlContent();
			checkCallError(xpp);
			checkCallStatusOK(xpp);
			checkCallKey(xpp, CALL_XML_KEY_CMD, CALL_XML_CMD_FILEPATH);
		} catch (UnsupportedEncodingException e) {
			throw new SearchLibException(e);
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		} catch (IllegalStateException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (ParserConfigurationException e) {
			throw new SearchLibException(e);
		} finally {
			if (uriWriteStream != null)
				uriWriteStream.close();
		}
	}

	public static void call_directory(Client client,
			ReplicationItem replicationItem, File file)
			throws SearchLibException {
		try {
			call(new URI(getPushTargetUrl(client, replicationItem, file)),
					CALL_XML_CMD_FILEPATH);
		} catch (UnsupportedEncodingException e) {
			throw new SearchLibException(e);
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		} catch (MalformedURLException e) {
			throw new SearchLibException(e);
		}
	}

	public static String getCachedUrl(ReplicationItem replicationItem)
			throws UnsupportedEncodingException, MalformedURLException {
		String url = replicationItem.getInstanceUrl().toExternalForm();
		String cachedUrl = url + (url.endsWith("/") ? "" : '/') + "push?use="
				+ URLEncoder.encode(replicationItem.getIndexName(), "UTF-8");

		String login = replicationItem.getLogin();
		String apiKey = replicationItem.getApiKey();
		if (login != null && login.length() > 0 && apiKey != null
				&& apiKey.length() > 0)
			cachedUrl += "&login=" + URLEncoder.encode(login, "UTF-8")
					+ "&key=" + apiKey;
		return cachedUrl;
	}
}
