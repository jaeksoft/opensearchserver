/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
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

import javax.xml.xpath.XPathExpressionException;

import org.apache.lucene.queryParser.ParseException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.util.XPathParser;

public enum RequestTypeEnum {

	SearchRequest(SearchRequest.class, "Search"),

	SpellCheckRequest(SpellCheckRequest.class, "Spell check"),

	MoreLikeThisRequest(MoreLikeThisRequest.class, "More like this");

	private Class<? extends AbstractRequest> requestClass;

	private String label;

	private RequestTypeEnum(Class<? extends AbstractRequest> requestClass,
			String label) {
		this.requestClass = requestClass;
		this.label = label;
	}

	public AbstractRequest newInstance(Config config)
			throws InstantiationException, IllegalAccessException {
		AbstractRequest request = requestClass.newInstance();
		request.init(config);
		return request;
	}

	private static RequestTypeEnum findByName(String name) {
		if (name == null)
			return SearchRequest;
		for (RequestTypeEnum type : values())
			if (name.equalsIgnoreCase(type.name()))
				return type;
		return SearchRequest;
	}

	public static AbstractRequest fromXmlConfig(Config config, XPathParser xpp,
			Node node) throws InstantiationException, IllegalAccessException,
			XPathExpressionException, DOMException, ParseException,
			ClassNotFoundException {
		if (node == null)
			return null;
		RequestTypeEnum type = findByName(XPathParser.getAttributeString(node,
				AbstractRequest.XML_ATTR_TYPE));
		AbstractRequest request = type.requestClass.newInstance();
		request.fromXmlConfig(config, xpp, node);
		return request;
	}

	public static AbstractRequest getNewCopy(AbstractRequest request)
			throws InstantiationException, IllegalAccessException {
		AbstractRequest newRequest = request.getType().requestClass
				.newInstance();
		newRequest.copyFrom(request);
		return newRequest;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

}
