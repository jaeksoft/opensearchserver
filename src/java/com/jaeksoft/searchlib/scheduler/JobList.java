/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.scheduler;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class JobList {

	private Config config;

	private ReadWriteLock rwl = new ReadWriteLock();

	private Map<String, JobItem> jobs;

	private JobItem[] jobsCache;

	public JobList(Config config) {
		this.config = config;
		jobs = new TreeMap<String, JobItem>();
		jobsCache = null;
	}

	/**
	 * Add a job to the list
	 * 
	 * @param job
	 */
	public void add(JobItem job) {
		rwl.w.lock();
		try {
			jobs.put(job.getName(), job);
			jobsCache = null;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * Find a jobItem by name
	 * 
	 * @param jobName
	 * @return
	 */
	public JobItem get(String jobName) {
		rwl.r.lock();
		try {
			return jobs.get(jobName);
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * Remove the jobItem by the name
	 * 
	 * @param jobName
	 */
	public void remove(String jobName) {
		rwl.w.lock();
		try {
			jobs.remove(jobName);
			jobsCache = null;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * Return an array of the jobItem in the jobList
	 * 
	 * @return
	 */
	public JobItem[] getJobs() {
		rwl.r.lock();
		try {
			if (jobsCache != null)
				return jobsCache;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			if (jobsCache != null)
				return jobsCache;
			jobsCache = new JobItem[jobs.size()];
			int i = 0;
			for (JobItem job : jobs.values())
				jobsCache[i++] = job;
			return jobsCache;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * Create a list of job name
	 * 
	 * @param list
	 */
	public void populateNameList(List<String> list) {
		rwl.r.lock();
		try {
			for (JobItem job : jobs.values())
				list.add(job.getName());
		} finally {
			rwl.r.unlock();
		}
	}

	public int getActiveCount() {
		rwl.r.lock();
		try {
			int c = 0;
			for (JobItem job : jobs.values())
				if (job.isActive())
					c++;
			return c;
		} finally {
			rwl.r.unlock();
		}
	}

	public int getCount() {
		rwl.r.lock();
		try {
			return jobs.size();
		} finally {
			rwl.r.unlock();
		}
	}

	private final static String JOBS_ROOTNODE_NAME = "jobs";

	/**
	 * Build a job list from an XML file
	 * 
	 * @param config
	 * @param file
	 * @return
	 * @throws XPathExpressionException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static JobList fromXml(Config config, File file)
			throws XPathExpressionException, ParserConfigurationException,
			SAXException, IOException {
		JobList jobList = new JobList(config);
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

	/**
	 * Write the job list in XML
	 * 
	 * @param xmlWriter
	 * @throws SAXException
	 * @throws UnsupportedEncodingException
	 */
	public void writeXml(XmlWriter xmlWriter) throws SAXException,
			UnsupportedEncodingException {
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

	public void checkExecution() throws SearchLibException {
		rwl.r.lock();
		try {
			for (JobItem job : jobs.values())
				job.checkTaskExecution(config);

			// Remove non existing job in the scheduler
			String indexName = config.getIndexName();
			String[] jobNames = TaskManager.getActiveJobs(indexName);
			if (jobNames != null)
				for (String jobName : jobNames) {
					JobItem jobItem = jobs.get(jobName);
					if (jobItem != null)
						if (!jobItem.isActive())
							jobItem = null;
					if (jobItem == null)
						TaskManager.removeJob(indexName, jobName);
				}
		} finally {
			rwl.r.unlock();
		}
	}

	public int getRunningCount() throws SearchLibException {
		rwl.r.lock();
		try {
			int c = 0;
			for (JobItem job : jobs.values())
				if (job.isRunning())
					c++;
			return c;
		} finally {
			rwl.r.unlock();
		}
	}
}
