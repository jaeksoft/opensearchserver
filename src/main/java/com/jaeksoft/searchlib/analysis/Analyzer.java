/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.DOMException;
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

	private TokenizerFactory queryTokenizer;
	private TokenizerFactory indexTokenizer;
	private List<FilterFactory> filters;
	private String name;
	private LanguageEnum lang;
	private Config config;
	private CompiledAnalyzer queryAnalyzer;
	private CompiledAnalyzer indexAnalyzer;

	public Analyzer(Config config) throws SearchLibException, ClassNotFoundException {
		name = null;
		this.config = config;
		lang = LanguageEnum.UNDEFINED;
		setQueryTokenizer(TokenizerFactory.getDefaultTokenizer(config));
		setIndexTokenizer(TokenizerFactory.getDefaultTokenizer(config));
		filters = new ArrayList<FilterFactory>();
		queryAnalyzer = null;
		indexAnalyzer = null;
	}

	public void copyFrom(Analyzer source) throws SearchLibException, ClassNotFoundException {
		rwl.w.lock();
		try {
			source.copyTo(this);
		} finally {
			rwl.w.unlock();
		}
	}

	private void copyTo(Analyzer target) throws SearchLibException, ClassNotFoundException {
		rwl.r.lock();
		try {
			target.name = this.name;
			target.lang = this.lang;
			target.queryTokenizer = (TokenizerFactory) ClassFactory.create(queryTokenizer);
			target.indexTokenizer = (TokenizerFactory) ClassFactory.create(indexTokenizer);
			target.filters = new ArrayList<FilterFactory>();
			for (FilterFactory filter : filters)
				target.filters.add((FilterFactory) FilterFactory.create(filter));
			target.config = this.config;
			target.queryAnalyzer = null;
			target.indexAnalyzer = null;

		} finally {
			rwl.r.unlock();
		}
	}

	private Analyzer(Config config, XPathParser xpp, Node node) throws SearchLibException {
		this.config = config;
		this.name = XPathParser.getAttributeString(node, "name");
		this.lang = LanguageEnum.findByCode(XPathParser.getAttributeString(node, "lang"));
		this.filters = new ArrayList<FilterFactory>();
		this.queryAnalyzer = null;
		this.indexAnalyzer = null;
	}

	public void recompile() {
		rwl.w.lock();
		try {
			if (queryAnalyzer != null)
				queryAnalyzer.close();
			queryAnalyzer = null;
			if (indexAnalyzer != null)
				indexAnalyzer.close();
			indexAnalyzer = null;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the tokenizer
	 */
	public TokenizerFactory getIndexTokenizer() {
		rwl.r.lock();
		try {
			return indexTokenizer;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @return the tokenizer
	 */
	public TokenizerFactory getQueryTokenizer() {
		rwl.r.lock();
		try {
			return queryTokenizer;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param tokenizer
	 *            the tokenizer to set
	 * @throws SearchLibException
	 *             inherited error
	 * @throws ClassNotFoundException
	 *             inherited error
	 */
	public void setIndexTokenizer(TokenizerFactory tokenizer) throws SearchLibException, ClassNotFoundException {
		rwl.w.lock();
		try {
			this.indexTokenizer = TokenizerFactory.create(tokenizer);
			indexAnalyzer = null;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @param tokenizer
	 *            the tokenizer to set
	 * @throws SearchLibException
	 *             inherited error
	 * @throws ClassNotFoundException
	 *             inherited error
	 */
	public void setQueryTokenizer(TokenizerFactory tokenizer) throws SearchLibException, ClassNotFoundException {
		rwl.w.lock();
		try {
			this.queryTokenizer = TokenizerFactory.create(tokenizer);
			queryAnalyzer = null;
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
			queryAnalyzer = null;
			indexAnalyzer = null;
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
			this.name = name == null ? null : name.intern();
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
	public void setLang(LanguageEnum lang) {
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
			queryAnalyzer = null;
			indexAnalyzer = null;
		} finally {
			rwl.w.unlock();
		}
	}

	public void add(Collection<FilterFactory> filters) {
		rwl.w.lock();
		try {
			this.filters.addAll(filters);
			queryAnalyzer = null;
			indexAnalyzer = null;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * Replace the old filter by the new one
	 * 
	 * @param oldFilter
	 *            removed filter
	 * @param newFilter
	 *            new filter
	 */
	public void replace(FilterFactory oldFilter, FilterFactory newFilter) {
		rwl.w.lock();
		try {
			int pos = 0;
			for (FilterFactory filter : filters)
				if (filter == oldFilter) {
					filters.set(pos, newFilter);
					queryAnalyzer = null;
					indexAnalyzer = null;
					return;
				} else
					pos++;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * 
	 * @param config
	 *            current config
	 * @param xpp
	 *            and XPathParser instance
	 * @param node
	 *            the current node
	 * @return a new analyzer
	 * @throws SearchLibException
	 *             inherited error
	 * @throws XPathExpressionException
	 *             inherited error
	 * @throws ClassNotFoundException
	 *             inherited error
	 * @throws DOMException
	 *             inherited error
	 */
	public static Analyzer fromXmlConfig(Config config, XPathParser xpp, Node node)
			throws SearchLibException, XPathExpressionException, DOMException, ClassNotFoundException {
		if (node == null)
			return null;
		Analyzer analyzer = new Analyzer(config, xpp, node);

		String indexTokenizer = XPathParser.getAttributeString(node, "tokenizer");
		if (indexTokenizer != null)
			analyzer.setIndexTokenizer(TokenizerFactory.create(config, indexTokenizer));
		NodeList nodes = xpp.getNodeList(node, "tokenizer");
		if (nodes.getLength() > 0)
			analyzer.setIndexTokenizer(TokenizerFactory.create(config, nodes.item(0)));

		nodes = xpp.getNodeList(node, "queryTokenizer");
		if (nodes.getLength() > 0)
			analyzer.setQueryTokenizer(TokenizerFactory.create(config, nodes.item(0)));
		else
			analyzer.setQueryTokenizer(TokenizerFactory.create(analyzer.indexTokenizer));

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
			writer.startElement("analyzer", "name", getName(), "lang", lang != null ? lang.getCode() : null);
			if (indexTokenizer != null)
				indexTokenizer.writeXmlConfig("tokenizer", writer);
			if (queryTokenizer != null)
				queryTokenizer.writeXmlConfig("queryTokenizer", writer);
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
	 *            the filter to move
	 */
	public void filterUp(FilterFactory filter) {
		rwl.w.lock();
		try {
			int i = filters.indexOf(filter);
			if (i == -1 || i == 0)
				return;
			filters.remove(i);
			filters.add(i - 1, filter);
			queryAnalyzer = null;
			indexAnalyzer = null;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * Move filter down
	 * 
	 * @param filter
	 *            the filter to move
	 */
	public void filterDown(FilterFactory filter) {
		rwl.w.lock();
		try {
			int i = filters.indexOf(filter);
			if (i == -1 || i == filters.size() - 1)
				return;
			filters.remove(i);
			filters.add(i + 1, filter);
			queryAnalyzer = null;
			indexAnalyzer = null;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * Remove the filter
	 * 
	 * @param filter
	 *            the filter to remove
	 */
	public void filterRemove(FilterFactory filter) {
		rwl.w.lock();
		try {
			filters.remove(filter);
			queryAnalyzer = null;
			indexAnalyzer = null;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the compiled analyzer for queries
	 * @throws SearchLibException
	 *             inherited error
	 */
	public CompiledAnalyzer getQueryAnalyzer() throws SearchLibException {
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
			queryAnalyzer = new CompiledAnalyzer(queryTokenizer, filters, FilterScope.QUERY);
			return queryAnalyzer;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * 
	 * @return the compiled analyzer for indexation
	 * @throws SearchLibException
	 *             inherited error
	 */
	public CompiledAnalyzer getIndexAnalyzer() throws SearchLibException {
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
			indexAnalyzer = new CompiledAnalyzer(indexTokenizer, filters, FilterScope.INDEX);
			return indexAnalyzer;
		} finally {
			rwl.w.unlock();
		}

	}

}
