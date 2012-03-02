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

package com.jaeksoft.searchlib.request;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class RequestMap {

	private Map<String, AbstractRequest> map;

	/**
	 * Returns a map containing the request read from the XML configuration
	 * file.
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
	public static RequestMap fromXmlConfig(Config config, XPathParser xpp,
			Node parentNode) throws XPathExpressionException, DOMException,
			ParseException, InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		RequestMap requestMap = new RequestMap();
		if (parentNode == null)
			return requestMap;
		NodeList nodes = xpp.getNodeList(parentNode, "request");
		if (nodes == null)
			return requestMap;
		for (int i = 0; i < nodes.getLength(); i++) {
			AbstractRequest request = RequestTypeEnum.fromXmlConfig(config,
					xpp, nodes.item(i));
			if (request != null)
				requestMap.put(request);
		}
		return requestMap;
	}

	private RequestMap() {
		map = new TreeMap<String, AbstractRequest>();
	}

	public void put(AbstractRequest request) {
		map.put(request.getRequestName(), request);
	}

	public AbstractRequest get(String requestName) {
		if (requestName == null)
			return null;
		return map.get(requestName);
	}

	public void writeXmlConfig(XmlWriter xmlWriter) throws SAXException {
		xmlWriter.startElement("requests");
		for (AbstractRequest request : map.values())
			request.writeXmlConfig(xmlWriter);
		xmlWriter.endElement();
	}

	public Set<Entry<String, AbstractRequest>> getRequests() {
		return map.entrySet();
	}

	public Set<String> getNameList() {
		return map.keySet();
	}

	public void remove(String requestName) {
		map.remove(requestName);
	}
}
