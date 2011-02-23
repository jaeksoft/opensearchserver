/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2011 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.request;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.xpath.XPathExpressionException;

import org.apache.lucene.queryParser.ParseException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class SearchRequestMap {

	/**
	 * RequestList est une liste de Request.
	 */
	private static final long serialVersionUID = -7165162765377426369L;

	private Map<String, SearchRequest> map;

	/**
	 * Construit une liste de requ�tes � partir du fichier de config XML.
	 * 
	 * @param config
	 * @param document
	 * @param xPath
	 * @throws XPathExpressionException
	 * @throws ParseException
	 * @throws DOMException
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public static SearchRequestMap fromXmlConfig(Config config,
			XPathParser xpp, Node parentNode) throws XPathExpressionException,
			DOMException, ParseException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		SearchRequestMap searchRequestList = new SearchRequestMap();
		if (parentNode == null)
			return searchRequestList;
		NodeList nodes = xpp.getNodeList(parentNode, "request");
		if (nodes == null)
			return searchRequestList;
		for (int i = 0; i < nodes.getLength(); i++) {
			SearchRequest searchRequest = SearchRequest.fromXmlConfig(config,
					xpp, nodes.item(i));
			searchRequestList.put(searchRequest);
		}
		return searchRequestList;
	}

	private SearchRequestMap() {
		map = new TreeMap<String, SearchRequest>();
	}

	public void put(SearchRequest searchRequest) {
		map.put(searchRequest.getRequestName(), searchRequest);
	}

	public SearchRequest get(String requestName) {
		return map.get(requestName);
	}

	public void writeXmlConfig(XmlWriter xmlWriter) throws SAXException {
		xmlWriter.startElement("requests");
		for (SearchRequest request : map.values())
			request.writeXmlConfig(xmlWriter);
		xmlWriter.endElement();
	}

	public Set<Entry<String, SearchRequest>> getRequests() {
		return map.entrySet();
	}

	public Set<String> getNameList() {
		return map.keySet();
	}

	public void remove(String requestName) {
		map.remove(requestName);
	}
}
