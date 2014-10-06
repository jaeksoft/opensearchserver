/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012-2014 Emmanuel Keller / Jaeksoft
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.ReaderAbstract;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
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

	public JoinItem[] getArray() {
		return joinList.toArray(new JoinItem[joinList.size()]);
	}

	public void writeXmlConfig(XmlWriter xmlWriter) throws SAXException {
		for (JoinItem item : joinList)
			item.writeXmlConfig(xmlWriter);
	}

	public void add(XPathParser xpp, Node node) throws XPathExpressionException {
		joinList.add(new JoinItem(xpp, node));
	}

	public DocIdInterface apply(AbstractSearchRequest searchRequest,
			ReaderAbstract reader, DocIdInterface collector,
			JoinResult[] joinResults, Timer timer) throws SearchLibException {
		int joinItemSize = joinList.size();
		int joinItemPos = 0;
		List<JoinFacet> facetList = new ArrayList<JoinFacet>(0);
		for (JoinItem joinItem : joinList) {
			JoinResult joinResult = new JoinResult(joinItemPos++,
					joinItem.getParamPosition(), joinItem.isReturnFields());
			joinResults[joinResult.joinPosition] = joinResult;
			collector = joinItem.apply(searchRequest, reader, collector,
					joinItemSize, joinResult, facetList, timer);
		}
		for (JoinFacet joinFacet : facetList)
			joinFacet.apply(collector, timer);
		for (JoinResult joinResult : joinResults)
			joinResult.setJoinDocInterface(collector);
		return collector;
	}

	final public void setFromServlet(final ServletTransaction transaction,
			final String prefix) throws SearchLibException, SyntaxError {
		for (JoinItem item : joinList)
			item.setFromServlet(transaction, prefix);
	}

	public void setParam(int pos, String param) throws SearchLibException {
		if (pos < 0 || pos >= joinList.size())
			throw new SearchLibException("Wrong join parameter (" + pos + ")");
		joinList.get(pos).setParam(param);
	}

	public void addAuthJoin() throws SearchLibException, IOException {
		if (joinList != null)
			for (JoinItem item : joinList)
				if (item instanceof AuthJoinItem)
					return;
		add(new AuthJoinItem(config));
	}

	public void init(Config config) {
		this.config = config;
	}

}
