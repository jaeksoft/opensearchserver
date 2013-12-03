/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012-2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.parser.htmlParser;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang3.StringEscapeUtils;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.streamlimiter.LimitException;
import com.jaeksoft.searchlib.streamlimiter.StreamLimiter;
import com.jaeksoft.searchlib.util.LinkUtils;
import com.jaeksoft.searchlib.util.MimeUtils;

public abstract class HtmlDocumentProvider {

	public static interface XPath {
		public abstract void xPath(String xPath, Collection<Object> nodes)
				throws XPathExpressionException;
	}

	private final HtmlParserEnum parserEnum;

	private String titleCache;

	private List<HtmlNodeAbstract<?>> metasCache;

	private HtmlNodeAbstract<?> rootNode;

	private int score;

	protected HtmlDocumentProvider(HtmlParserEnum parserEnum) {
		this.parserEnum = parserEnum;
		titleCache = null;
		metasCache = null;
		score = 0;
		rootNode = null;
	}

	public void init(String charset, StreamLimiter streamLimiter)
			throws LimitException {
		try {
			rootNode = getDocument(charset, streamLimiter);
		} catch (LimitException e) {
			throw e;
		} catch (Exception e) {
			Logging.warn(e);
		}
	}

	public void init(String htmlSource) throws IOException,
			ParserConfigurationException, SAXException {
		rootNode = getDocument(htmlSource);
	}

	public HtmlNodeAbstract<?> getRootNode() {
		return rootNode;
	}

	public final String getName() {
		return parserEnum.getLabel();
	}

	protected abstract HtmlNodeAbstract<?> getDocument(String charset,
			InputStream inputStream) throws SAXException, IOException,
			ParserConfigurationException;

	protected HtmlNodeAbstract<?> getDocument(String charset,
			StreamLimiter streamLimiter) throws SAXException, IOException,
			ParserConfigurationException, SearchLibException {
		return getDocument(charset, streamLimiter.getNewInputStream());
	}

	protected abstract HtmlNodeAbstract<?> getDocument(String htmlSource)
			throws IOException, ParserConfigurationException, SAXException;

	public void score() {
		score = getTitle() != null ? 10000 : 0;
		score += getMetas() != null ? metasCache.size() * 1000 : 0;
		score += rootNode != null ? rootNode.countElements() : 0;
	}

	final public String getTitle() {
		if (titleCache != null)
			return titleCache;
		if (rootNode == null)
			return null;
		String[] p1 = { "html", "head", "title" };
		String title = rootNode.getFirstTextNode(p1);
		if (title == null) {
			String[] p2 = { "html", "title" };
			title = rootNode.getFirstTextNode(p2);
		}
		if (title == null)
			return null;
		titleCache = StringEscapeUtils.unescapeHtml4(title);
		return titleCache;
	}

	final public URL getCanonicalLink(URL currentUrl) {
		if (rootNode == null)
			return null;
		String[] p1 = { "html", "head", "link" };
		List<HtmlNodeAbstract<?>> nodes = rootNode.getNodes(p1);
		if (nodes == null)
			return null;
		for (HtmlNodeAbstract<?> node : nodes) {
			String rel = node.getAttribute("rel");
			if (rel == null)
				continue;
			if (!"canonical".equalsIgnoreCase(rel))
				continue;
			String href = node.getAttribute("href");
			if (href == null)
				return null;
			return LinkUtils.getLink(currentUrl, href, null, false);
		}
		return null;
	}

	final public List<HtmlNodeAbstract<?>> getMetas() {
		if (metasCache != null)
			return metasCache;
		if (rootNode == null)
			return null;
		final String[] p1 = { "html", "head", "meta" };
		final String[] p2 = { "html", "meta" };
		metasCache = rootNode.getNewNodeList();
		rootNode.getNodes(metasCache, p1);
		rootNode.getNodes(metasCache, p2);
		return metasCache;
	}

	final public static String getMetaContent(final HtmlNodeAbstract<?> node) {
		String content = node.getAttributeText("content");
		if (content == null)
			return null;
		return StringEscapeUtils.unescapeHtml4(content);
	}

	final public String getMetaHttpEquiv(String name) {
		getMetas();
		if (metasCache == null)
			return null;
		for (HtmlNodeAbstract<?> node : metasCache) {
			String attr_http_equiv = node.getAttributeText("http-equiv");
			if (name.equalsIgnoreCase(attr_http_equiv))
				return getMetaContent(node);
		}
		return null;
	}

	final public String getMetaCharset() {
		String contentType = getMetaHttpEquiv("content-type");
		if (contentType == null)
			return null;
		return MimeUtils.extractContentTypeCharset(contentType);
	}

	final public URL getBaseHref() {
		List<HtmlNodeAbstract<?>> list = rootNode.getNodes("html", "head",
				"base");
		if (list == null)
			return null;
		if (list.size() == 0)
			return null;
		HtmlNodeAbstract<?> node = list.get(0);
		if (node == null)
			return null;
		String url = node.getAttributeText("href");
		if (url == null)
			return null;
		try {
			return LinkUtils.newEncodedURL(url);
		} catch (MalformedURLException e) {
			Logging.warn(e);
			return null;
		} catch (URISyntaxException e) {
			Logging.warn(e);
			return null;
		}
	}

	final public static HtmlDocumentProvider bestScore(
			List<HtmlDocumentProvider> providers) {
		HtmlDocumentProvider bestProvider = null;
		for (HtmlDocumentProvider provider : providers) {
			provider.score();
			if (bestProvider == null)
				bestProvider = provider;
			else if (provider.score > bestProvider.score)
				bestProvider = provider;
		}
		return bestProvider;
	}

	public abstract boolean isXPathSupported();

	public void xPath(String xPath, Collection<Object> nodes)
			throws XPathExpressionException {
		((XPath) rootNode).xPath(xPath, nodes);
	}

}
