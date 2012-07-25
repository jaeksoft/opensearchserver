/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2011 Emmanuel Keller / Jaeksoft
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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringEscapeUtils;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;

public abstract class ClassFactory {

	protected Config config;

	protected Map<ClassPropertyEnum, ClassProperty> properties;

	protected List<ClassProperty> userProperties;

	protected String packageName;

	public ClassFactory() {
		config = null;
		properties = new TreeMap<ClassPropertyEnum, ClassProperty>();
		userProperties = null;
		addProperty(ClassPropertyEnum.CLASS, null, null);
		packageName = null;
	}

	final public ClassProperty addProperty(ClassPropertyEnum classPropertyEnum,
			String defaultValue, Object[] valueList) {
		ClassProperty classProperty = properties.get(classPropertyEnum);
		if (classProperty != null)
			return classProperty;
		classProperty = new ClassProperty(this, classPropertyEnum,
				defaultValue, valueList);
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

	protected ClassProperty getProperty(ClassPropertyEnum prop) {
		return properties.get(prop);
	}

	protected float getFloatProperty(ClassPropertyEnum prop) {
		String value = getProperty(prop).getValue();
		if (value == null)
			return 1.0F;
		return Float.parseFloat(value);
	}

	protected boolean getBooleanProperty(ClassPropertyEnum prop) {
		String value = getProperty(prop).getValue();
		if (value == null)
			return false;
		return Boolean.parseBoolean(value);
	}

	final protected void addProperties(NamedNodeMap nnm)
			throws SearchLibException {
		if (nnm == null)
			return;
		int l = nnm.getLength();
		for (int i = 0; i < l; i++) {
			Node attr = nnm.item(i);
			ClassPropertyEnum propEnum = ClassPropertyEnum.valueOf(attr
					.getNodeName());
			if (propEnum != null) {
				ClassProperty prop = getProperty(propEnum);
				if (prop != null)
					prop.setValue(StringEscapeUtils.unescapeXml(attr
							.getNodeValue()));
			} else {
				Logging.warn("Property not found: " + attr.getNodeName());
			}
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
	public String[] getAttributes() {
		String[] attributes = new String[properties.size() * 2];
		int i = 0;
		for (ClassProperty prop : properties.values()) {
			attributes[i++] = prop.getClassPropertyEnum().getAttribute();
			attributes[i++] = prop.getValue();
		}
		return attributes;
	}

	/**
	 * 
	 * @param config
	 * @param packageName
	 * @param className
	 * @return
	 * @throws SearchLibException
	 */
	protected static ClassFactory create(Config config, String packageName,
			String className) throws SearchLibException {
		try {
			String cl = className;
			if (className.indexOf('.') == -1)
				cl = packageName + '.' + cl;
			ClassFactory o = (ClassFactory) Class.forName(cl).newInstance();
			o.setParams(config, packageName, className);
			o.initProperties();
			return o;
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		} catch (ClassNotFoundException e) {
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
	 */
	protected static ClassFactory create(Config config, String packageName,
			Node node) throws SearchLibException {
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
		return newClassFactory;
	}

	/**
	 * 
	 * @param classFactory
	 * @return
	 * @throws SearchLibException
	 */
	protected static ClassFactory create(ClassFactory classFactory)
			throws SearchLibException {
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

}
