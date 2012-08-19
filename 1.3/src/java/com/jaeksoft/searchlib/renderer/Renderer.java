/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2012 Emmanuel Keller / Jaeksoft
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
	private final static String RENDERER_ITEM_ROOT_ATTR_SEARCHBUTTONLABEL = "searchButtonLabel";
	private final static String RENDERER_ITEM_NODE_NAME_FIELD = "field";
	private final static String RENDERER_ITEM_NODE_COMMON_STYLE = "style";
	private final static String RENDERER_ITEM_NODE_INPUT_STYLE = "inputStyle";
	private final static String RENDERER_ITEM_NODE_BUTTON_STYLE = "buttonStyle";
	private final static String RENDERER_ITEM_NODE_AUTOCOMPLETE_STYLE = "autocompleteStyle";
	private final static String RENDERER_ITEM_NODE_AUTOCOMPLETELIST_STYLE = "autocompleteListStyle";
	private final static String RENDERER_ITEM_NODE_AUTOCOMPLETELINK_STYLE = "autocompleteLinkStyle";
	private final static String RENDERER_ITEM_NODE_AUTOCOMPLETELINKHOVER_STYLE = "autocompleteLinkHoverStyle";
	private final static String RENDERER_ITEM_NODE_DOCUMENTFOUND_STYLE = "documentFoundStyle";
	private final static String RENDERER_ITEM_NODE_ALINK = "alink";
	private final static String RENDERER_ITEM_NODE_AVISITED = "avisited";
	private final static String RENDERER_ITEM_NODE_AACTIVE = "aactive";
	private final static String RENDERER_ITEM_NODE_AHOVER = "ahover";

	private final ReadWriteLock rwl = new ReadWriteLock();

	private String name;

	private String requestName;

	private String commonStyle;

	private String inputStyle;

	private String buttonStyle;

	private String documentFoundStyle;

	private String autocompleteStyle;

	private String autocompleteListStyle;

	private String autocompleteLinkStyle;

	private String autocompleteLinkHoverStyle;

	private String searchButtonLabel;

	private String alink;

	private String avisited;

	private String aactive;

	private String ahover;

	private List<RendererField> fields;

	public Renderer() {
		name = null;
		requestName = null;
		commonStyle = null;
		inputStyle = null;
		buttonStyle = null;
		documentFoundStyle = null;
		autocompleteStyle = null;
		autocompleteListStyle = null;
		autocompleteLinkStyle = null;
		autocompleteLinkHoverStyle = null;
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
		setSearchButtonLabel(XPathParser.getAttributeString(rootNode,
				RENDERER_ITEM_ROOT_ATTR_SEARCHBUTTONLABEL));
		setCommonStyle(xpp.getSubNodeTextIfAny(rootNode,
				RENDERER_ITEM_NODE_COMMON_STYLE));
		setInputStyle(xpp.getSubNodeTextIfAny(rootNode,
				RENDERER_ITEM_NODE_INPUT_STYLE));
		setButtonStyle(xpp.getSubNodeTextIfAny(rootNode,
				RENDERER_ITEM_NODE_BUTTON_STYLE));
		setDocumentFoundStyle(xpp.getSubNodeTextIfAny(rootNode,
				RENDERER_ITEM_NODE_DOCUMENTFOUND_STYLE));
		setAutocompleteStyle(xpp.getSubNodeTextIfAny(rootNode,
				RENDERER_ITEM_NODE_AUTOCOMPLETE_STYLE));
		setAutocompleteListStyle(xpp.getSubNodeTextIfAny(rootNode,
				RENDERER_ITEM_NODE_AUTOCOMPLETELIST_STYLE));
		setAutocompleteLinkStyle(xpp.getSubNodeTextIfAny(rootNode,
				RENDERER_ITEM_NODE_AUTOCOMPLETELINK_STYLE));
		setAutocompleteLinkHoverStyle(xpp.getSubNodeTextIfAny(rootNode,
				RENDERER_ITEM_NODE_AUTOCOMPLETELINKHOVER_STYLE));
		setAactive(xpp
				.getSubNodeTextIfAny(rootNode, RENDERER_ITEM_NODE_AACTIVE));
		setAhover(xpp.getSubNodeTextIfAny(rootNode, RENDERER_ITEM_NODE_AHOVER));
		setAlink(xpp.getSubNodeTextIfAny(rootNode, RENDERER_ITEM_NODE_ALINK));
		setAvisited(xpp.getSubNodeTextIfAny(rootNode,
				RENDERER_ITEM_NODE_AVISITED));
		NodeList nodeList = xpp.getNodeList(rootNode,
				RENDERER_ITEM_NODE_NAME_FIELD);
		for (int i = 0; i < nodeList.getLength(); i++)
			addField(new RendererField(xpp, nodeList.item(i)));
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
				target.commonStyle = commonStyle;
				target.inputStyle = inputStyle;
				target.buttonStyle = buttonStyle;
				target.documentFoundStyle = documentFoundStyle;
				target.autocompleteStyle = autocompleteStyle;
				target.autocompleteListStyle = autocompleteListStyle;
				target.autocompleteLinkStyle = autocompleteLinkStyle;
				target.autocompleteLinkHoverStyle = autocompleteLinkHoverStyle;
				target.searchButtonLabel = searchButtonLabel;
				target.aactive = aactive;
				target.ahover = ahover;
				target.alink = alink;
				target.avisited = avisited;
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
	 * @param commonStyle
	 *            the commonStyle to set
	 */
	public void setCommonStyle(String commonStyle) {
		this.commonStyle = commonStyle;
	}

	/**
	 * @return the commonStyle
	 */
	public String getCommonStyle() {
		return commonStyle;
	}

	/**
	 * @return the buttonStyle
	 */
	public String getButtonStyle() {
		return buttonStyle;
	}

	public final String getAlink() {
		return alink;
	}

	public final void setAlink(String alink) {
		this.alink = alink;
	}

	public final String getAvisited() {
		return avisited;
	}

	public final void setAvisited(String avisited) {
		this.avisited = avisited;
	}

	public final String getAactive() {
		return aactive;
	}

	public final void setAactive(String aactive) {
		this.aactive = aactive;
	}

	public final String getAhover() {
		return ahover;
	}

	public final void setAhover(String ahover) {
		this.ahover = ahover;
	}

	/**
	 * @param buttonStyle
	 *            the buttonStyle to set
	 */
	public void setButtonStyle(String buttonStyle) {
		this.buttonStyle = buttonStyle;
	}

	public String getDocumentFoundStyle() {
		return documentFoundStyle;
	}

	public void setDocumentFoundStyle(String documentFoundStyle) {
		this.documentFoundStyle = documentFoundStyle;
	}

	/**
	 * @return the autocompleteStyle
	 */
	public String getAutocompleteStyle() {
		return autocompleteStyle;
	}

	/**
	 * @param autocompleteStyle
	 *            the autocompleteStyle to set
	 */
	public void setAutocompleteStyle(String autocompleteStyle) {
		this.autocompleteStyle = autocompleteStyle;
	}

	/**
	 * @return the autocompleteListStyle
	 */
	public String getAutocompleteListStyle() {
		return autocompleteListStyle;
	}

	/**
	 * @param autocompleteListStyle
	 *            the autocompleteListStyle to set
	 */
	public void setAutocompleteListStyle(String autocompleteListStyle) {
		this.autocompleteListStyle = autocompleteListStyle;
	}

	public String getAutocompleteLinkStyle() {
		return autocompleteLinkStyle;
	}

	public void setAutocompleteLinkStyle(String autocompleteLinkStyle) {
		this.autocompleteLinkStyle = autocompleteLinkStyle;
	}

	public String getAutocompleteLinkHoverStyle() {
		return autocompleteLinkHoverStyle;
	}

	public void setAutocompleteLinkHoverStyle(String autocompleteLinkHoverStyle) {
		this.autocompleteLinkHoverStyle = autocompleteLinkHoverStyle;
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
					RENDERER_ITEM_ROOT_ATTR_SEARCHBUTTONLABEL,
					searchButtonLabel);
			xmlWriter.writeSubTextNodeIfAny(RENDERER_ITEM_NODE_COMMON_STYLE,
					commonStyle);
			xmlWriter.writeSubTextNodeIfAny(RENDERER_ITEM_NODE_INPUT_STYLE,
					inputStyle);
			xmlWriter.writeSubTextNodeIfAny(RENDERER_ITEM_NODE_BUTTON_STYLE,
					buttonStyle);
			xmlWriter.writeSubTextNodeIfAny(
					RENDERER_ITEM_NODE_DOCUMENTFOUND_STYLE, documentFoundStyle);
			xmlWriter.writeSubTextNodeIfAny(
					RENDERER_ITEM_NODE_AUTOCOMPLETE_STYLE, autocompleteStyle);
			xmlWriter.writeSubTextNodeIfAny(
					RENDERER_ITEM_NODE_AUTOCOMPLETELIST_STYLE,
					autocompleteListStyle);
			xmlWriter.writeSubTextNodeIfAny(
					RENDERER_ITEM_NODE_AUTOCOMPLETELINK_STYLE,
					autocompleteLinkStyle);
			xmlWriter.writeSubTextNodeIfAny(
					RENDERER_ITEM_NODE_AUTOCOMPLETELINKHOVER_STYLE,
					autocompleteLinkHoverStyle);
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
