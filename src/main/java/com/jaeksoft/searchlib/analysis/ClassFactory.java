/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.analysis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringEscapeUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.XmlWriter;

public abstract class ClassFactory {

	protected Config config;

	protected Map<ClassPropertyEnum, ClassProperty> properties;

	protected List<ClassProperty> userProperties;

	protected String packageName;

	public ClassFactory() {
		config = null;
		properties = new TreeMap<ClassPropertyEnum, ClassProperty>();
		userProperties = null;
		addProperty(ClassPropertyEnum.CLASS, null, null, 0, 0);
		packageName = null;
	}

	final public ClassProperty addProperty(ClassPropertyEnum classPropertyEnum,
			String defaultValue, Object[] valueList, int cols, int rows) {
		ClassProperty classProperty = properties.get(classPropertyEnum);
		if (classProperty != null)
			return classProperty;
		classProperty = new ClassProperty(this, classPropertyEnum,
				defaultValue, valueList, cols, rows);
		properties.put(classPropertyEnum, classProperty);
		if (classPropertyEnum.isUser()) {
			if (userProperties == null)
				userProperties = new ArrayList<ClassProperty>();
			userProperties.add(classProperty);
		}
		return classProperty;
	}

	/**
	 * 
	 * @param config
	 * @param packageName
	 * @param className
	 * @param properties
	 * @throws IOException
	 * @throws SearchLibException
	 */
	public void setParams(Config config, String packageName, String className)
			throws IOException, SearchLibException {
		this.config = config;
		this.packageName = packageName;
		getProperty(ClassPropertyEnum.CLASS).setValue(className);
	}

	protected void initProperties() throws SearchLibException {
	}

	protected void checkValue(ClassPropertyEnum prop, String value)
			throws SearchLibException {
	}

	public void checkProperties() throws SearchLibException {
		for (ClassProperty prop : properties.values())
			checkValue(prop.getClassPropertyEnum(), prop.getValue());
	}

	public ClassProperty getProperty(ClassPropertyEnum prop) {
		return properties.get(prop);
	}

	protected float getFloatProperty(ClassPropertyEnum prop) {
		ClassProperty cp = getProperty(prop);
		if (cp == null)
			return 1.0F;
		String value = cp.getValue();
		if (value == null)
			return 1.0F;
		return Float.parseFloat(value);
	}

	protected boolean getBooleanProperty(ClassPropertyEnum prop) {
		ClassProperty cp = getProperty(prop);
		if (cp == null)
			return false;
		String value = cp.getValue();
		if (value == null)
			return false;
		return Boolean.parseBoolean(value);
	}

	protected String getStringProperty(ClassPropertyEnum prop) {
		ClassProperty cp = getProperty(prop);
		if (cp == null)
			return null;
		return cp.getValue();
	}

	final private void addProperty(final String name, final String value)
			throws SearchLibException {
		ClassPropertyEnum propEnum = ClassPropertyEnum.valueOf(name);
		if (propEnum != null) {
			ClassProperty prop = getProperty(propEnum);
			if (prop != null)
				prop.setValue(value);
		} else {
			Logging.warn("Property not found: " + name);
		}
	}

	final protected void addProperties(final NamedNodeMap nnm)
			throws SearchLibException {
		if (nnm == null)
			return;
		int l = nnm.getLength();
		for (int i = 0; i < l; i++) {
			Node attr = nnm.item(i);
			addProperty(attr.getNodeName(),
					StringEscapeUtils.unescapeXml(attr.getNodeValue()));
		}
	}

	/**
	 * 
	 * @return
	 */
	public String getClassName() {
		return getProperty(ClassPropertyEnum.CLASS).getValue();
	}

	/**
	 * Return the class name and properties
	 * 
	 * @return a string array
	 */
	public String[] getXmlAttributes() {
		String[] attributes = new String[properties.size() * 2];
		int i = 0;
		for (ClassProperty prop : properties.values()) {
			if (!prop.isMultilinetextbox()) {
				attributes[i++] = prop.getClassPropertyEnum().getAttribute();
				attributes[i++] = prop.getValue();
			}
		}
		return attributes;
	}

	public void writeXmlNodeAttributes(XmlWriter writer, String nodeName)
			throws SAXException {
		Map<String, String> map = new HashMap<String, String>();
		for (ClassProperty prop : properties.values()) {
			if (prop.isMultilinetextbox())
				map.put(prop.getClassPropertyEnum().getAttribute(),
						prop.getValue());
		}
		if (map.isEmpty())
			return;
		writer.startElement(nodeName);
		for (Map.Entry<String, String> entry : map.entrySet()) {
			writer.startElement("attribute", "name", entry.getKey());
			writer.textNode(entry.getValue());
			writer.endElement();
		}
		writer.endElement();
	}

	/**
	 * 
	 * @param config
	 * @param packageName
	 * @param className
	 * @return
	 * @throws SearchLibException
	 * @throws ClassNotFoundException
	 */
	protected static ClassFactory create(Config config, String packageName,
			String className) throws SearchLibException, ClassNotFoundException {
		String cl = className;
		if (className.indexOf('.') == -1)
			cl = packageName + '.' + cl;
		try {
			ClassFactory o = (ClassFactory) Class.forName(cl).newInstance();
			o.setParams(config, packageName, className);
			o.initProperties();
			return o;
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		}
	}

	/**
	 * 
	 * @param config
	 * @param factoryClass
	 * @return
	 * @throws SearchLibException
	 */
	protected static <T extends ClassFactory> T createInstance(Config config,
			Class<T> factoryClass) throws SearchLibException {
		try {
			T o = factoryClass.newInstance();
			o.setParams(config, factoryClass.getPackage().getName(),
					factoryClass.getSimpleName());
			o.initProperties();
			return o;
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		}
	}

	/**
	 * 
	 * @param classFactory
	 * @return
	 * @throws SearchLibException
	 * @throws ClassNotFoundException
	 * @throws DOMException
	 */
	protected static ClassFactory create(Config config, String packageName,
			Node node, String attributeNodeName) throws SearchLibException,
			DOMException, ClassNotFoundException {
		if (node == null)
			return null;
		NamedNodeMap nnm = node.getAttributes();
		if (nnm == null)
			return null;
		Node classNode = nnm.getNamedItem(ClassPropertyEnum.CLASS
				.getAttribute());
		if (classNode == null)
			return null;
		ClassFactory newClassFactory = create(config, packageName,
				classNode.getNodeValue());
		newClassFactory.addProperties(nnm);
		List<Node> attrNodes = DomUtils.getNodes(node, attributeNodeName,
				"attribute");
		if (attrNodes != null)
			for (Node attrNode : attrNodes)
				newClassFactory.addProperty(
						DomUtils.getAttributeText(attrNode, "name"),
						attrNode.getTextContent());
		return newClassFactory;
	}

	/**
	 * 
	 * @param classFactory
	 * @return
	 * @throws SearchLibException
	 * @throws ClassNotFoundException
	 */
	protected static ClassFactory create(ClassFactory classFactory)
			throws SearchLibException, ClassNotFoundException {
		ClassFactory newClassFactory = create(classFactory.config,
				classFactory.packageName, classFactory.getClassName());
		for (ClassProperty prop : classFactory.properties.values())
			newClassFactory.getProperty(prop.getClassPropertyEnum()).setValue(
					prop.getValue());
		return newClassFactory;
	}

	public boolean isProperty() {
		if (userProperties == null)
			return false;
		return userProperties.size() > 0;
	}

	public List<ClassProperty> getUserProperties() {
		return userProperties;
	}

	public void setUserProperty(String name, String value)
			throws SearchLibException {
		if (userProperties == null || name == null)
			throw new SearchLibException("No properties");
		for (ClassProperty prop : userProperties) {
			if (name.equals(prop.getClassPropertyEnum().getName())) {
				prop.setValue(value);
				return;
			}
		}
		throw new SearchLibException("Property " + name + " not found.");
	}
}
