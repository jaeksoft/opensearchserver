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
	public void setParams(Config config, String packageName, String className,
			Properties properties) throws IOException {
		this.config = config;
		this.packageName = packageName;
		this.className = className;
		this.properties = properties;
	}

	/**
	 * 
	 * @return
	 */
	public String getClassName() {
		return className;
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
			String className, Properties properties) throws SearchLibException {
		try {
			String cl = className;
			if (className.indexOf('.') == -1)
				cl = packageName + '.' + cl;
			if (properties == null)
				properties = new Properties();
			else
				properties = new Properties(properties);
			ClassFactory o = (ClassFactory) Class.forName(cl).newInstance();
			o.setParams(config, packageName, className, properties);
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
		return create(classFactory.config, classFactory.packageName,
				classFactory.className, classFactory.properties);
	}
}
