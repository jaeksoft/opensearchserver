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

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.jaeksoft.searchlib.web.RendererServlet;

public class Renderer implements Comparable<Renderer> {

	private final static String RENDERER_ITEM_ROOTNODE_NAME = "renderer";
	private final static String RENDERER_ITEM_ROOT_ATTR_NAME = "name";
	private final static String RENDERER_ITEM_ROOT_ATTR_REQUEST = "request";
	private final static String RENDERER_ITEM_ROOT_ATTR_SEARCHBUTTONLABEL = "searchButtonLabel";
	private final static String RENDERER_ITEM_ROOT_ATTR_NORESULTFOUNDTEXT = "noResultFoundText";
	private final static String RENDERER_ITEM_ROOT_ATTR_ONERESULTFOUNDTEXT = "oneResultFoundText";
	private final static String RENDERER_ITEM_ROOT_ATTR_RESULTSFOUNDTEXT = "resultsFoundText";
	private final static String RENDERER_ITEM_NODE_NAME_FIELD = "field";
	private final static String RENDERER_ITEM_NODE_COMMON_STYLE = "style";
	private final static String RENDERER_ITEM_NODE_INPUT_STYLE = "inputStyle";
	private final static String RENDERER_ITEM_NODE_BUTTON_STYLE = "buttonStyle";
	private final static String RENDERER_ITEM_NODE_AUTOCOMPLETE_STYLE = "autocompleteStyle";
	private final static String RENDERER_ITEM_NODE_AUTOCOMPLETELIST_STYLE = "autocompleteListStyle";
	private final static String RENDERER_ITEM_NODE_AUTOCOMPLETELINK_STYLE = "autocompleteLinkStyle";
	private final static String RENDERER_ITEM_NODE_AUTOCOMPLETELINKHOVER_STYLE = "autocompleteLinkHoverStyle";
	private final static String RENDERER_ITEM_NODE_DOCUMENTFOUND_STYLE = "documentFoundStyle";
	private final static String RENDERER_ITEM_NODE_PAGING_STYLE = "ossPagingStyle";
	private final static String RENDERER_ITEM_NODE_CURRENTPAGE_STYLE = "ossCurrentPageStyle";
	private final static String RENDERER_ITEM_NODE_ALINK = "alink";
	private final static String RENDERER_ITEM_NODE_AVISITED = "avisited";
	private final static String RENDERER_ITEM_NODE_AACTIVE = "aactive";
	private final static String RENDERER_ITEM_NODE_AHOVER = "ahover";
	private final static String RENDERER_ITEM_NODE_FACET_STYLE = "facetStyle";
	private final static String RENDERER_ITEM_NODE_RESULT_STYLE = "resultStyle";
	private final static String RENDERER_ITEM_NODE_HEADER = "header";
	private final static String RENDERER_ITEM_NODE_FOOTER = "footer";

	private final ReadWriteLock rwl = new ReadWriteLock();

	private String name;

	private String requestName;

	private String commonStyle;

	private String inputStyle;

	private String buttonStyle;

	private String documentFoundStyle;

	private String pagingStyle;

	private String currentPageStyle;

	private String autocompleteStyle;

	private String autocompleteListStyle;

	private String autocompleteLinkStyle;

	private String autocompleteLinkHoverStyle;

	private String searchButtonLabel;

	private String resultsFoundText;

	private String oneResultFoundText;

	private String noResultFoundText;

	private String alink;

	private String avisited;

	private String aactive;

	private String ahover;

	private String facetStyle;

	private String resultStyle;

	private List<RendererField> fields;

	private volatile String cssCache;

	private String footer;

	private String header;

	public Renderer() {
		name = null;
		requestName = null;
		commonStyle = null;
		inputStyle = null;
		buttonStyle = null;
		documentFoundStyle = null;
		currentPageStyle = null;
		pagingStyle = null;
		autocompleteStyle = null;
		autocompleteListStyle = null;
		autocompleteLinkStyle = null;
		autocompleteLinkHoverStyle = null;
		searchButtonLabel = "Search";
		oneResultFoundText = "1 result found";
		resultsFoundText = "results found";
		noResultFoundText = "No results found";
		cssCache = null;
		fields = new ArrayList<RendererField>();
		footer = null;
		header = null;
	}

	public Renderer(XPathParser xpp) throws ParserConfigurationException,
			SAXException, IOException, XPathExpressionException {
		this();
		Node rootNode = xpp.getNode(RENDERER_ITEM_ROOTNODE_NAME);
		if (rootNode == null)
			return;
		setName(XPathParser.getAttributeString(rootNode,
				RENDERER_ITEM_ROOT_ATTR_NAME));
		setRequestName(XPathParser.getAttributeString(rootNode,
				RENDERER_ITEM_ROOT_ATTR_REQUEST));
		setSearchButtonLabel(XPathParser.getAttributeString(rootNode,
				RENDERER_ITEM_ROOT_ATTR_SEARCHBUTTONLABEL));
		setNoResultFoundText(XPathParser.getAttributeString(rootNode,
				RENDERER_ITEM_ROOT_ATTR_NORESULTFOUNDTEXT));
		setOneResultFoundText(XPathParser.getAttributeString(rootNode,
				RENDERER_ITEM_ROOT_ATTR_ONERESULTFOUNDTEXT));
		setResultsFoundText(XPathParser.getAttributeString(rootNode,
				RENDERER_ITEM_ROOT_ATTR_RESULTSFOUNDTEXT));
		setCommonStyle(xpp.getSubNodeTextIfAny(rootNode,
				RENDERER_ITEM_NODE_COMMON_STYLE, true));
		setInputStyle(xpp.getSubNodeTextIfAny(rootNode,
				RENDERER_ITEM_NODE_INPUT_STYLE, true));
		setButtonStyle(xpp.getSubNodeTextIfAny(rootNode,
				RENDERER_ITEM_NODE_BUTTON_STYLE, true));
		setDocumentFoundStyle(xpp.getSubNodeTextIfAny(rootNode,
				RENDERER_ITEM_NODE_DOCUMENTFOUND_STYLE, true));
		setCurrentPageStyle(xpp.getSubNodeTextIfAny(rootNode,
				RENDERER_ITEM_NODE_CURRENTPAGE_STYLE, true));
		setPagingStyle(xpp.getSubNodeTextIfAny(rootNode,
				RENDERER_ITEM_NODE_PAGING_STYLE, true));
		setDocumentFoundStyle(xpp.getSubNodeTextIfAny(rootNode,
				RENDERER_ITEM_NODE_DOCUMENTFOUND_STYLE, true));
		setAutocompleteStyle(xpp.getSubNodeTextIfAny(rootNode,
				RENDERER_ITEM_NODE_AUTOCOMPLETE_STYLE, true));
		setAutocompleteListStyle(xpp.getSubNodeTextIfAny(rootNode,
				RENDERER_ITEM_NODE_AUTOCOMPLETELIST_STYLE, true));
		setAutocompleteLinkStyle(xpp.getSubNodeTextIfAny(rootNode,
				RENDERER_ITEM_NODE_AUTOCOMPLETELINK_STYLE, true));
		setAutocompleteLinkHoverStyle(xpp.getSubNodeTextIfAny(rootNode,
				RENDERER_ITEM_NODE_AUTOCOMPLETELINKHOVER_STYLE, true));
		setAactive(xpp.getSubNodeTextIfAny(rootNode,
				RENDERER_ITEM_NODE_AACTIVE, true));
		setAhover(xpp.getSubNodeTextIfAny(rootNode, RENDERER_ITEM_NODE_AHOVER,
				true));
		setAlink(xpp.getSubNodeTextIfAny(rootNode, RENDERER_ITEM_NODE_ALINK,
				true));
		setAvisited(xpp.getSubNodeTextIfAny(rootNode,
				RENDERER_ITEM_NODE_AVISITED, true));
		setFacetStyle(xpp.getSubNodeTextIfAny(rootNode,
				RENDERER_ITEM_NODE_FACET_STYLE, true));
		setResultStyle(xpp.getSubNodeTextIfAny(rootNode,
				RENDERER_ITEM_NODE_RESULT_STYLE, true));
		setHeader(xpp.getSubNodeTextIfAny(rootNode, RENDERER_ITEM_NODE_HEADER,
				true));
		setFooter(xpp.getSubNodeTextIfAny(rootNode, RENDERER_ITEM_NODE_FOOTER,
				true));
		NodeList nodeList = xpp.getNodeList(rootNode,
				RENDERER_ITEM_NODE_NAME_FIELD);
		for (int i = 0; i < nodeList.getLength(); i++)
			addField(new RendererField(xpp, nodeList.item(i)));
	}

	public Renderer(Renderer source) {
		this();
		source.copyTo(this);
	}

	public void setDefaultCss() throws SearchLibException {
		InputStream is = getClass()
				.getResourceAsStream(
						"/com/jaeksoft/searchlib/template/common/renderers/default.xml");
		try {
			Renderer r = new Renderer(new XPathParser(is));
			rwl.w.lock();
			try {
				commonStyle = r.commonStyle;
				inputStyle = r.inputStyle;
				buttonStyle = r.buttonStyle;
				documentFoundStyle = r.documentFoundStyle;
				currentPageStyle = r.currentPageStyle;
				pagingStyle = r.pagingStyle;
				autocompleteStyle = r.autocompleteStyle;
				autocompleteListStyle = r.autocompleteListStyle;
				autocompleteLinkStyle = r.autocompleteLinkStyle;
				autocompleteLinkHoverStyle = r.autocompleteLinkHoverStyle;
				aactive = r.aactive;
				ahover = r.ahover;
				alink = r.alink;
				avisited = r.avisited;
				facetStyle = r.facetStyle;
				resultStyle = r.resultStyle;
				this.cssCache = null;
			} finally {
				rwl.w.unlock();
			}
		} catch (XPathExpressionException e) {
			throw new SearchLibException(e);
		} catch (ParserConfigurationException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

	private void writeCss(PrintWriter pw, String name, String content) {
		pw.print(name);
		pw.print(" { ");
		if (content != null)
			pw.println(content);
		pw.println(" }");
	}

	public String getFullCSS() {
		rwl.r.lock();
		try {
			if (cssCache != null)
				return cssCache;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			StringWriter sw = null;
			PrintWriter pw = null;
			if (cssCache != null)
				return cssCache;
			try {
				sw = new StringWriter();
				pw = new PrintWriter(sw);
				writeCss(pw, ".osscmnrdr", commonStyle);
				writeCss(pw, ".ossinputrdr", inputStyle);
				writeCss(pw, ".ossbuttonrdr", buttonStyle);
				writeCss(pw, "a:link", alink);
				writeCss(pw, "a:hover", ahover);
				writeCss(pw, "a:visited", avisited);
				writeCss(pw, "a:active", aactive);
				writeCss(pw, "#ossautocomplete", autocompleteStyle);
				writeCss(pw, "#ossautocompletelist", autocompleteListStyle);
				writeCss(pw, ".ossautocomplete_link", autocompleteLinkStyle);
				writeCss(pw, ".ossautocomplete_link_over",
						autocompleteLinkHoverStyle);
				writeCss(pw, ".ossnumfound", documentFoundStyle);
				writeCss(pw, ".oss-paging", pagingStyle);
				writeCss(pw, ".oss-currentpage", currentPageStyle);
				writeCss(pw, ".oss-facet", facetStyle);
				writeCss(pw, ".oss-result", resultStyle);

				int i = 0;
				for (RendererField rendererField : fields) {
					i++;
					writeCss(pw, ".ossfieldrdr" + i, rendererField.getStyle());
				}
				cssCache = sw.toString();
				return cssCache;
			} finally {
				if (pw != null)
					IOUtils.closeQuietly(pw);
				if (sw != null)
					IOUtils.closeQuietly(sw);
			}
		} finally {
			rwl.w.unlock();
		}
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
				target.currentPageStyle = currentPageStyle;
				target.pagingStyle = pagingStyle;
				target.autocompleteStyle = autocompleteStyle;
				target.autocompleteListStyle = autocompleteListStyle;
				target.autocompleteLinkStyle = autocompleteLinkStyle;
				target.autocompleteLinkHoverStyle = autocompleteLinkHoverStyle;
				target.searchButtonLabel = searchButtonLabel;
				target.noResultFoundText = noResultFoundText;
				target.oneResultFoundText = oneResultFoundText;
				target.resultsFoundText = resultsFoundText;
				target.aactive = aactive;
				target.ahover = ahover;
				target.alink = alink;
				target.avisited = avisited;
				target.facetStyle = facetStyle;
				target.resultStyle = resultStyle;
				target.fields.clear();
				target.header = header;
				target.footer = footer;
				target.cssCache = null;
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
			this.cssCache = null;
		} finally {
			rwl.w.unlock();
		}
	}

	public void removeField(RendererField field) {
		rwl.w.lock();
		try {
			fields.remove(field);
			this.cssCache = null;
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
			this.cssCache = null;
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
			this.cssCache = null;
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
		rwl.w.lock();
		try {
			this.commonStyle = commonStyle;
			this.cssCache = null;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the commonStyle
	 */
	public String getCommonStyle() {
		rwl.r.lock();
		try {
			return commonStyle;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @return the buttonStyle
	 */
	public String getButtonStyle() {
		rwl.r.lock();
		try {
			return buttonStyle;
		} finally {
			rwl.r.unlock();
		}
	}

	public final String getAlink() {
		rwl.r.lock();
		try {
			return alink;
		} finally {
			rwl.r.unlock();
		}
	}

	public final void setAlink(String alink) {
		rwl.w.lock();
		try {
			this.alink = alink;
			this.cssCache = null;
		} finally {
			rwl.w.unlock();
		}
	}

	public final String getAvisited() {
		rwl.r.lock();
		try {
			return avisited;
		} finally {
			rwl.r.unlock();
		}
	}

	public final void setAvisited(String avisited) {
		rwl.w.lock();
		try {
			this.avisited = avisited;
			this.cssCache = null;
		} finally {
			rwl.w.unlock();
		}
	}

	public final String getAactive() {
		rwl.r.lock();
		try {
			return aactive;
		} finally {
			rwl.r.unlock();
		}
	}

	public final void setAactive(String aactive) {
		rwl.w.lock();
		try {
			this.aactive = aactive;
			this.cssCache = null;
		} finally {
			rwl.w.unlock();
		}
	}

	public final String getAhover() {
		rwl.r.lock();
		try {
			return ahover;
		} finally {
			rwl.r.unlock();
		}
	}

	public final void setAhover(String ahover) {
		rwl.w.lock();
		try {
			this.ahover = ahover;
			this.cssCache = null;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @param buttonStyle
	 *            the buttonStyle to set
	 */
	public void setButtonStyle(String buttonStyle) {
		rwl.w.lock();
		try {
			this.buttonStyle = buttonStyle;
			this.cssCache = null;
		} finally {
			rwl.w.unlock();
		}
	}

	public String getDocumentFoundStyle() {
		rwl.r.lock();
		try {
			return documentFoundStyle;
		} finally {
			rwl.r.unlock();
		}
	}

	public void setDocumentFoundStyle(String documentFoundStyle) {
		rwl.w.lock();
		try {
			this.documentFoundStyle = documentFoundStyle;
			this.cssCache = null;
		} finally {
			rwl.w.unlock();
		}
	}

	public String getPagingStyle() {
		rwl.r.lock();
		try {
			return pagingStyle;
		} finally {
			rwl.r.unlock();
		}
	}

	public void setPagingStyle(String pagingStyle) {
		rwl.w.lock();
		try {
			this.pagingStyle = pagingStyle;
		} finally {
			rwl.w.unlock();
		}
	}

	public String getCurrentPageStyle() {
		rwl.r.lock();
		try {
			return currentPageStyle;
		} finally {
			rwl.r.unlock();
		}
	}

	public void setCurrentPageStyle(String currentPageStyle) {
		rwl.w.lock();
		try {
			this.currentPageStyle = currentPageStyle;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the autocompleteStyle
	 */
	public String getAutocompleteStyle() {
		rwl.r.lock();
		try {
			return autocompleteStyle;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param autocompleteStyle
	 *            the autocompleteStyle to set
	 */
	public void setAutocompleteStyle(String autocompleteStyle) {
		rwl.w.lock();
		try {
			this.autocompleteStyle = autocompleteStyle;
			this.cssCache = null;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the autocompleteListStyle
	 */
	public String getAutocompleteListStyle() {
		rwl.r.lock();
		try {
			return autocompleteListStyle;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param autocompleteListStyle
	 *            the autocompleteListStyle to set
	 */
	public void setAutocompleteListStyle(String autocompleteListStyle) {
		rwl.w.lock();
		try {
			this.autocompleteListStyle = autocompleteListStyle;
			this.cssCache = null;
		} finally {
			rwl.w.unlock();
		}
	}

	public String getAutocompleteLinkStyle() {
		rwl.r.lock();
		try {
			return autocompleteLinkStyle;
		} finally {
			rwl.r.unlock();
		}
	}

	public void setAutocompleteLinkStyle(String autocompleteLinkStyle) {
		rwl.w.lock();
		try {
			this.autocompleteLinkStyle = autocompleteLinkStyle;
			this.cssCache = null;
		} finally {
			rwl.w.unlock();
		}
	}

	public String getAutocompleteLinkHoverStyle() {
		rwl.r.lock();
		try {
			return autocompleteLinkHoverStyle;
		} finally {
			rwl.r.unlock();
		}
	}

	public void setAutocompleteLinkHoverStyle(String autocompleteLinkHoverStyle) {
		rwl.w.lock();
		try {
			this.autocompleteLinkHoverStyle = autocompleteLinkHoverStyle;
			this.cssCache = null;
		} finally {
			rwl.w.unlock();
		}
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
		rwl.w.lock();
		try {
			this.inputStyle = inputStyle;
			this.cssCache = null;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the inputStyle
	 */
	public String getInputStyle() {
		rwl.r.lock();
		try {
			return inputStyle;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param searchButtonLabel
	 *            the searchButtonLabel to set
	 */
	public void setSearchButtonLabel(String searchButtonLabel) {
		rwl.w.lock();
		try {
			this.searchButtonLabel = searchButtonLabel;
			this.cssCache = null;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the searchButtonLabel
	 */
	public String getSearchButtonLabel() {
		rwl.r.lock();
		try {
			return searchButtonLabel;
		} finally {
			rwl.r.unlock();
		}
	}

	public String getFacetStyle() {
		rwl.r.lock();
		try {
			return facetStyle;
		} finally {
			rwl.r.unlock();
		}
	}

	public String getResultStyle() {
		rwl.r.lock();
		try {
			return resultStyle;
		} finally {
			rwl.r.unlock();
		}
	}

	public void setResultStyle(String resultStyle) {
		rwl.w.lock();
		try {
			this.resultStyle = resultStyle;
			this.cssCache = null;
		} finally {
			rwl.w.unlock();
		}
	}

	public void setFacetStyle(String facetStyle) {
		rwl.w.lock();
		try {
			this.facetStyle = facetStyle;
			this.cssCache = null;
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	public final int compareTo(Renderer o) {
		rwl.r.lock();
		try {
			return name.compareTo(o.name);
		} finally {
			rwl.r.unlock();
		}
	}

	public void writeXml(XmlWriter xmlWriter) throws SAXException {
		rwl.r.lock();
		try {
			xmlWriter.startElement(RENDERER_ITEM_ROOTNODE_NAME,
					RENDERER_ITEM_ROOT_ATTR_NAME, name,
					RENDERER_ITEM_ROOT_ATTR_REQUEST, requestName,
					RENDERER_ITEM_ROOT_ATTR_SEARCHBUTTONLABEL,
					searchButtonLabel,
					RENDERER_ITEM_ROOT_ATTR_NORESULTFOUNDTEXT,
					noResultFoundText,
					RENDERER_ITEM_ROOT_ATTR_ONERESULTFOUNDTEXT,
					oneResultFoundText,
					RENDERER_ITEM_ROOT_ATTR_RESULTSFOUNDTEXT, resultsFoundText);
			xmlWriter.writeSubTextNodeIfAny(RENDERER_ITEM_NODE_COMMON_STYLE,
					commonStyle);
			xmlWriter.writeSubTextNodeIfAny(RENDERER_ITEM_NODE_INPUT_STYLE,
					inputStyle);
			xmlWriter.writeSubTextNodeIfAny(RENDERER_ITEM_NODE_BUTTON_STYLE,
					buttonStyle);
			xmlWriter.writeSubTextNodeIfAny(
					RENDERER_ITEM_NODE_DOCUMENTFOUND_STYLE, documentFoundStyle);
			xmlWriter.writeSubTextNodeIfAny(
					RENDERER_ITEM_NODE_CURRENTPAGE_STYLE, currentPageStyle);
			xmlWriter.writeSubTextNodeIfAny(RENDERER_ITEM_NODE_PAGING_STYLE,
					pagingStyle);
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
			xmlWriter.writeSubTextNodeIfAny(RENDERER_ITEM_NODE_FACET_STYLE,
					facetStyle);
			xmlWriter.writeSubTextNodeIfAny(RENDERER_ITEM_NODE_RESULT_STYLE,
					resultStyle);
			xmlWriter.writeSubTextNodeIfAny(RENDERER_ITEM_NODE_HEADER, header);
			xmlWriter.writeSubTextNodeIfAny(RENDERER_ITEM_NODE_FOOTER, footer);
			for (RendererField field : fields)
				field.writeXml(xmlWriter, RENDERER_ITEM_NODE_NAME_FIELD);
			xmlWriter.endElement();
			this.cssCache = null;
		} finally {
			rwl.r.unlock();
		}
	}

	public String getApiUrl() throws UnsupportedEncodingException {
		rwl.r.lock();
		try {
			return RendererServlet.doRenderer(name, null);
		} finally {
			rwl.r.unlock();
		}
	}

	public String getIFrameHtmlCode(String width, String height)
			throws UnsupportedEncodingException {
		rwl.r.lock();
		try {
			StringBuffer sb = new StringBuffer();
			sb.append("<iframe src=\"");
			sb.append(getApiUrl());
			sb.append("\" scrolling=\"auto\" frameborder=\"1\" width=\"");
			sb.append(width);
			sb.append("\" height=\"");
			sb.append(height);
			sb.append("\"><p>Your browser does not support iframes.</p></iframe>");
			return sb.toString();
		} finally {
			rwl.r.unlock();
		}
	}

	public String getResultsFoundText() {
		rwl.r.lock();
		try {
			return resultsFoundText;
		} finally {
			rwl.r.unlock();
		}
	}

	public void setResultsFoundText(String resultsFoundText) {
		rwl.w.lock();
		try {
			this.resultsFoundText = resultsFoundText;
		} finally {
			rwl.w.unlock();
		}
	}

	public String getOneResultFoundText() {
		rwl.r.lock();
		try {
			return oneResultFoundText;
		} finally {
			rwl.r.unlock();
		}
	}

	public void setOneResultFoundText(String oneResultFoundText) {
		rwl.w.lock();
		try {
			this.oneResultFoundText = oneResultFoundText;
		} finally {
			rwl.w.unlock();
		}
	}

	public String getNoResultFoundText() {
		rwl.r.lock();
		try {
			return noResultFoundText;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * 
	 * @param noResultFoundText
	 */
	public void setNoResultFoundText(String noResultFoundText) {
		rwl.w.lock();
		try {
			this.noResultFoundText = noResultFoundText;
		} finally {
			rwl.w.unlock();
		}
	}

	final public String getResultFoundText(int resultsCount) {
		rwl.r.lock();
		try {
			switch (resultsCount) {
			case 0:
				return noResultFoundText;
			case 1:
				return oneResultFoundText;
			default:
				StringBuffer sb = new StringBuffer();
				sb.append(resultsCount);
				sb.append(' ');
				sb.append(resultsFoundText);
				return sb.toString();
			}
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @return the footer
	 */
	public String getFooter() {
		rwl.r.lock();
		try {
			return footer == null ? "" : footer;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param footer
	 *            the footer to set
	 */
	public void setFooter(String footer) {
		rwl.w.lock();
		try {
			this.footer = footer;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the header
	 */
	public String getHeader() {
		rwl.r.lock();
		try {
			return header == null ? "" : header;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param header
	 *            the header to set
	 */
	public void setHeader(String header) {
		rwl.w.lock();
		try {
			this.header = header;
		} finally {
			rwl.w.unlock();
		}
	}
}
