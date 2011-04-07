/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2011 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.renderer;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.jaeksoft.searchlib.web.RendererServlet;

public class Renderer implements Comparable<Renderer> {

	private final static String RENDERER_ITEM_ROOTNODE_NAME = "renderer";
	private final static String RENDERER_ITEM_ROOT_ATTR_NAME = "name";
	private final static String RENDERER_ITEM_ROOT_ATTR_REQUEST = "request";
	private final static String RENDERER_ITEM_ROOT_ATTR_INPUTSTYLE = "inputStyle";
	private final static String RENDERER_ITEM_ROOT_ATTR_SEARCHBUTTONLABEL = "searchButtonLabel";
	private final static String RENDERER_ITEM_NODE_NAME_FIELD = "field";
	private final static String RENDERER_ITEM_NODE_STYLE = "style";

	private final ReadWriteLock rwl = new ReadWriteLock();

	private String name;

	private String requestName;

	private String style;

	private String inputStyle;

	private String searchButtonLabel;
	private String hyperlink,fonts;

	private List<RendererField> fields;

	public Renderer() {
		name = null;
		requestName = null;
		style = null;
		inputStyle = null;
		searchButtonLabel = "Search";
		fields = new ArrayList<RendererField>();
	}

	public Renderer(File file) throws ParserConfigurationException,
			SAXException, IOException, XPathExpressionException {
		this();
		if (!file.exists())
			return;
		XPathParser xpp = new XPathParser(file);
		Node rootNode = xpp.getNode(RENDERER_ITEM_ROOTNODE_NAME);
		if (rootNode == null)
			return;
		setName(XPathParser.getAttributeString(rootNode,
				RENDERER_ITEM_ROOT_ATTR_NAME));
		setRequestName(XPathParser.getAttributeString(rootNode,
				RENDERER_ITEM_ROOT_ATTR_REQUEST));
		setInputStyle(XPathParser.getAttributeString(rootNode,
				RENDERER_ITEM_ROOT_ATTR_INPUTSTYLE));
		setSearchButtonLabel(XPathParser.getAttributeString(rootNode,
				RENDERER_ITEM_ROOT_ATTR_SEARCHBUTTONLABEL));
		Node styleNode = xpp.getNode(rootNode, RENDERER_ITEM_NODE_STYLE);
		if (styleNode != null)
			setStyle(styleNode.getTextContent());
		NodeList nodeList = xpp.getNodeList(rootNode,
				RENDERER_ITEM_NODE_NAME_FIELD);
		for (int i = 0; i < nodeList.getLength(); i++)
			addField(new RendererField(nodeList.item(i)));
	}

	public Renderer(Renderer source) {
		this();
		source.copyTo(this);
	}

	public void copyTo(Renderer target) {
		rwl.r.lock();
		try {
			target.rwl.w.lock();
			try {
				target.name = name;
				target.requestName = requestName;
				target.style = style;
				target.inputStyle = inputStyle;
				target.searchButtonLabel = searchButtonLabel;
				target.fields.clear();
				for (RendererField field : fields)
					target.addField(new RendererField(field));
			} finally {
				target.rwl.w.unlock();
			}
		} finally {
			rwl.r.unlock();
		}
	}

	public void addField(RendererField field) {
		rwl.w.lock();
		try {
			fields.add(field);
		} finally {
			rwl.w.unlock();
		}
	}

	public void removeField(RendererField field) {
		rwl.w.lock();
		try {
			fields.remove(field);
		} finally {
			rwl.w.unlock();
		}

	}

	/**
	 * Move field up
	 * 
	 * @param field
	 */
	public void fieldUp(RendererField field) {
		rwl.w.lock();
		try {
			int i = fields.indexOf(field);
			if (i == -1 || i == 0)
				return;
			fields.remove(i);
			fields.add(i - 1, field);
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * Move field down
	 * 
	 * @param field
	 */
	public void fieldDown(RendererField field) {
		rwl.w.lock();
		try {
			int i = fields.indexOf(field);
			if (i == -1 || i == fields.size() - 1)
				return;
			fields.remove(i);
			fields.add(i + 1, field);
		} finally {
			rwl.w.unlock();
		}
	}

	public List<RendererField> getFields() {
		rwl.r.lock();
		try {
			return fields;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		rwl.w.lock();
		try {
			this.name = name;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @param style
	 *            the style to set
	 */
	public void setStyle(String style) {
		this.style = style;
	}

	/**
	 * @return the style
	 */
	public String getStyle() {
		
		style=setHyperLinkStyle("FF0000","00FF00","FF00FF","0000FF");
		style+="\n"+setFont("Arial");
		return style;
	}
	public String setFont(String font)
	{
		fonts=".osscmnrdr{";
		
		fonts+="font-style:normal;";
		fonts+="font-size:14px;";
		fonts+="font-family:"+font+";";
		fonts+="}";
		return fonts;
	}
public String setHyperLinkStyle(String link,String visited,String hover,String active)
{
	hyperlink="a:link {color:#"+link+";} ";
	hyperlink+="a:visited {color:#"+visited+";}";
	hyperlink+="a:hover {color:#"+hover+";}";
	hyperlink+="a:active {color:#"+active+";}";
	return hyperlink;
	
}
	/**
	 * @return the name
	 */
	public String getName() {
		rwl.r.lock();
		try {
			return name;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param name
	 *            the requestName to set
	 */
	public void setRequestName(String requestName) {
		rwl.w.lock();
		try {
			this.requestName = requestName;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the requestName
	 */
	public String getRequestName() {
		rwl.r.lock();
		try {
			return requestName;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param inputStyle
	 *            the inputStyle to set
	 */
	public void setInputStyle(String inputStyle) {
		this.inputStyle = inputStyle;
	}

	/**
	 * @return the inputStyle
	 */
	public String getInputStyle() {
		return inputStyle;
	}

	/**
	 * @param searchButtonLabel
	 *            the searchButtonLabel to set
	 */
	public void setSearchButtonLabel(String searchButtonLabel) {
		this.searchButtonLabel = searchButtonLabel;
	}

	/**
	 * @return the searchButtonLabel
	 */
	public String getSearchButtonLabel() {
		return searchButtonLabel;
	}

	@Override
	public final int compareTo(Renderer o) {
		return name.compareTo(o.name);
	}

	public void writeXml(XmlWriter xmlWriter) throws SAXException {
		rwl.r.lock();
		try {
			xmlWriter.startElement(RENDERER_ITEM_ROOTNODE_NAME,
					RENDERER_ITEM_ROOT_ATTR_NAME, name,
					RENDERER_ITEM_ROOT_ATTR_REQUEST, requestName,
					RENDERER_ITEM_ROOT_ATTR_INPUTSTYLE, inputStyle,
					RENDERER_ITEM_ROOT_ATTR_SEARCHBUTTONLABEL,
					searchButtonLabel);
			xmlWriter.startElement(RENDERER_ITEM_NODE_STYLE);
			xmlWriter.textNode(style);
			xmlWriter.endElement();
			for (RendererField field : fields)
				field.writeXml(xmlWriter, RENDERER_ITEM_NODE_NAME_FIELD);
			xmlWriter.endElement();
		} finally {
			rwl.r.unlock();
		}
	}

	public String getApiUrl() throws UnsupportedEncodingException {
		return RendererServlet.doRenderer(name, null);
	}

	public String getIFrameHtmlCode(String width, String height)
			throws UnsupportedEncodingException {
		StringBuffer sb = new StringBuffer();
		sb.append("<iframe src=\"");
		sb.append(getApiUrl());
		sb.append("\" scrolling=\"auto\" frameborder=\"1\" width=\"");
		sb.append(width);
		sb.append("\" height=\"");
		sb.append(height);
		sb.append("\"><p>Your browser does not support iframes.</p></iframe>");
		return sb.toString();
	}
}
