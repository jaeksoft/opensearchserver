/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2010 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.analysis;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.tokenizer.TokenizerFactory;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class Analyzer {

	final private ReadWriteLock rwl = new ReadWriteLock();

	private TokenizerFactory tokenizer;
	private List<FilterFactory> filters;
	private String name;
	private LanguageEnum lang;
	private Config config;
	private CompiledAnalyzer queryAnalyzer;
	private CompiledAnalyzer indexAnalyzer;

	public Analyzer(Config config) throws SearchLibException {
		name = null;
		this.config = config;
		lang = LanguageEnum.UNDEFINED;
		setTokenizer(TokenizerFactory.getDefaultTokenizer(config));
		filters = new ArrayList<FilterFactory>();
		queryAnalyzer = null;
		indexAnalyzer = null;
	}

	public void copyFrom(Analyzer source) throws SearchLibException {
		rwl.w.lock();
		try {
			source.copyTo(this);
		} finally {
			rwl.w.unlock();
		}
	}

	private void copyTo(Analyzer target) throws SearchLibException {
		rwl.r.lock();
		try {
			target.name = this.name;
			target.lang = this.lang;
			target.tokenizer = (TokenizerFactory) ClassFactory
					.create(tokenizer);
			target.filters = new ArrayList<FilterFactory>();
			for (FilterFactory filter : filters)
				target.filters
						.add((FilterFactory) FilterFactory.create(filter));
			target.config = this.config;
			target.queryAnalyzer = this.queryAnalyzer;
			target.indexAnalyzer = this.indexAnalyzer;

		} finally {
			rwl.r.unlock();
		}
	}

	private Analyzer(Config config, XPathParser xpp, Node node)
			throws SearchLibException {
		this.config = config;
		this.name = XPathParser.getAttributeString(node, "name");
		this.lang = LanguageEnum.findByCode(XPathParser.getAttributeString(
				node, "lang"));
		this.filters = new ArrayList<FilterFactory>();
		this.queryAnalyzer = null;
		this.indexAnalyzer = null;
	}

	/**
	 * @return the tokenizer
	 */
	public TokenizerFactory getTokenizer() {
		rwl.r.lock();
		try {
			return tokenizer;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param tokenizer
	 *            the tokenizer to set
	 * @throws SearchLibException
	 */
	public void setTokenizer(TokenizerFactory tokenizer)
			throws SearchLibException {
		rwl.w.lock();
		try {
			this.tokenizer = TokenizerFactory.create(tokenizer);
			this.queryAnalyzer = null;
			this.indexAnalyzer = null;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the filters
	 */
	public List<FilterFactory> getFilters() {
		rwl.r.lock();
		try {
			return filters;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param filters
	 *            the filters to set
	 */
	public void setFilters(List<FilterFactory> filters) {
		rwl.w.lock();
		try {
			this.filters = filters;
			this.queryAnalyzer = null;
			this.indexAnalyzer = null;
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
	 * @return the lang
	 */
	public LanguageEnum getLang() {
		rwl.r.lock();
		try {
			return lang;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param lang
	 *            the lang to set
	 */
	public synchronized void setLang(LanguageEnum lang) {
		rwl.w.lock();
		try {
			this.lang = lang;
		} finally {
			rwl.w.unlock();
		}
	}

	public void add(FilterFactory filter) throws SearchLibException {
		rwl.w.lock();
		try {
			filters.add(filter);
			this.queryAnalyzer = null;
			this.indexAnalyzer = null;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * 
	 * @param config
	 * @param xpp
	 * @param node
	 * @return
	 * @throws SearchLibException
	 * @throws XPathExpressionException
	 */
	public static Analyzer fromXmlConfig(Config config, XPathParser xpp,
			Node node) throws SearchLibException, XPathExpressionException {
		if (node == null)
			return null;
		Analyzer analyzer = new Analyzer(config, xpp, node);

		String tokenizerFactoryClassName = XPathParser.getAttributeString(node,
				"tokenizer");
		if (tokenizerFactoryClassName != null) {
			analyzer.setTokenizer(TokenizerFactory.create(config,
					tokenizerFactoryClassName));
		}
		NodeList nodes = xpp.getNodeList(node, "tokenizer");
		if (nodes.getLength() > 0)
			analyzer.setTokenizer(TokenizerFactory.create(config, nodes.item(0)));

		nodes = xpp.getNodeList(node, "filter");
		for (int i = 0; i < nodes.getLength(); i++) {
			Node n = nodes.item(i);
			FilterFactory filter = FilterFactory.create(config, n);
			analyzer.add(filter);
		}
		return analyzer;
	}

	public void writeXmlConfig(XmlWriter writer) throws SAXException {
		rwl.r.lock();
		try {
			writer.startElement("analyzer", "name", getName(), "lang",
					lang != null ? lang.getCode() : null);
			if (tokenizer != null)
				tokenizer.writeXmlConfig(writer);
			if (filters != null && filters.size() > 0)
				for (FilterFactory filter : filters)
					filter.writeXmlConfig(writer);
			writer.endElement();
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * Move filter up
	 * 
	 * @param filter
	 */
	public void filterUp(FilterFactory filter) {
		rwl.w.lock();
		try {
			int i = filters.indexOf(filter);
			if (i == -1 || i == 0)
				return;
			filters.remove(i);
			filters.add(i - 1, filter);
			this.queryAnalyzer = null;
			this.indexAnalyzer = null;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * Move filter down
	 * 
	 * @param filter
	 */
	public void filterDown(FilterFactory filter) {
		rwl.w.lock();
		try {
			int i = filters.indexOf(filter);
			if (i == -1 || i == filters.size() - 1)
				return;
			filters.remove(i);
			filters.add(i + 1, filter);
			this.queryAnalyzer = null;
			this.indexAnalyzer = null;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * Remove the filter
	 * 
	 * @param filter
	 */
	public void filterRemove(FilterFactory filter) {
		rwl.w.lock();
		try {
			filters.remove(filter);
			this.queryAnalyzer = null;
			this.indexAnalyzer = null;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * Returns the compiled analyzer for queries
	 * 
	 * @return
	 */
	public CompiledAnalyzer getQueryAnalyzer() {
		rwl.r.lock();
		try {
			if (queryAnalyzer != null)
				return queryAnalyzer;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			if (queryAnalyzer != null)
				return queryAnalyzer;
			queryAnalyzer = new CompiledAnalyzer(tokenizer, filters,
					FilterScope.QUERY);
			return queryAnalyzer;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * Returns the compiled analyzer for indexation
	 * 
	 * @return
	 */
	public CompiledAnalyzer getIndexAnalyzer() {
		rwl.r.lock();
		try {
			if (indexAnalyzer != null)
				return indexAnalyzer;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			if (indexAnalyzer != null)
				return indexAnalyzer;
			indexAnalyzer = new CompiledAnalyzer(tokenizer, filters,
					FilterScope.INDEX);
			return indexAnalyzer;
		} finally {
			rwl.w.unlock();
		}

	}

}
