/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.renderer;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

/**
 * @author Ayyathurai N Naveen
 * 
 */
public class RendererLogField {

	private final static String RENDERER_LOG_FIELD_ATTR_CUSTOMLOGNAME = "customLogName";

	private final static String RENDERER_LOG_FIELD_ATTR_PARAMETERNAME = "parameterName";

	private String customlogItem;

	private RendererLogParameterEnum logParameterEnum;

	public RendererLogField() {
		this.customlogItem = StringUtils.EMPTY;
	}

	public RendererLogField(RendererLogField logReportField) {
		copyTo(logReportField);
	}

	public RendererLogField(XPathParser xpp, Node node)
			throws XPathExpressionException {
		setCustomlogItem(XPathParser.getAttributeString(node,
				RENDERER_LOG_FIELD_ATTR_CUSTOMLOGNAME));
		setLogParameterEnum(RendererLogParameterEnum
				.find(XPathParser.getAttributeString(node,
						RENDERER_LOG_FIELD_ATTR_PARAMETERNAME)));
	}

	public void copyTo(RendererLogField logReportField) {
		this.customlogItem = logReportField.getCustomlogItem();
		this.logParameterEnum = logReportField.getLogParameterEnum();
	}

	public RendererLogParameterEnum getLogParameterEnum() {
		return logParameterEnum;
	}

	public void setLogParameterEnum(RendererLogParameterEnum logReportEnum) {
		this.logParameterEnum = logReportEnum;
	}

	public String getCustomlogItem() {
		return customlogItem;
	}

	public void setCustomlogItem(String customlogItem) {
		this.customlogItem = customlogItem;
	}

	public void writeXml(XmlWriter xmlWriter, String nodeName)
			throws SAXException {
		xmlWriter.startElement(nodeName, RENDERER_LOG_FIELD_ATTR_CUSTOMLOGNAME,
				customlogItem, RENDERER_LOG_FIELD_ATTR_PARAMETERNAME,
				logParameterEnum.name());

		xmlWriter.endElement();
	}
}
