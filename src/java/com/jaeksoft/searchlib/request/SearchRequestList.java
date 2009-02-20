/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft SearchLib Community.
 *
 * Jaeksoft SearchLib Community is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft SearchLib Community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft SearchLib Community. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.request;

import java.io.PrintWriter;
import java.util.HashMap;

import javax.xml.xpath.XPathExpressionException;

import org.apache.lucene.queryParser.ParseException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlInfo;

public class SearchRequestList extends HashMap<String, SearchRequest> implements
		XmlInfo {

	/**
	 * RequestList est une liste de Request.
	 */
	private static final long serialVersionUID = -7165162765377426369L;

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
	public static SearchRequestList fromXmlConfig(Config config,
			XPathParser xpp, Node parentNode) throws XPathExpressionException,
			DOMException, ParseException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		SearchRequestList searchRequestList = new SearchRequestList();
		if (parentNode == null)
			return searchRequestList;
		NodeList nodes = xpp.getNodeList(parentNode, "request");
		if (nodes == null)
			return searchRequestList;
		for (int i = 0; i < nodes.getLength(); i++) {
			SearchRequest searchRequest = SearchRequest.fromXmlConfig(config,
					xpp, nodes.item(i));
			searchRequestList
					.put(searchRequest.getRequestName(), searchRequest);
		}
		return searchRequestList;
	}

	public void xmlInfo(PrintWriter writer) {
		writer.println("<requests>");
		for (SearchRequest request : this.values())
			request.xmlInfo(writer);
		writer.println("</requests>");
	}

}
