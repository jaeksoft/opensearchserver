/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2013 Emmanuel Keller / Jaeksoft
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
import java.util.List;
import java.util.TreeSet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPathExpressionException;

import org.apache.cxf.helpers.DOMUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.memory.MemoryIndex;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.FieldContent;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.FormatUtils.ThreadSafeDecimalFormat;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class Classifier implements Comparable<Classifier> {

	private final static String CLASSIFIER_ITEM_ROOTNODE_NAME = "classifier";
	private final static String CLASSIFIER_ITEM_NODE_NAME = "classifierItem";
	private final static String CLASSIFIER_ITEM_ROOT_ATTR_NAME = "name";
	private final static String CLASSIFIER_ITEM_ROOT_ATTR_FIELD = "field";
	private final static String CLASSIFIER_ITEM_ROOT_ATTR_SCOREFIELD = "scoreField";
	private final static String CLASSIFIER_ITEM_ROOT_ATTR_ACTIVE = "active";
	private final static String CLASSIFIER_ITEM_ROOT_ATTR_METHOD = "method";
	private final static String CLASSIFIER_ITEM_DEFAULT_VALUE_NODE = "defaultValue";

	private final ReadWriteLock rwl = new ReadWriteLock();

	private String name;

	private String fieldName;

	private String scoreFieldName;

	private boolean active;

	private String defaultValue;

	private TreeSet<ClassifierItem> valueSet;

	private ClassifierItem[] valueSetArray;

	private ClassificationMethodEnum method;

	public Classifier() {
		valueSetArray = null;
		valueSet = new TreeSet<ClassifierItem>();
		name = null;
		fieldName = null;
		scoreFieldName = null;
		active = false;
		defaultValue = null;
		method = ClassificationMethodEnum.BESTSCORE;
	}

	public Classifier(Classifier source) {
		this();
		source.copyTo(this);
	}

	public void copyTo(Classifier target) {
		rwl.r.lock();
		try {
			target.rwl.w.lock();
			try {
				target.name = name;
				target.fieldName = fieldName;
				target.scoreFieldName = scoreFieldName;
				target.active = active;
				target.method = method;
				target.valueSetArray = null;
				target.defaultValue = defaultValue;
				target.valueSet.clear();
				for (ClassifierItem item : valueSet)
					target.valueSet.add(new ClassifierItem(item));
				target.buildValueSetArray();
			} finally {
				target.rwl.w.unlock();
			}
		} finally {
			rwl.r.unlock();
		}
	}

	protected Classifier(File file) throws ParserConfigurationException,
			SAXException, IOException, XPathExpressionException,
			SearchLibException {
		this();
		if (!file.exists())
			return;
		Document document = DOMUtils.readXml(new StreamSource(file));
		Node rootNode = DomUtils.getFirstNode(document,
				CLASSIFIER_ITEM_ROOTNODE_NAME);
		if (rootNode == null)
			return;
		setName(XPathParser.getAttributeString(rootNode,
				CLASSIFIER_ITEM_ROOT_ATTR_NAME));
		setFieldName(XPathParser.getAttributeString(rootNode,
				CLASSIFIER_ITEM_ROOT_ATTR_FIELD));
		setScoreFieldName(XPathParser.getAttributeString(rootNode,
				CLASSIFIER_ITEM_ROOT_ATTR_SCOREFIELD));
		setActive("yes".equalsIgnoreCase(XPathParser.getAttributeString(
				rootNode, CLASSIFIER_ITEM_ROOT_ATTR_ACTIVE)));
		setMethod(ClassificationMethodEnum.find(XPathParser.getAttributeString(
				rootNode, CLASSIFIER_ITEM_ROOT_ATTR_METHOD)));
		Node defaultValueNode = DomUtils.getFirstNode(rootNode,
				CLASSIFIER_ITEM_DEFAULT_VALUE_NODE);
		if (defaultValueNode != null)
			setDefaultValue(defaultValueNode.getTextContent());
		List<Node> nodes = DomUtils.getNodes(rootNode,
				CLASSIFIER_ITEM_NODE_NAME);
		if (nodes == null)
			return;
		for (Node n : nodes)
			addNoLock(new ClassifierItem(n));
		buildValueSetArray();
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	public ClassifierItem[] getValueSet() {
		rwl.r.lock();
		try {
			return valueSetArray;
		} finally {
			rwl.r.unlock();
		}
	}

	public int getValueSetSize() {
		rwl.r.lock();
		try {
			return valueSetArray == null ? 0 : valueSetArray.length;
		} finally {
			rwl.r.unlock();
		}
	}

	private final void buildValueSetArray() {
		valueSetArray = new ClassifierItem[valueSet.size()];
		valueSet.toArray(valueSetArray);
	}

	private final void addNoLock(ClassifierItem item) {
		valueSet.add(item);
	}

	public void add(ClassifierItem item) throws SearchLibException {
		rwl.w.lock();
		try {
			addNoLock(item);
			buildValueSetArray();
		} finally {
			rwl.w.unlock();
		}
	}

	public void replace(ClassifierItem oldItem, ClassifierItem newItem) {
		rwl.w.lock();
		try {
			valueSet.remove(oldItem);
			valueSet.add(newItem);
			buildValueSetArray();
		} finally {
			rwl.w.unlock();
		}
	}

	public void remove(ClassifierItem item) {
		rwl.w.lock();
		try {
			valueSet.remove(item);
			buildValueSetArray();
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @param fieldName
	 *            the fieldName to set
	 */
	public void setFieldName(String fieldName) {
		rwl.w.lock();
		try {
			this.fieldName = fieldName;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the fieldName
	 */
	public String getFieldName() {
		return fieldName;
	}

	/**
	 * @param scoreFieldName
	 *            the scoreFieldName to set
	 */
	public void setScoreFieldName(String scoreFieldName) {
		rwl.w.lock();
		try {
			this.scoreFieldName = scoreFieldName;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the scoreFieldName
	 */
	public String getScoreFieldName() {
		return scoreFieldName;
	}

	/**
	 * @param active
	 *            the active to set
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * @return the active
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * @param method
	 *            the method to set
	 */
	public void setMethod(ClassificationMethodEnum method) {
		this.method = method;
	}

	/**
	 * @return the method
	 */
	public ClassificationMethodEnum getMethod() {
		return method;
	}

	@Override
	public int compareTo(Classifier o) {
		return name.compareTo(o.name);
	}

	public void writeXml(XmlWriter xmlWriter) throws SAXException {
		rwl.r.lock();
		try {
			xmlWriter.startElement(CLASSIFIER_ITEM_ROOTNODE_NAME,
					CLASSIFIER_ITEM_ROOT_ATTR_NAME, name,
					CLASSIFIER_ITEM_ROOT_ATTR_FIELD, fieldName,
					CLASSIFIER_ITEM_ROOT_ATTR_SCOREFIELD, scoreFieldName,
					CLASSIFIER_ITEM_ROOT_ATTR_ACTIVE, active ? "yes" : "no",
					CLASSIFIER_ITEM_ROOT_ATTR_METHOD, method.name());
			if (defaultValue != null && defaultValue.length() > 0) {
				xmlWriter.startElement(CLASSIFIER_ITEM_DEFAULT_VALUE_NODE);
				xmlWriter.textNode(defaultValue);
				xmlWriter.endElement();
			}
			for (ClassifierItem item : valueSet)
				item.writeXml(xmlWriter, CLASSIFIER_ITEM_NODE_NAME);
			xmlWriter.endElement();
		} finally {
			rwl.r.unlock();
		}
	}

	private void multivaluedClassification(Client client,
			IndexDocument document, LanguageEnum lang, MemoryIndex index)
			throws ParseException, SearchLibException, SyntaxError, IOException {
		boolean setDefaultValue = defaultValue != null
				&& defaultValue.length() > 0;
		for (ClassifierItem item : valueSet) {
			float score = item.score(client, lang, index);
			if (score > 0.0f) {
				document.add(fieldName, item.getValue(), item.getBoost());
				if (scoreFieldName != null && scoreFieldName.length() > 0)
					document.addString(scoreFieldName, Float.toString(score));
				setDefaultValue = false;
			}
		}
		if (setDefaultValue)
			document.add(fieldName, defaultValue, 1.0F);
	}

	private final static ThreadSafeDecimalFormat scoreFormat = new ThreadSafeDecimalFormat(
			"0.###########");

	private void bestScoreClassification(Client client, IndexDocument document,
			LanguageEnum lang, MemoryIndex index) throws ParseException,
			SearchLibException, SyntaxError, IOException {
		ClassifierItem selectedItem = null;
		float maxScore = 0;
		for (ClassifierItem item : valueSet) {
			float score = item.score(client, lang, index);
			if (score > maxScore) {
				selectedItem = item;
				maxScore = score;
			}
		}
		if (selectedItem != null) {
			document.add(getFieldName(), selectedItem.getValue(),
					selectedItem.getBoost());
			if (scoreFieldName != null && scoreFieldName.length() > 0) {
				document.addString(scoreFieldName, scoreFormat.format(maxScore));
			}
		} else {
			if (defaultValue != null && defaultValue.length() > 0)
				document.add(fieldName, defaultValue, 1.0F);

		}
	}

	public void classification(Client client, IndexDocument document)
			throws SearchLibException, ParseException, SyntaxError, IOException {
		rwl.r.lock();
		try {
			MemoryIndex index = new MemoryIndex();
			LanguageEnum lang = document.getLang();
			Analyzer analyzer = client.getSchema().getIndexPerFieldAnalyzer(
					lang);
			for (FieldContent fieldContent : document.getFieldContentArray()) {
				String fieldName = fieldContent.getField();
				String concatValues = fieldContent.getMergedValues(" ");
				index.addField(fieldName, concatValues, analyzer);
			}
			if (method == ClassificationMethodEnum.MULTIVALUED)
				multivaluedClassification(client, document, lang, index);
			else if (method == ClassificationMethodEnum.BESTSCORE)
				bestScoreClassification(client, document, lang, index);

		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @return the defaultValue
	 */
	public String getDefaultValue() {
		rwl.r.lock();
		try {
			return defaultValue;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param defaultValue
	 *            the defaultValue to set
	 */
	public void setDefaultValue(String defaultValue) {
		rwl.w.lock();
		try {
			this.defaultValue = defaultValue;
		} finally {
			rwl.w.unlock();
		}
	}
}
