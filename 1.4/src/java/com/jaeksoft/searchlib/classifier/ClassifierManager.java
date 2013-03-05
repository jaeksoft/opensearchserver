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

package com.jaeksoft.searchlib.classifier;

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
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.BeforeUpdateInterface;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.XmlWriter;

public class ClassifierManager implements BeforeUpdateInterface {

	private ReadWriteLock rwl = new ReadWriteLock();

	private TreeSet<Classifier> classifierSet;

	private Classifier[] classifierArray;

	private Classifier[] activeClassifierArray;

	private Client client;

	public ClassifierManager(Client client, File directory)
			throws XPathExpressionException, SearchLibException,
			ParserConfigurationException, SAXException, IOException {
		this.client = client;
		classifierArray = null;
		activeClassifierArray = null;
		classifierSet = new TreeSet<Classifier>();
		for (File f : directory.listFiles())
			if (f.isFile()) {
				String fname = f.getName();
				if (!FilenameUtils.isExtension(fname, "xml"))
					continue;
				if (fname.endsWith("_old.xml"))
					continue;
				if (fname.endsWith("_tmp.xml"))
					continue;
				add(new Classifier(f));
			}
	}

	private void buildClassifieArray() {
		classifierArray = new Classifier[classifierSet.size()];
		classifierSet.toArray(classifierArray);
		List<Classifier> activeList = new ArrayList<Classifier>();
		for (Classifier cl : classifierArray)
			if (cl.isActive())
				activeList.add(cl);
		activeClassifierArray = new Classifier[activeList.size()];
		activeList.toArray(activeClassifierArray);
	}

	public Classifier[] getArray() {
		rwl.r.lock();
		try {
			return classifierArray;
		} finally {
			rwl.r.unlock();
		}
	}

	public void add(Classifier item) throws SearchLibException {
		rwl.w.lock();
		try {
			if (classifierSet.contains(item))
				throw new SearchLibException("This item already exists");
			classifierSet.add(item);
			buildClassifieArray();
		} finally {
			rwl.w.unlock();
		}
	}

	public void replace(Classifier oldItem, Classifier newItem) {
		rwl.w.lock();
		try {
			classifierSet.remove(oldItem);
			classifierSet.add(newItem);
			buildClassifieArray();
		} finally {
			rwl.w.unlock();
		}
	}

	public void remove(Classifier item) {
		rwl.w.lock();
		try {
			classifierSet.remove(item);
			buildClassifieArray();
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * Write the classifier list in XML
	 * 
	 * @param xmlWriter
	 * @throws SAXException
	 */
	public void writeXml(XmlWriter xmlWriter) throws SAXException {
		rwl.r.lock();
		try {
			xmlWriter.startElement("classifier");
			for (Classifier classifier : classifierSet)
				classifier.writeXml(xmlWriter);
			xmlWriter.endElement();
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public void update(Schema schema, IndexDocument document)
			throws SearchLibException {
		if (classifierArray == null)
			return;
		try {
			for (Classifier classifier : activeClassifierArray)
				classifier.classification(client, document);
		} catch (ParseException e) {
			throw new SearchLibException(e);
		} catch (SyntaxError e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		}

	}

}
