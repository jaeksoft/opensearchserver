/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2012 Emmanuel Keller / Jaeksoft
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
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.FilenameUtils;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.index.UpdateInterfaces;
import com.jaeksoft.searchlib.util.InfoCallback;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.XmlWriter;

public class LearnerManager implements UpdateInterfaces.After,
		UpdateInterfaces.Delete {

	private ReadWriteLock rwl = new ReadWriteLock();

	private TreeMap<String, Learner> learnerMap;

	private Learner[] learnerArray;

	private Learner[] activeLearnerArray;

	private Client client;

	public LearnerManager(Client client, File directory)
			throws XPathExpressionException, SearchLibException,
			ParserConfigurationException, SAXException, IOException {
		this.client = client;
		learnerArray = null;
		activeLearnerArray = null;
		learnerMap = new TreeMap<String, Learner>();
		for (File f : directory.listFiles())
			if (f.isFile()) {
				String fname = f.getName();
				if (!FilenameUtils.isExtension(fname, "xml"))
					continue;
				if (fname.endsWith("_old.xml"))
					continue;
				if (fname.endsWith("_tmp.xml"))
					continue;
				add(new Learner(client, f));
			}
	}

	private void buildLearnerArray() {
		learnerArray = new Learner[learnerMap.size()];
		learnerMap.values().toArray(learnerArray);
		List<Learner> activeList = new ArrayList<Learner>(0);
		for (Learner learner : learnerArray)
			if (learner.isActive())
				activeList.add(learner);
		activeLearnerArray = new Learner[activeList.size()];
		activeList.toArray(activeLearnerArray);
	}

	public Learner[] getArray() {
		rwl.r.lock();
		try {
			return learnerArray;
		} finally {
			rwl.r.unlock();
		}
	}

	public Learner[] getActiveArray() {
		rwl.r.lock();
		try {
			return activeLearnerArray;
		} finally {
			rwl.r.unlock();
		}
	}

	public void add(Learner item) throws SearchLibException, IOException {
		rwl.w.lock();
		try {
			if (learnerMap.get(item.getName()) != null)
				throw new SearchLibException("This item already exists");
			learnerMap.put(item.getName(), item);
			buildLearnerArray();
			client.saveLearner(item);
		} finally {
			rwl.w.unlock();
		}
	}

	public void set(Learner item) throws SearchLibException, IOException {
		rwl.w.lock();
		try {
			if (learnerMap.get(item.getName()) == null)
				throw new SearchLibException("This item does not exist");
			learnerMap.put(item.getName(), item);
			buildLearnerArray();
			client.saveLearner(item);
		} finally {
			rwl.w.unlock();
		}
	}

	public void remove(String name) throws SearchLibException, IOException {
		rwl.w.lock();
		try {
			Learner learner = learnerMap.remove(name);
			buildLearnerArray();
			if (learner != null) {
				learner.reset();
				client.deleteLearner(learner);
			}
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * Write the learner list in XML
	 * 
	 * @param xmlWriter
	 * @throws SAXException
	 */
	public void writeXml(XmlWriter xmlWriter) throws SAXException {
		rwl.r.lock();
		try {
			xmlWriter.startElement("learner");
			for (Learner learner : learnerMap.values())
				learner.writeXml(xmlWriter);
			xmlWriter.endElement();
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public void update(IndexDocument document) throws SearchLibException {
		Learner[] learners = getActiveArray();
		if (learners == null)
			return;
		List<IndexDocument> documents = new ArrayList<IndexDocument>(1);
		documents.add(document);
		for (Learner learner : learners)
			learner.learn(documents);
	}

	@Override
	public void update(Collection<IndexDocument> documents)
			throws SearchLibException {
		Learner[] learners = getActiveArray();
		if (learners == null)
			return;
		for (Learner learner : learners)
			learner.learn(documents);
	}

	@Override
	public void delete(String field, String value) throws SearchLibException {
		Learner[] learners = getActiveArray();
		if (learners == null)
			return;
		List<String> values = new ArrayList<String>(1);
		values.add(value);
		for (Learner learner : learners)
			learner.remove(field, values);
	}

	@Override
	public void delete(String field, Collection<String> values)
			throws SearchLibException {
		Learner[] learners = getActiveArray();
		if (learners == null)
			return;
		for (Learner learner : learners)
			learner.remove(field, values);
	}

	public Learner get(String name) {
		rwl.r.lock();
		try {
			return learnerMap.get(name);
		} finally {
			rwl.r.unlock();
		}
	}

	public void learn(String learnerName, InfoCallback callback)
			throws SearchLibException {
		Learner learner = get(learnerName);
		if (learner == null)
			throw new SearchLibException("Learner not found: " + learnerName);
		learner.learn(callback);
	}

	public void reset(String learnerName) throws SearchLibException,
			IOException {
		Learner learner = get(learnerName);
		if (learner == null)
			throw new SearchLibException("Learner not found: " + learnerName);
		learner.reset();
	}

}
