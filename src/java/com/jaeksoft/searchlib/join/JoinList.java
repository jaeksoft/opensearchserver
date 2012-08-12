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

package com.jaeksoft.searchlib.join;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.index.ReaderLocal;
import com.jaeksoft.searchlib.result.collector.DocIdInterface;
import com.jaeksoft.searchlib.util.Timer;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.jaeksoft.searchlib.web.ServletTransaction;

public class JoinList implements Iterable<JoinItem> {

	private List<JoinItem> joinList;

	private transient Config config;

	public JoinList(JoinList list) {
		this.config = list.config;
		this.joinList = new ArrayList<JoinItem>(list.size());
		for (JoinItem item : list)
			add(new JoinItem(item));
	}

	public JoinList(Config config) {
		this.joinList = new ArrayList<JoinItem>(0);
		this.config = config;
	}

	private void renumbered() {
		int i = 1;
		for (JoinItem item : joinList)
			item.setParamPosition(i++);
	}

	public void add(JoinItem joinItem) {
		joinList.add(joinItem);
		renumbered();
	}

	public void remove(JoinItem joinItem) {
		joinList.remove(joinItem);
		renumbered();
	}

	public int size() {
		return joinList.size();
	}

	@Override
	public Iterator<JoinItem> iterator() {
		return joinList.iterator();
	}

	public Object[] toArray() {
		return joinList.toArray();
	}

	public void writeXmlConfig(XmlWriter xmlWriter) throws SAXException {
		for (JoinItem item : joinList)
			item.writeXmlConfig(xmlWriter);
	}

	public void add(XPathParser xpp, Node node) throws XPathExpressionException {
		joinList.add(new JoinItem(xpp, node));
	}

	public DocIdInterface apply(ReaderLocal reader, DocIdInterface collector,
			JoinResult[] joinResults, Timer timer) throws SearchLibException {
		int joinItemSize = joinList.size();
		int joinItemPos = 0;
		for (JoinItem joinItem : joinList) {
			JoinResult joinResult = new JoinResult(joinItemPos++,
					joinItem.getParamPosition(), joinItem.isReturnFields());
			joinResults[joinResult.joinPosition] = joinResult;
			collector = joinItem.apply(reader, collector, joinItemSize,
					joinResult, timer);
		}
		return collector;
	}

	public void setFromServlet(ServletTransaction transaction) {
		for (JoinItem item : joinList) {
			String q = transaction.getParameterString(item.getParamPosition());
			if (q != null)
				item.setQueryString(q);
		}
	}

}
