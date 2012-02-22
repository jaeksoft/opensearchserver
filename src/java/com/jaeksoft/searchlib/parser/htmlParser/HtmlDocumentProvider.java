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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringEscapeUtils;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.streamlimiter.LimitException;
import com.jaeksoft.searchlib.streamlimiter.StreamLimiter;

public abstract class HtmlDocumentProvider {

	private String titleCache;

	private List<HtmlNodeAbstract<?>> metasCache;

	private HtmlNodeAbstract<?> rootNode;

	private int score;

	public HtmlDocumentProvider(String charset, StreamLimiter streamLimiter)
			throws LimitException {
		titleCache = null;
		metasCache = null;
		score = 0;
		rootNode = null;
		try {
			rootNode = getDocument(charset, streamLimiter.getNewInputStream());
		} catch (LimitException e) {
			throw e;
		} catch (Exception e) {
			Logging.warn(e.getMessage(), e);
		}
	}

	public HtmlNodeAbstract<?> getRootNode() {
		return rootNode;
	}

	public abstract String getName();

	protected abstract HtmlNodeAbstract<?> getDocument(String charset,
			InputStream inputStream) throws SAXException, IOException,
			ParserConfigurationException;

	public void score() {
		score = getTitle() != null ? 10000 : 0;
		score += getMetas() != null ? metasCache.size() * 1000 : 0;
		score += rootNode.countElements();
	}

	final public String getTitle() {
		if (titleCache != null)
			return titleCache;
		String[] p1 = { "html", "head", "title" };
		String title = rootNode.getTextNode(p1);
		if (title != null)
			return title;
		String[] p2 = { "html", "title" };
		titleCache = rootNode.getTextNode(p2);
		return titleCache;
	}

	final public List<HtmlNodeAbstract<?>> getMetas() {
		if (metasCache != null)
			return metasCache;
		String[] p1 = { "html", "head", "meta" };
		String[] p2 = { "html", "meta" };
		metasCache = rootNode.getNewNodeList();
		rootNode.getNodes(metasCache, p1);
		rootNode.getNodes(metasCache, p2);
		return metasCache;
	}

	final public static String getMetaContent(HtmlNodeAbstract<?> node) {
		String content = node.getAttributeText("content");
		if (content == null)
			return null;
		return StringEscapeUtils.unescapeHtml(content);
	}

	final public String getMetaCharset() {
		List<HtmlNodeAbstract<?>> metas = getMetas();
		if (metas == null)
			return null;
		for (HtmlNodeAbstract<?> node : metas) {
			String charset = node.getAttributeText("charset");
			if (charset != null && charset.length() > 0)
				return charset;
		}
		return null;
	}

	final public String getMetaHttpEquiv(String name) {
		List<HtmlNodeAbstract<?>> metas = getMetas();
		if (metas == null)
			return null;
		for (HtmlNodeAbstract<?> node : metas) {
			String attr_http_equiv = node.getAttributeText("http-equiv");
			if (name.equalsIgnoreCase(attr_http_equiv))
				return getMetaContent(node);
		}
		return null;
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
