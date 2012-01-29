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

package com.jaeksoft.searchlib.parser.htmlParser;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringEscapeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.parser.LimitException;
import com.jaeksoft.searchlib.parser.LimitInputStream;
import com.jaeksoft.searchlib.util.DomUtils;

public abstract class HtmlDocumentProvider {

	private String titleCache;

	private List<Node> metasCache;

	private Document document;

	private int score;

	public HtmlDocumentProvider(String charset, LimitInputStream inputStream)
			throws LimitException {
		titleCache = null;
		metasCache = null;
		score = 0;
		document = null;
		try {
			inputStream.restartFromCache();
			document = getDocument(charset, inputStream);
		} catch (LimitException e) {
			throw e;
		} catch (Exception e) {
			Logging.warn(e.getMessage(), e);
		}
	}

	public Document getDocument() {
		return document;
	}

	public abstract String getName();

	protected abstract Document getDocument(String charset,
			LimitInputStream inputStream) throws SAXException, IOException,
			ParserConfigurationException;

	public void score() {
		score = getTitle() != null ? 10000 : 0;
		score += getMetas() != null ? metasCache.size() * 1000 : 0;
		score += DomUtils.countElements(document);
	}

	final public String getTitle() {
		if (titleCache != null)
			return titleCache;
		String[] p1 = { "html", "head", "title" };
		String title = DomUtils.getTextNode(document, p1);
		if (title != null)
			return title;
		String[] p2 = { "html", "title" };
		titleCache = DomUtils.getTextNode(document, p2);
		return titleCache;
	}

	final public List<Node> getMetas() {
		if (metasCache != null)
			return metasCache;
		String[] p1 = { "html", "head", "meta" };
		String[] p2 = { "html", "meta" };
		metasCache = new ArrayList<Node>();
		DomUtils.getNodes(metasCache, document, p1);
		DomUtils.getNodes(metasCache, document, p2);
		return metasCache;
	}

	final public static String getMetaContent(Node node) {
		String content = DomUtils.getAttributeText(node, "content");
		if (content == null)
			return null;
		return StringEscapeUtils.unescapeHtml(content);
	}

	final public String getMetaCharset() {
		List<Node> metas = getMetas();
		if (metas == null)
			return null;
		for (Node node : metas) {
			String charset = DomUtils.getAttributeText(node, "charset");
			if (charset != null && charset.length() > 0)
				return charset;
		}
		return null;
	}

	final public String getMetaHttpEquiv(String name) {
		List<Node> metas = getMetas();
		if (metas == null)
			return null;
		for (Node node : metas) {
			String attr_http_equiv = DomUtils.getAttributeText(node,
					"http-equiv");
			if (name.equalsIgnoreCase(attr_http_equiv))
				return getMetaContent(node);
		}
		return null;
	}

	final public URL getBaseHref() {
		List<Node> list = DomUtils.getNodes(document, "html", "head", "base");
		if (list == null)
			return null;
		if (list.size() == 0)
			return null;
		Node node = list.get(0);
		if (node == null)
			return null;
		String url = DomUtils.getAttributeText(node, "href");
		if (url == null)
			return null;
		try {
			return new URL(url);
		} catch (MalformedURLException e) {
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
}
