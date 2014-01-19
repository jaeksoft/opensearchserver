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

package com.jaeksoft.searchlib.scoring;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.jaeksoft.searchlib.webservice.query.search.SearchQueryAbstract.Scoring.Type;

public class AdvancedScore {

	private final ReadWriteLock rwl = new ReadWriteLock();

	private List<AdvancedScoreItem> itemList;

	private AdvancedScoreItem[] array;

	private double scoreWeight;

	public final static String ADVANCED_SCORE_NODE = "advancedScore";

	public final static String ADVANCED_SCORE_ATTR_SCOREWEIGHT = "scoreWeight";

	public final static String ADVANCED_SCORE_ITEM_NODE = "scoreItem";

	public AdvancedScore() {
		itemList = new ArrayList<AdvancedScoreItem>(0);
		array = null;
		scoreWeight = 1.0;
	}

	public static AdvancedScore fromXmlConfig(XPathParser xpp, Node parentNode)
			throws XPathExpressionException {
		Node scoreNode = xpp.getNode(parentNode, ADVANCED_SCORE_NODE);
		if (scoreNode == null)
			return null;
		AdvancedScore advancedScore = new AdvancedScore();
		advancedScore.setScoreWeight(DomUtils.getAttributeDouble(scoreNode,
				ADVANCED_SCORE_ATTR_SCOREWEIGHT, 1.0));
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
			if (array != null)
				return array;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			if (array != null)
				return array;
			array = new AdvancedScoreItem[itemList.size()];
			itemList.toArray(array);
			return array;
		} finally {
			rwl.w.unlock();
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

	public void writeXmlConfig(XmlWriter xmlWriter) throws SAXException {
		rwl.r.lock();
		try {
			if (itemList.size() == 0)
				return;
			xmlWriter.startElement(ADVANCED_SCORE_NODE,
					ADVANCED_SCORE_ATTR_SCOREWEIGHT,
					Double.toString(scoreWeight));
			for (AdvancedScoreItem item : itemList)
				item.writeXmlConfig(xmlWriter);
			xmlWriter.endElement();
		} finally {
			rwl.r.unlock();
		}
	}

	public void addItem(AdvancedScoreItem scoreItem) {
		rwl.w.lock();
		try {
			if (!itemList.contains(scoreItem))
				itemList.add(scoreItem);
		} finally {
			rwl.w.unlock();
		}
	}

	public void removeItem(AdvancedScoreItem scoreItem) {
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

	public void setScoreWeight(double scoreWeight) {
		rwl.w.lock();
		try {
			this.scoreWeight = scoreWeight;
		} finally {
			rwl.w.unlock();
		}
	}

	public double getScoreWeight() {
		rwl.r.lock();
		try {
			return scoreWeight;
		} finally {
			rwl.r.unlock();
		}
	}

	final public boolean isDistance() {
		rwl.r.lock();
		try {
			for (AdvancedScoreItem item : itemList)
				if (item.getType() == Type.DISTANCE)
					return true;
			return false;
		} finally {
			rwl.r.unlock();
		}
	}

}
