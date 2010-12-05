/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.scheduler;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class JobManager {

	private ReadWriteLock rwl = new ReadWriteLock();

	private HashMap<String, JobItem> jobMap;

	public JobManager() {
		jobMap = new HashMap<String, JobItem>();
	}

	public void add(JobItem item) {

	}

	private final static String JOBS_ROOTNODE_NAME = "jobs";

	public static JobManager fromXml(File file)
			throws XPathExpressionException, ParserConfigurationException,
			SAXException, IOException {
		JobManager jobManager = new JobManager();
		if (!file.exists())
			return jobManager;
		XPathParser xpp = new XPathParser(file);
		Node rootNode = xpp.getNode(JOBS_ROOTNODE_NAME);
		if (rootNode == null)
			return jobManager;
		NodeList nodes = xpp.getNodeList(rootNode, JobItem.JOB_NODE_NAME);
		if (nodes == null)
			return jobManager;
		for (int i = 0; i < nodes.getLength(); i++) {
			JobItem jobItem = JobItem.fromXml(xpp, nodes.item(i));
			jobManager.add(jobItem);
		}
		return jobManager;
	}

	public void writeXml(XmlWriter xmlWriter) throws SAXException {
		rwl.r.lock();
		try {
			xmlWriter.startElement("jobs");
			xmlWriter.endElement();
		} finally {
			rwl.r.unlock();
		}
	}
}
