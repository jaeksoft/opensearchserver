/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2011 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.api;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class ApiManager {

	private File apiFile;
	private String API_ROOT_NODE = "api";
	final private ReadWriteLock rwl = new ReadWriteLock();

	public ApiManager(File indexDir, String filename)
			throws SearchLibException, XPathExpressionException,
			ParserConfigurationException, SAXException, IOException,
			TransformerConfigurationException {
		apiFile = new File(indexDir, filename);

	}

	public void createNewApi(Api api) throws TransformerConfigurationException,
			IOException, SAXException, XPathExpressionException,
			ParserConfigurationException, SearchLibException {
		create(api);

	}

	private void create(Api api) throws IOException, SAXException,
			TransformerConfigurationException, XPathExpressionException,
			ParserConfigurationException {
		if (!apiFile.exists())
			apiFile.createNewFile();
		PrintWriter pw = new PrintWriter(apiFile);
		try {
			XmlWriter xmlWriter = new XmlWriter(pw, "UTF-8");
			xmlWriter.startElement(API_ROOT_NODE);
			writeXml(xmlWriter, api);
			xmlWriter.endElement();
			xmlWriter.endDocument();

		} finally {
			pw.close();
		}
	}

	public String getvalue(String apiName) throws XPathExpressionException,
			ParserConfigurationException, SAXException, IOException {
		return getQueryTemplateName(apiName);

	}

	private String getQueryTemplateName(String apiName)
			throws XPathExpressionException, ParserConfigurationException,
			SAXException, IOException {
		XPathParser xpp = new XPathParser(apiFile);
		NodeList nodeList = xpp.getNodeList("/api/" + apiName);
		return DomUtils.getText(nodeList.item(0));

	}

	public void writeXml(XmlWriter xmlWriter, Api api) throws SAXException,
			XPathExpressionException, ParserConfigurationException, IOException {
		rwl.r.lock();
		try {
			xmlWriter.startElement(api.getApiName());
			xmlWriter.writeSubTextNodeIfAny("querytemplate",
					api.getQueryTemplate());
			xmlWriter.endElement();

		} finally {
			rwl.r.unlock();
		}
	}
}