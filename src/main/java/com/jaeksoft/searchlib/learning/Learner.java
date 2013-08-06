/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.learning;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPathExpressionException;

import org.apache.cxf.helpers.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.FieldMap;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.InfoCallback;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class Learner implements Comparable<Learner> {

	private final static String LEARNER_ITEM_ROOT_NODE_NAME = "learner";
	private final static String LEARNER_ITEM_ROOT_ATTR_NAME = "name";
	private final static String LEARNER_ITEM_ROOT_ATTR_ACTIVE = "active";
	private final static String LEARNER_ITEM_ROOT_ATTR_CLASS = "class";
	private final static String LEARNER_ITEM_ROOT_ATTR_BUFFER = "buffer";
	private final static String LEARNER_ITEM_ROOT_ATTR_MAX_RANK = "maxRank";
	private final static String LEARNER_ITEM_ROOT_ATTR_MIN_SCORE = "minScore";
	private final static String LEARNER_ITEM_ROOT_ATTR_SEARCH_REQUEST = "searchRequest";
	private final static String LEARNER_ITEM_MAP_SRC_NODE_NAME = "sourceFields";
	private final static String LEARNER_ITEM_MAP_TGT_NODE_NAME = "targetFields";

	private final ReadWriteLock rwl = new ReadWriteLock();

	private String name;

	private String className;

	private String searchRequest;

	private final FieldMap sourceFieldMap;

	private final FieldMap targetFieldMap;

	private boolean active;

	private int maxRank;

	private double minScore;

	private int buffer;

	private LearnerInterface learnerInstance;

	public Learner(String name) {
		this.name = name;
		active = false;
		className = null;
		learnerInstance = null;
		searchRequest = null;
		sourceFieldMap = new FieldMap();
		targetFieldMap = new FieldMap();
		maxRank = 1;
		minScore = 0;
		buffer = 1000;
	}

	public Learner() {
		this((String) null);
	}

	public Learner(Learner source) {
		this();
		source.copyTo(this);
	}

	public void copyTo(Learner target) {
		rwl.r.lock();
		try {
			target.rwl.w.lock();
			try {
				target.name = name;
				target.active = active;
				target.className = className;
				target.searchRequest = searchRequest;
				target.learnerInstance = learnerInstance;
				sourceFieldMap.copyTo(target.sourceFieldMap);
				targetFieldMap.copyTo(target.targetFieldMap);
				target.maxRank = maxRank;
				target.minScore = minScore;
			} finally {
				target.rwl.w.unlock();
			}
		} finally {
			rwl.r.unlock();
		}
	}

	protected Learner(File file) throws ParserConfigurationException,
			SAXException, IOException, XPathExpressionException,
			SearchLibException {
		this();
		if (!file.exists())
			return;
		Document document = DOMUtils.readXml(new StreamSource(file));
		Node rootNode = DomUtils.getFirstNode(document,
				LEARNER_ITEM_ROOT_NODE_NAME);
		if (rootNode == null)
			return;
		setName(XPathParser.getAttributeString(rootNode,
				LEARNER_ITEM_ROOT_ATTR_NAME));
		setActive("yes".equalsIgnoreCase(XPathParser.getAttributeString(
				rootNode, LEARNER_ITEM_ROOT_ATTR_ACTIVE)));
		setClassName(XPathParser.getAttributeString(rootNode,
				LEARNER_ITEM_ROOT_ATTR_CLASS));
		setSearchRequest(XPathParser.getAttributeString(rootNode,
				LEARNER_ITEM_ROOT_ATTR_SEARCH_REQUEST));
		setMinScore(XPathParser.getAttributeDouble(rootNode,
				LEARNER_ITEM_ROOT_ATTR_MIN_SCORE));
		setMaxRank(XPathParser.getAttributeValue(rootNode,
				LEARNER_ITEM_ROOT_ATTR_MAX_RANK));
		setBuffer(XPathParser.getAttributeValue(rootNode,
				LEARNER_ITEM_ROOT_ATTR_BUFFER));
		sourceFieldMap.load(DomUtils.getFirstNode(rootNode,
				LEARNER_ITEM_MAP_SRC_NODE_NAME));
		targetFieldMap.load(DomUtils.getFirstNode(rootNode,
				LEARNER_ITEM_MAP_TGT_NODE_NAME));
	}

	/**
	 * 
	 * @return
	 */
	public FieldMap getSourceFieldMap() {
		rwl.r.lock();
		try {
			return sourceFieldMap;
		} finally {
			rwl.r.unlock();
		}
	}

	public FieldMap getTargetFieldMap() {
		rwl.r.lock();
		try {
			return targetFieldMap;
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
	 * @param className
	 *            the className to set
	 * @throws SearchLibException
	 */
	public void setClassName(String className) throws SearchLibException {
		rwl.w.lock();
		try {
			if (!StringUtils.equals(className, this.className)) {
				if (learnerInstance != null)
					learnerInstance.reset();
				learnerInstance = null;
			}
			this.className = className;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the classNAme
	 */
	public String getClassName() {
		rwl.r.lock();
		try {
			return className;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param active
	 *            the active to set
	 */
	public void setActive(boolean active) {
		rwl.w.lock();
		try {
			this.active = active;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the active
	 */
	public boolean isActive() {
		rwl.r.lock();
		try {
			return active;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public int compareTo(Learner o) {
		rwl.r.lock();
		try {
			return name.compareTo(o.name);
		} finally {
			rwl.r.unlock();
		}
	}

	public void writeXml(XmlWriter xmlWriter) throws SAXException {
		rwl.r.lock();
		try {
			xmlWriter.startElement(LEARNER_ITEM_ROOT_NODE_NAME,
					LEARNER_ITEM_ROOT_ATTR_NAME, name,
					LEARNER_ITEM_ROOT_ATTR_CLASS, className,
					LEARNER_ITEM_ROOT_ATTR_SEARCH_REQUEST, searchRequest,
					LEARNER_ITEM_ROOT_ATTR_ACTIVE, active ? "yes" : "no",
					LEARNER_ITEM_ROOT_ATTR_MAX_RANK, Integer.toString(maxRank),
					LEARNER_ITEM_ROOT_ATTR_MIN_SCORE,
					Double.toString(minScore), LEARNER_ITEM_ROOT_ATTR_BUFFER,
					Integer.toString(buffer));
			xmlWriter.startElement(LEARNER_ITEM_MAP_SRC_NODE_NAME);
			sourceFieldMap.store(xmlWriter);
			xmlWriter.endElement();
			xmlWriter.startElement(LEARNER_ITEM_MAP_TGT_NODE_NAME);
			targetFieldMap.store(xmlWriter);
			xmlWriter.endElement();
			xmlWriter.endElement();
		} finally {
			rwl.r.unlock();
		}
	}

	public String getSearchRequest() {
		rwl.r.lock();
		try {
			return searchRequest;
		} finally {
			rwl.r.unlock();
		}
	}

	public void setSearchRequest(String searchRequest) {
		rwl.w.lock();
		try {
			this.searchRequest = searchRequest;
		} finally {
			rwl.w.unlock();
		}
	}

	private LearnerInterface getInstance(Client client)
			throws SearchLibException {
		rwl.r.lock();
		try {
			if (learnerInstance != null)
				return learnerInstance;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			if (learnerInstance != null)
				return learnerInstance;
			if (client == null)
				return null;
			learnerInstance = (LearnerInterface) Class.forName(className)
					.newInstance();
			File instancesFile = new File(client.getLearnerDirectory(), name
					+ ".data");
			learnerInstance.init(instancesFile);
			return learnerInstance;
		} catch (ClassNotFoundException e) {
			throw new SearchLibException(e);
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		} finally {
			rwl.w.unlock();
		}
	}

	public LearnerInterface getInstance() throws SearchLibException {
		return getInstance(null);
	}

	public void checkInstance(Client client) throws SearchLibException {
		getInstance(client);
	}

	public void classify(Client client, IndexDocument document)
			throws SearchLibException {
		rwl.r.lock();
		try {
			LearnerInterface instance = getInstance(client);
			instance.classify(document, sourceFieldMap, targetFieldMap,
					maxRank, minScore);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			rwl.r.unlock();
		}
	}

	public LearnerResultItem[] classify(Client client, String text,
			Integer max_rank, Double min_score) throws SearchLibException {
		LearnerInterface instance = getInstance(client);
		rwl.r.lock();
		try {
			if (max_rank == null)
				max_rank = maxRank;
			if (min_score == null)
				min_score = minScore;
			List<LearnerResultItem> list = new ArrayList<LearnerResultItem>(0);
			instance.classify(text, max_rank, min_score, list);
			LearnerResultItem[] result = LearnerResultItem.sortArray(list);
			return LearnerResultItem.maxRank(result, max_rank);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			rwl.r.unlock();
		}
	}

	public void learn(Client client, InfoCallback infoCallback)
			throws SearchLibException {
		LearnerInterface instance = getInstance(client);
		rwl.r.lock();
		try {
			instance.learn(client, searchRequest, sourceFieldMap, buffer,
					infoCallback);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			rwl.r.unlock();
		}
	}

	public void reset(Client client) throws SearchLibException {
		LearnerInterface instance = getInstance(client);
		instance.reset();

	}

	/**
	 * @return the maxRank
	 */
	public int getMaxRank() {
		rwl.r.lock();
		try {
			return maxRank;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param maxRank
	 *            the maxRank to set
	 */
	public void setMaxRank(int maxRank) {
		rwl.w.lock();
		try {
			this.maxRank = maxRank;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the minScore
	 */
	public double getMinScore() {
		rwl.r.lock();
		try {
			return minScore;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param minScore
	 *            the minScore to set
	 */
	public void setMinScore(double minScore) {
		rwl.w.lock();
		try {
			this.minScore = minScore;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the buffer
	 */
	public double getBuffer() {
		rwl.r.lock();
		try {
			return buffer;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param buffer
	 *            the buffer to set
	 */
	public void setBuffer(int buffer) {
		rwl.w.lock();
		try {
			if (buffer <= 0)
				buffer = 1;
			this.buffer = buffer;
		} finally {
			rwl.w.unlock();
		}
	}
}
