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
	private final static String RENDERER_ITEM_ROOT_ATTR_FACET_WIDTH = "facetWidth";
	private final static String RENDERER_ITEM_NODE_CSS = "css";
	private final static String RENDERER_ITEM_NODE_NAME_FIELD = "field";
	private final static String RENDERER_ITEM_NODE_HEADER = "header";
	private final static String RENDERER_ITEM_NODE_FOOTER = "footer";

	private final ReadWriteLock rwl = new ReadWriteLock();

	private String name;

	private String requestName;

	private String searchButtonLabel;

	private String resultsFoundText;

	private String oneResultFoundText;

	private String noResultFoundText;

	private String facetWidth;

	private List<RendererField> fields;

	private String footer;

	private String header;

	private String css;

	public Renderer() {
		name = null;
		requestName = null;
		searchButtonLabel = "Search";
		oneResultFoundText = "1 result found";
		resultsFoundText = "results found";
		noResultFoundText = "No results found";
		facetWidth = "200px";
		fields = new ArrayList<RendererField>();
		footer = null;
		header = null;
		css = null;
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
		String p = XPathParser.getAttributeString(rootNode,
				RENDERER_ITEM_ROOT_ATTR_FACET_WIDTH);
		if (p == null || p.length() == 0)
			p = "200px";
		setFacetWidth(p);
		setHeader(xpp.getSubNodeTextIfAny(rootNode, RENDERER_ITEM_NODE_HEADER,
				true));
		setFooter(xpp.getSubNodeTextIfAny(rootNode, RENDERER_ITEM_NODE_FOOTER,
				true));
		setCss(xpp.getSubNodeTextIfAny(rootNode, RENDERER_ITEM_NODE_CSS, true));
		NodeList nodeList = xpp.getNodeList(rootNode,
				RENDERER_ITEM_NODE_NAME_FIELD);
		for (int i = 0; i < nodeList.getLength(); i++)
			addField(new RendererField(xpp, nodeList.item(i)));
		if (css == null || css.length() == 0)
			css = getOldCss(xpp, rootNode);
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
			setCss(r.css);
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
		if (content == null || content.length() == 0)
			return;
		pw.print(name);
		pw.print(" { ");
		pw.println(content);
		pw.println(" }");
	}

	/**
	 * Return the old CSS stylesheet
	 * 
	 * @return
	 * @throws XPathExpressionException
	 */
	private String getOldCss(XPathParser xpp, Node rootNode)
			throws XPathExpressionException {

		final String RENDERER_ITEM_NODE_COMMON_STYLE = "style";
		final String RENDERER_ITEM_NODE_INPUT_STYLE = "inputStyle";
		final String RENDERER_ITEM_NODE_BUTTON_STYLE = "buttonStyle";
		final String RENDERER_ITEM_NODE_AUTOCOMPLETE_STYLE = "autocompleteStyle";
		final String RENDERER_ITEM_NODE_AUTOCOMPLETELIST_STYLE = "autocompleteListStyle";
		final String RENDERER_ITEM_NODE_AUTOCOMPLETELINK_STYLE = "autocompleteLinkStyle";
		final String RENDERER_ITEM_NODE_AUTOCOMPLETELINKHOVER_STYLE = "autocompleteLinkHoverStyle";
		final String RENDERER_ITEM_NODE_DOCUMENTFOUND_STYLE = "documentFoundStyle";
		final String RENDERER_ITEM_NODE_PAGING_STYLE = "ossPagingStyle";
		final String RENDERER_ITEM_NODE_CURRENTPAGE_STYLE = "ossCurrentPageStyle";
		final String RENDERER_ITEM_NODE_ALINK = "alink";
		final String RENDERER_ITEM_NODE_AVISITED = "avisited";
		final String RENDERER_ITEM_NODE_AACTIVE = "aactive";
		final String RENDERER_ITEM_NODE_AHOVER = "ahover";
		final String RENDERER_ITEM_NODE_FACET_STYLE = "facetStyle";
		final String RENDERER_ITEM_NODE_RESULT_STYLE = "resultStyle";

		String commonStyle = xpp.getSubNodeTextIfAny(rootNode,
				RENDERER_ITEM_NODE_COMMON_STYLE, true);

		String inputStyle = xpp.getSubNodeTextIfAny(rootNode,
				RENDERER_ITEM_NODE_INPUT_STYLE, true);

		String buttonStyle = xpp.getSubNodeTextIfAny(rootNode,
				RENDERER_ITEM_NODE_BUTTON_STYLE, true);

		String documentFoundStyle = xpp.getSubNodeTextIfAny(rootNode,
				RENDERER_ITEM_NODE_DOCUMENTFOUND_STYLE, true);

		String pagingStyle = xpp.getSubNodeTextIfAny(rootNode,
				RENDERER_ITEM_NODE_PAGING_STYLE, true);

		String currentPageStyle = xpp.getSubNodeTextIfAny(rootNode,
				RENDERER_ITEM_NODE_CURRENTPAGE_STYLE, true);

		String autocompleteStyle = xpp.getSubNodeTextIfAny(rootNode,
				RENDERER_ITEM_NODE_AUTOCOMPLETE_STYLE, true);

		String autocompleteListStyle = xpp.getSubNodeTextIfAny(rootNode,
				RENDERER_ITEM_NODE_AUTOCOMPLETELIST_STYLE, true);

		String autocompleteLinkStyle = xpp.getSubNodeTextIfAny(rootNode,
				RENDERER_ITEM_NODE_AUTOCOMPLETELINK_STYLE, true);

		String autocompleteLinkHoverStyle = xpp.getSubNodeTextIfAny(rootNode,
				RENDERER_ITEM_NODE_AUTOCOMPLETELINKHOVER_STYLE, true);

		String aactive = xpp.getSubNodeTextIfAny(rootNode,
				RENDERER_ITEM_NODE_AACTIVE, true);

		String ahover = xpp.getSubNodeTextIfAny(rootNode,
				RENDERER_ITEM_NODE_AHOVER, true);

		String alink = xpp.getSubNodeTextIfAny(rootNode,
				RENDERER_ITEM_NODE_ALINK, true);

		String avisited = xpp.getSubNodeTextIfAny(rootNode,
				RENDERER_ITEM_NODE_AVISITED, true);

		String facetStyle = xpp.getSubNodeTextIfAny(rootNode,
				RENDERER_ITEM_NODE_FACET_STYLE, true);

		String resultStyle = xpp.getSubNodeTextIfAny(rootNode,
				RENDERER_ITEM_NODE_RESULT_STYLE, true);

		rwl.w.lock();
		try {
			StringWriter sw = null;
			PrintWriter pw = null;
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
					writeCss(pw, ".ossfieldrdr" + i,
							rendererField.getOldStyle());
				}
				return sw.toString();
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
				target.searchButtonLabel = searchButtonLabel;
				target.noResultFoundText = noResultFoundText;
				target.oneResultFoundText = oneResultFoundText;
				target.resultsFoundText = resultsFoundText;
				target.facetWidth = facetWidth;
				target.fields.clear();
				target.header = header;
				target.footer = footer;
				target.css = css;
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
	 * @param searchButtonLabel
	 *            the searchButtonLabel to set
	 */
	public void setSearchButtonLabel(String searchButtonLabel) {
		rwl.w.lock();
		try {
			this.searchButtonLabel = searchButtonLabel;
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
					RENDERER_ITEM_ROOT_ATTR_RESULTSFOUNDTEXT, resultsFoundText,
					RENDERER_ITEM_ROOT_ATTR_FACET_WIDTH, facetWidth);

			xmlWriter.writeSubTextNodeIfAny(RENDERER_ITEM_NODE_HEADER, header);
			xmlWriter.writeSubTextNodeIfAny(RENDERER_ITEM_NODE_FOOTER, footer);
			xmlWriter.writeSubTextNodeIfAny(RENDERER_ITEM_NODE_CSS, css);
			for (RendererField field : fields)
				field.writeXml(xmlWriter, RENDERER_ITEM_NODE_NAME_FIELD);
			xmlWriter.endElement();
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

	/**
	 * @return the facetWidth
	 */
	public String getFacetWidth() {
		rwl.r.lock();
		try {
			return facetWidth;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param facetWidth
	 *            the facetWidth to set
	 */
	public void setFacetWidth(String facetWidth) {
		rwl.w.lock();
		try {
			this.facetWidth = facetWidth;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the css
	 */
	public String getCss() {
		rwl.r.lock();
		try {
			return css;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param css
	 *            the css to set
	 */
	public void setCss(String css) {
		rwl.w.lock();
		try {
			this.css = css;
		} finally {
			rwl.w.unlock();
		}
	}
}
