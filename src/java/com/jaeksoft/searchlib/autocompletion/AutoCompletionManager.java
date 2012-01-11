/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.autocompletion;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import org.apache.poi.util.IOUtils;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.util.ReadWriteLock;

public class AutoCompletionManager {

	private final ReadWriteLock rwl = new ReadWriteLock();

	protected Client autoCompClient;

	protected Client targetClient;

	private Properties properties;

	private File propFile;

	private final static String autoCompletionProperties = "autocompletion-properties.xml";
	private final static String autoCompletionSubDirectory = "autocompletion";
	private final static String autoCompletionConfigPath = "/autocompletion_config.xml";
	private final static String autoCompletionPropertyField = "field";

	public AutoCompletionManager(Config config) throws SearchLibException,
			InvalidPropertiesFormatException, IOException {
		File subDir = new File(config.getDirectory(),
				autoCompletionSubDirectory);
		if (!subDir.exists())
			subDir.mkdir();
		this.autoCompClient = new Client(subDir, autoCompletionConfigPath, true);
		this.targetClient = (Client) config;
		propFile = new File(config.getDirectory(), autoCompletionProperties);
		properties = new Properties();
		if (propFile.exists()) {
			FileInputStream inputStream = null;
			try {
				inputStream = new FileInputStream(propFile);
				properties.loadFromXML(inputStream);
			} finally {
				if (inputStream != null)
					IOUtils.closeQuietly(inputStream);
			}
		}
	}

	private void saveProperties() throws IOException {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(propFile);
			properties.storeToXML(fos, "");
		} finally {
			if (fos != null)
				IOUtils.closeQuietly(fos);
		}
	}

	public String getField() {
		rwl.r.lock();
		try {
			return properties.getProperty(autoCompletionPropertyField);
		} finally {
			rwl.r.unlock();
		}
	}

	public void setField(String field) throws SearchLibException {
		rwl.w.lock();
		try {
			properties.setProperty(autoCompletionPropertyField, field);
			saveProperties();
		} catch (IOException e) {
			throw new SearchLibException(e);
		} finally {
			rwl.w.unlock();
		}
	}

	public void close() {
		rwl.w.lock();
		try {
			this.autoCompClient.close();
		} finally {
			rwl.w.unlock();
		}
	}

	public void build() {
		// TODO Auto-generated method stub

	}

}
