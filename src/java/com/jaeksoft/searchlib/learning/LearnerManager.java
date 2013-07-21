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
import java.util.List;
import java.util.TreeSet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.FilenameUtils;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.index.BeforeUpdateInterface;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.XmlWriter;

public class LearnerManager implements BeforeUpdateInterface {

	private ReadWriteLock rwl = new ReadWriteLock();

	private TreeSet<Learner> learnerSet;

	private Learner[] learnerArray;

	private Learner[] activeLearnerArray;

	private Client client;

	public LearnerManager(Client client, File directory)
			throws XPathExpressionException, SearchLibException,
			ParserConfigurationException, SAXException, IOException {
		this.client = client;
		learnerArray = null;
		activeLearnerArray = null;
		learnerSet = new TreeSet<Learner>();
		for (File f : directory.listFiles())
			if (f.isFile()) {
				String fname = f.getName();
				if (!FilenameUtils.isExtension(fname, "xml"))
					continue;
				if (fname.endsWith("_old.xml"))
					continue;
				if (fname.endsWith("_tmp.xml"))
					continue;
				add(new Learner(f));
			}
	}

	private void buildLearnerArray() {
		learnerArray = new Learner[learnerSet.size()];
		learnerSet.toArray(learnerArray);
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

	public void add(Learner item) throws SearchLibException {
		rwl.w.lock();
		try {
			if (learnerSet.contains(item))
				throw new SearchLibException("This item already exists");
			learnerSet.add(item);
			buildLearnerArray();
		} finally {
			rwl.w.unlock();
		}
	}

	public void replace(Learner oldItem, Learner newItem) {
		rwl.w.lock();
		try {
			learnerSet.remove(oldItem);
			learnerSet.add(newItem);
			buildLearnerArray();
		} finally {
			rwl.w.unlock();
		}
	}

	public void remove(Learner item) {
		rwl.w.lock();
		try {
			learnerSet.remove(item);
			buildLearnerArray();
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
			for (Learner learner : learnerSet)
				learner.writeXml(xmlWriter);
			xmlWriter.endElement();
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public void update(Schema schema, IndexDocument document)
			throws SearchLibException {
		if (learnerArray == null)
			return;
		for (Learner learner : activeLearnerArray)
			learner.learn(client, document);
	}

}
