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

package com.jaeksoft.searchlib.scoring;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.apache.lucene.search.Query;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class AdvancedScore {

	private final ReadWriteLock rwl = new ReadWriteLock();

	private List<AdvancedScoreItem> itemList;

	public final static String ADVANCED_SCORE_NODE = "advancedScore";

	public final static String ADVANCED_SCORE_ITEM_NODE = "scoreItem";

	public AdvancedScore() {
		itemList = new ArrayList<AdvancedScoreItem>(0);
	}

	public static AdvancedScore fromXmlConfig(XPathParser xpp, Node parentNode)
			throws XPathExpressionException {
		Node scoreNode = xpp.getNode(parentNode, ADVANCED_SCORE_NODE);
		if (scoreNode == null)
			return null;
		AdvancedScore advancedScore = new AdvancedScore();
		NodeList nodeList = xpp
				.getNodeList(scoreNode, ADVANCED_SCORE_ITEM_NODE);
		int l = nodeList.getLength();
		for (int i = 0; i < l; i++)
			advancedScore.itemList.add(new AdvancedScoreItem(nodeList.item(i)));
		return advancedScore;
	}

	public AdvancedScoreItem[] getArray() {
		rwl.r.lock();
		try {
			AdvancedScoreItem[] array = new AdvancedScoreItem[itemList.size()];
			itemList.toArray(array);
			return array;
		} finally {
			rwl.r.unlock();
		}
	}

	public boolean isEmpty() {
		rwl.r.lock();
		try {
			return itemList.size() == 0;
		} finally {
			rwl.r.unlock();
		}
	}

	public void copyFrom(AdvancedScore from) {
		rwl.w.lock();
		try {
			itemList.clear();
			for (AdvancedScoreItem item : from.getArray())
				itemList.add(new AdvancedScoreItem(item));
		} finally {
			rwl.w.unlock();
		}
	}

	public static AdvancedScore copy(AdvancedScore from) {
		if (from == null)
			return null;
		AdvancedScore to = new AdvancedScore();
		to.copyFrom(from);
		return to;
	}

	public Query getNewQuery(Query subQuery) {
		rwl.r.lock();
		try {
			return new AdvancedScoreQuery(subQuery, this);
		} finally {
			rwl.r.unlock();
		}
	}

	public void writeXmlConfig(XmlWriter xmlWriter) throws SAXException {
		rwl.r.lock();
		try {
			if (itemList.size() == 0)
				return;
			xmlWriter.startElement(ADVANCED_SCORE_NODE);
			for (AdvancedScoreItem item : itemList)
				item.writeXmlConfig(xmlWriter);
			xmlWriter.endElement();
		} finally {
			rwl.r.unlock();
		}
	}

	public void add(AdvancedScoreItem scoreItem) {
		rwl.w.lock();
		try {
			if (!itemList.contains(scoreItem))
				itemList.add(scoreItem);
		} finally {
			rwl.w.unlock();
		}
	}

	public void remove(AdvancedScoreItem scoreItem) {
		rwl.w.lock();
		try {
			itemList.remove(scoreItem);
		} finally {
			rwl.w.unlock();
		}
	}

	private final String getCacheKey() {
		rwl.r.lock();
		try {
			StringBuilder sb = new StringBuilder();
			for (AdvancedScoreItem scoreItem : itemList) {
				sb.append(scoreItem.name());
				sb.append('|');
			}
			return sb.toString();
		} finally {
			rwl.r.unlock();
		}
	}

	public final static String getCacheKey(AdvancedScore advancedScore) {
		if (advancedScore == null)
			return "";
		return advancedScore.getCacheKey();
	}

}
