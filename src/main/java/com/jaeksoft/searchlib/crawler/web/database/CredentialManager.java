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

package com.jaeksoft.searchlib.crawler.web.database;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.util.LinkUtils;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class CredentialManager {

	final private ReadWriteLock rwl = new ReadWriteLock();

	// For better performances, pattern are grouped by hostname in a map
	private Map<String, List<CredentialItem>> credentialMap = null;

	private File credentialFile;

	public CredentialManager(File indexDir, String filename)
			throws SearchLibException {
		credentialFile = new File(indexDir, filename);
		credentialMap = new TreeMap<String, List<CredentialItem>>();
		try {
			load();
		} catch (ParserConfigurationException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (XPathExpressionException e) {
			throw new SearchLibException(e);
		}
	}

	private void load() throws ParserConfigurationException, SAXException,
			IOException, XPathExpressionException, SearchLibException {
		if (!credentialFile.exists())
			return;
		XPathParser xpp = new XPathParser(credentialFile);
		NodeList nodeList = xpp.getNodeList("/credentials/credential");
		int l = nodeList.getLength();
		for (int i = 0; i < l; i++) {
			Node node = nodeList.item(i);
			CredentialItem credentialItem = CredentialItem.fromXml(node);
			addCredential(credentialItem);
		}
	}

	private void store() throws IOException, TransformerConfigurationException,
			SAXException {
		if (!credentialFile.exists())
			credentialFile.createNewFile();
		PrintWriter pw = new PrintWriter(credentialFile);
		try {
			XmlWriter xmlWriter = new XmlWriter(pw, "UTF-8");
			xmlWriter.startElement("credentials");
			Iterator<List<CredentialItem>> it = credentialMap.values()
					.iterator();
			while (it.hasNext())
				for (CredentialItem item : it.next())
					item.writeXml(xmlWriter);
			xmlWriter.endElement();
			xmlWriter.endDocument();
		} finally {
			pw.close();
		}
	}

	public CredentialItem getCredential(String sPattern)
			throws MalformedURLException, URISyntaxException {
		rwl.r.lock();
		try {
			String host = LinkUtils.newEncodedURL(sPattern).getHost();
			List<CredentialItem> itemList = credentialMap.get(host);
			if (itemList == null)
				return null;
			Iterator<CredentialItem> it = itemList.iterator();
			while (it.hasNext()) {
				CredentialItem item = it.next();
				if (item.getPattern().equals(sPattern))
					return item;
			}
			return null;
		} finally {
			rwl.r.unlock();
		}
	}

	private void delCredentialWithoutLock(String sPattern)
			throws MalformedURLException, URISyntaxException {
		String host = LinkUtils.newEncodedURL(sPattern).getHost();
		List<CredentialItem> itemList = credentialMap.get(host);
		if (itemList == null)
			return;
		Iterator<CredentialItem> it = itemList.iterator();
		while (it.hasNext())
			if (it.next().getPattern().equals(sPattern))
				it.remove();
	}

	public void delCredential(String pattern) throws SearchLibException {
		rwl.w.lock();
		try {
			delCredentialWithoutLock(pattern);
			store();
		} catch (MalformedURLException e) {
			throw new SearchLibException(e);
		} catch (TransformerConfigurationException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		} finally {
			rwl.w.unlock();
		}
	}

	public void delCredentials(Collection<String> patterns)
			throws SearchLibException {
		rwl.w.lock();
		try {
			for (String pattern : patterns)
				delCredentialWithoutLock(pattern);
			store();
		} catch (MalformedURLException e) {
			throw new SearchLibException(e);
		} catch (TransformerConfigurationException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		} finally {
			rwl.w.unlock();
		}
	}

	private void addCredentialWithoutLock(CredentialItem credentialItem)
			throws MalformedURLException, URISyntaxException {
		String host = credentialItem.extractUrl().getHost();
		List<CredentialItem> itemList = credentialMap.get(host);
		if (itemList == null) {
			itemList = new ArrayList<CredentialItem>();
			credentialMap.put(host, itemList);
		}
		itemList.add(credentialItem);
	}

	public void addCredential(CredentialItem credentialItem)
			throws SearchLibException {
		rwl.w.lock();
		try {
			addCredentialWithoutLock(credentialItem);
			store();
		} catch (TransformerConfigurationException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} catch (URISyntaxException e) {
			throw new SearchLibException(e);
		} finally {
			rwl.w.unlock();
		}
	}

	public void updateCredential(CredentialItem credentialItem)
			throws SearchLibException {
		rwl.w.lock();
		try {
			boolean found = false;
			Iterator<List<CredentialItem>> it = credentialMap.values()
					.iterator();
			while (it.hasNext() && !found)
				found = it.next().contains(credentialItem);
			if (!found)
				throw new SearchLibException("Unknown credential item");
			store();
		} catch (TransformerConfigurationException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} finally {
			rwl.w.unlock();
		}
	}

	public int getCredentials(String startsWith, long start, long rows,
			List<CredentialItem> list) throws SearchLibException {
		rwl.r.lock();
		try {
			Iterator<List<CredentialItem>> it = credentialMap.values()
					.iterator();
			long end = start + rows;
			int pos = 0;
			int total = 0;
			while (it.hasNext())
				for (CredentialItem item : it.next()) {
					if (startsWith != null) {
						if (!item.getPattern().startsWith(startsWith)) {
							pos++;
							continue;
						}
					}
					if (pos >= start && pos < end)
						list.add(item);
					total++;
					pos++;
				}
			return total;
		} finally {
			rwl.r.unlock();
		}
	}

	public CredentialItem matchCredential(URL url) {
		rwl.r.lock();
		try {
			List<CredentialItem> credentialList = credentialMap.get(url
					.getHost());
			if (credentialList == null)
				return null;
			String sUrl = url.toExternalForm();
			for (CredentialItem credentialItem : credentialList)
				if (credentialItem.match(sUrl))
					return credentialItem;
			return null;
		} finally {
			rwl.r.unlock();
		}
	}

}
