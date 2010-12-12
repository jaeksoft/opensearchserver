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
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class JobList {

	private ReadWriteLock rwl = new ReadWriteLock();

	private Map<String, JobItem> jobs;

	public JobList() {
		jobs = new TreeMap<String, JobItem>();
	}

	public void add(JobItem job) {
		rwl.w.lock();
		try {
			jobs.put(job.getName(), job);
		} finally {
			rwl.w.unlock();
		}
	}

	public void remove(JobItem job) {
		rwl.w.lock();
		try {
			jobs.remove(job);
		} finally {
			rwl.w.unlock();
		}
	}

	private final static String JOBS_ROOTNODE_NAME = "jobs";

	public static JobList fromXml(Config config, File file)
			throws XPathExpressionException, ParserConfigurationException,
			SAXException, IOException {
		JobList jobList = new JobList();
		if (!file.exists())
			return jobList;
		XPathParser xpp = new XPathParser(file);
		Node rootNode = xpp.getNode(JOBS_ROOTNODE_NAME);
		if (rootNode == null)
			return jobList;
		NodeList nodes = xpp.getNodeList(rootNode, JobItem.JOB_NODE_NAME);
		if (nodes == null)
			return jobList;
		for (int i = 0; i < nodes.getLength(); i++) {
			JobItem jobItem = JobItem.fromXml(config, xpp, nodes.item(i));
			jobList.add(jobItem);
		}
		return jobList;
	}

	public void writeXml(XmlWriter xmlWriter) throws SAXException {
		rwl.r.lock();
		try {
			xmlWriter.startElement("jobs");
			for (JobItem job : jobs.values())
				job.writeXml(xmlWriter);
			xmlWriter.endElement();
		} finally {
			rwl.r.unlock();
		}
	}

}
