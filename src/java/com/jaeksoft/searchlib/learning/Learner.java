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

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPathExpressionException;

import org.apache.cxf.helpers.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class Learner implements Comparable<Learner> {

	private final static String LEARNER_ITEM_ROOT_NODE_NAME = "learner";
	private final static String LEARNER_ITEM_ROOT_ATTR_NAME = "name";
	private final static String LEARNER_ITEM_ROOT_ATTR_ACTIVE = "active";
	private final static String LEARNER_ITEM_ROOT_ATTR_CLASS = "class";

	private final ReadWriteLock rwl = new ReadWriteLock();

	private String name;

	private String className;

	private String parameters;

	private boolean active;

	private LearnerInterface learnerInstance;

	public Learner() {
		name = null;
		active = false;
		className = null;
		parameters = null;
		learnerInstance = null;
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
				target.parameters = parameters;
				target.learnerInstance = learnerInstance;
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
		setParameters(rootNode.getTextContent());
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
	 */
	public void setClassName(String className) {
		rwl.w.lock();
		try {
			if (!StringUtils.equals(className, this.className))
				learnerInstance = null;
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

	public String getParameters() {
		rwl.r.lock();
		try {
			return parameters;
		} finally {
			rwl.r.unlock();
		}
	}

	public void setParameters(String parameters) {
		rwl.w.lock();
		try {
			this.parameters = parameters;
		} finally {
			rwl.w.unlock();
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
					LEARNER_ITEM_ROOT_ATTR_ACTIVE, active ? "yes" : "no");
			if (parameters != null && parameters.length() > 0)
				xmlWriter.textNode(parameters);
			xmlWriter.endElement();
		} finally {
			rwl.r.unlock();
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
			learnerInstance.init(client, this);
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

	public void checkInstance(Client client) throws SearchLibException {
		getInstance(client);
	}

	public void learn(Client client, IndexDocument document)
			throws SearchLibException {
		LearnerInterface instance = getInstance(client);
		instance.learn(client, document);
	}

	public void flush() throws SearchLibException {
		LearnerInterface instance = getInstance(null);
		instance.flush();
	}
}
