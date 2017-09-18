/**
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2008-2016 Emmanuel Keller / Jaeksoft
 * <p>
 * http://www.open-search-server.com
 * <p>
 * This file is part of OpenSearchServer.
 * <p>
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.
 * If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.request;

import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.xpath.XPathExpressionException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

public class RequestMap implements XmlWriter.Interface {

	private final Map<String, AbstractRequest> map;

	/**
	 * Returns a map containing the request read from the XML configuration
	 * file.
	 *
	 * @param config
         * @param xpp 
         * @param parentNode 
	 * @throws XPathExpressionException
	 * @throws ParseException
	 * @throws DOMException
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public static RequestMap fromXmlConfig(Config config, XPathParser xpp, Node parentNode)
			throws XPathExpressionException, DOMException, ParseException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		RequestMap requestMap = new RequestMap();
		if (parentNode == null)
			return requestMap;
		NodeList nodes = xpp.getNodeList(parentNode, "request");
		if (nodes == null)
			return requestMap;
		for (int i = 0; i < nodes.getLength(); i++) {
			AbstractRequest request = RequestTypeEnum.fromXmlConfig(config, xpp, nodes.item(i));
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

	@Override
	public void writeXml(XmlWriter xmlWriter) throws SAXException {
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

	public void getNameList(List<String> nameList, RequestTypeEnum... types) {
		for (AbstractRequest request : map.values()) {
			boolean found = false;
			for (RequestTypeEnum type : types) {
				if (request.requestType == type) {
					found = true;
					break;
				}
			}
			if (found)
				nameList.add(request.getRequestName());
		}
	}

	public void remove(String requestName) {
		map.remove(requestName);
	}
}
