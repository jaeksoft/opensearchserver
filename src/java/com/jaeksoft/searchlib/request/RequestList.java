/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
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
import java.util.HashSet;

import javax.xml.xpath.XPathExpressionException;

import org.apache.lucene.queryParser.ParseException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlInfo;

public class RequestList extends HashMap<String, Request> implements XmlInfo {

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
	 */
	public static RequestList fromXmlConfig(Config config, XPathParser xpp,
			Node parentNode) throws XPathExpressionException, DOMException,
			ParseException {
		RequestList requestList = new RequestList();
		if (parentNode == null)
			return requestList;
		NodeList nodes = xpp.getNodeList(parentNode, "request");
		if (nodes == null)
			return requestList;
		for (int i = 0; i < nodes.getLength(); i++) {
			TemplateRequest templateRequest = TemplateRequest.fromXmlConfig(
					config, xpp, nodes.item(i));
			requestList.put(templateRequest.getName(), templateRequest);
		}
		return requestList;
	}

	public void xmlInfo(PrintWriter writer, HashSet<String> classDetail) {
		writer.println("<requests>");
		for (Request request : this.values())
			request.xmlInfo(writer, classDetail);
		writer.println("</requests>");
	}

}
