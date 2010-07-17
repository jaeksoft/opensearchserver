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

package com.jaeksoft.searchlib.analysis;

import java.io.IOException;
import java.util.Properties;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;

public abstract class ClassFactory {

	protected Config config;

	protected Properties properties;

	protected String packageName;

	protected String className;

	public ClassFactory() {
		config = null;
		properties = null;
		className = null;
		packageName = null;
	}

	/**
	 * 
	 * @param config
	 * @param packageName
	 * @param className
	 * @param properties
	 * @throws IOException
	 */
	public void setParams(Config config, String packageName, String className)
			throws IOException {
		this.config = config;
		this.packageName = packageName;
		this.className = className;
	}

	public void setProperty(String key, String value) throws SearchLibException {
		if (properties == null)
			properties = new Properties();
		properties.setProperty(key, value);
	}

	public void setProperties(Properties props) {
		this.properties = new Properties(props);
	}

	/**
	 * 
	 * @return
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * Return the class name and properties
	 * 
	 * @return a string array
	 */
	public String[] getAttributes() {
		String[] attributes = new String[1 + getPropertyList().length];
		int i = 0;
		attributes[i++] = "class";
		attributes[i++] = className;
		for (String a : getPropertyList()) {
			attributes[i++] = a;
			attributes[i++] = properties.getProperty(a);
		}
		return attributes;
	}

	/**
	 * 
	 * @param config
	 * @param packageName
	 * @param className
	 * @param properties
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
	protected static ClassFactory create(ClassFactory classFactory)
			throws SearchLibException {
		ClassFactory newClassFactory = create(classFactory.config,
				classFactory.packageName, classFactory.className);
		if (classFactory.properties != null)
			newClassFactory.properties = new Properties(classFactory.properties);
		return newClassFactory;
	}

	private final static String[] EMPTY_PROP_LIST = {};

	public String[] getPropertyList() {
		return EMPTY_PROP_LIST;
	}
}
