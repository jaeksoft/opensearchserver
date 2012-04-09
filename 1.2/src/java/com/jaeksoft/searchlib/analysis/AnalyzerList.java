/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2010 Emmanuel Keller / Jaeksoft
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class AnalyzerList {

	private Map<String, List<Analyzer>> nameListMap;
	private Map<String, Analyzer> nameLangMap;

	private final ReadWriteLock rwl = new ReadWriteLock();

	public AnalyzerList() {
		nameLangMap = new TreeMap<String, Analyzer>();
		nameListMap = new TreeMap<String, List<Analyzer>>();
	}

	final private static String getAnalyzerLangKey(String analyzerName,
			LanguageEnum lang) {
		StringBuffer sb = new StringBuffer(analyzerName);
		sb.append('_');
		sb.append(lang.getCode());
		return sb.toString();
	}

	final private static String getAnalyzerLangKey(Analyzer analyzer) {
		return getAnalyzerLangKey(analyzer.getName(), analyzer.getLang());
	}

	public boolean add(Analyzer analyzer) {
		rwl.w.lock();
		try {
			List<Analyzer> alist = nameListMap.get(analyzer.getName());
			if (alist == null) {
				alist = new ArrayList<Analyzer>();
				nameListMap.put(analyzer.getName(), alist);
			}
			alist.add(analyzer);
			nameLangMap.put(getAnalyzerLangKey(analyzer), analyzer);
			return true;
		} finally {
			rwl.w.unlock();
		}
	}

	public void remove(Analyzer analyzer) {
		rwl.w.lock();
		try {
			List<Analyzer> alist = nameListMap.get(analyzer.getName());
			if (alist != null) {
				alist.remove(analyzer);
				if (alist.size() == 0)
					nameListMap.remove(analyzer.getName());
			}
			nameLangMap.remove(getAnalyzerLangKey(analyzer));
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * Recompile the analyzers
	 */
	public void recompile() {
		rwl.r.lock();
		try {
			for (List<Analyzer> alist : nameListMap.values())
				for (Analyzer a : alist)
					a.recompile();
		} finally {
			rwl.r.unlock();
		}
	}

	public Set<String> getNameSet() {
		rwl.r.lock();
		try {
			return nameListMap.keySet();
		} finally {
			rwl.r.unlock();
		}
	}

	public List<Analyzer> get(String name) {
		rwl.r.lock();
		try {
			return nameListMap.get(name);
		} finally {
			rwl.r.unlock();
		}
	}

	public Analyzer get(String name, LanguageEnum lang) {
		rwl.r.lock();
		try {
			if (lang == null)
				lang = LanguageEnum.UNDEFINED;
			return nameLangMap.get(getAnalyzerLangKey(name, lang));
		} finally {
			rwl.r.unlock();
		}
	}

	public static AnalyzerList fromXmlConfig(Config config, XPathParser xpp,
			Node parentNode) throws XPathExpressionException,
			SearchLibException {
		AnalyzerList analyzers = new AnalyzerList();
		if (parentNode == null)
			return analyzers;
		NodeList nodes = xpp.getNodeList(parentNode, "analyzer");
		if (nodes == null)
			return analyzers;
		for (int i = 0; i < nodes.getLength(); i++)
			analyzers.add(Analyzer.fromXmlConfig(config, xpp, nodes.item(i)));
		return analyzers;
	}

	public void writeXmlConfig(XmlWriter writer) throws SAXException {
		rwl.r.lock();
		try {
			if (nameListMap.size() == 0)
				return;
			writer.startElement("analyzers");
			for (Analyzer analyzer : nameLangMap.values())
				analyzer.writeXmlConfig(writer);
			writer.endElement();
		} finally {
			rwl.r.unlock();
		}
	}

}
