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

package com.jaeksoft.searchlib.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import org.apache.poi.util.IOUtils;

public class PropertiesUtils {

	public static void loadFromXml(File propFile, Properties properties)
			throws InvalidPropertiesFormatException, IOException {
		if (!propFile.exists())
			return;
		FileInputStream inputStream = null;
		try {
			inputStream = new FileInputStream(propFile);
			properties.loadFromXML(inputStream);
		} finally {
			if (inputStream != null)
				IOUtils.closeQuietly(inputStream);
		}
	}

	public static Properties loadFromXml(File propFile)
			throws InvalidPropertiesFormatException, IOException {
		Properties properties = new Properties();
		loadFromXml(propFile, properties);
		return properties;
	}

	public static void storeToXml(Properties properties, File propFile)
			throws IOException {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(propFile);
			properties.storeToXML(fos, "");
		} finally {
			if (fos != null)
				IOUtils.closeQuietly(fos);
		}
	}

}
