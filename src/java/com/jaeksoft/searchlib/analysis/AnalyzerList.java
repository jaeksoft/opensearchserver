/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlInfo;

public class AnalyzerList extends AbstractList<Analyzer> implements XmlInfo {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1994552936047860059L;

	private ArrayList<Analyzer> analyzerList;
	private Map<String, Analyzer> analyzersName;

	public AnalyzerList() {
		analyzersName = new TreeMap<String, Analyzer>();
		analyzerList = new ArrayList<Analyzer>();
	}

	@Override
	public boolean add(Analyzer analyzer) {
		if (!analyzerList.add(analyzer))
			return false;
		analyzersName.put(analyzer.getName() + "_" + analyzer.getLang(),
				analyzer);
		return true;
	}

	@Override
	public Analyzer get(int index) {
		return analyzerList.get(index);
	}

	public Analyzer get(String name, String lang) {
		return analyzersName.get(name + "_" + lang);
	}

	public AnalyzerList get(String lang) {
		AnalyzerList anlzrList = new AnalyzerList();
		for (Analyzer anlzr : this.analyzerList)
			if (lang.equals(anlzr.getLang()))
				anlzrList.add(anlzr);
		return anlzrList;
	}

	@Override
	public int size() {
		return analyzerList.size();
	}

	public static AnalyzerList fromXmlConfig(XPathParser xpp, Node parentNode)
			throws XPathExpressionException, InstantiationException,
			IllegalAccessException, ClassNotFoundException, DOMException,
			IOException {
		AnalyzerList analyzers = new AnalyzerList();
		if (parentNode == null)
			return analyzers;
		NodeList nodes = xpp.getNodeList(parentNode, "analyzer");
		if (nodes == null)
			return analyzers;
		for (int i = 0; i < nodes.getLength(); i++)
			analyzers.add(Analyzer.fromXmlConfig(xpp, nodes.item(i)));
		return analyzers;
	}

	public void xmlInfo(PrintWriter writer) {
		writer.println("<analyzers>");
		for (Analyzer analyzer : this.analyzerList)
			analyzer.xmlInfo(writer);
		writer.println("</analyzers>");
	}

}
