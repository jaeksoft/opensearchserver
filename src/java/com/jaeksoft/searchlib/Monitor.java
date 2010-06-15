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

package com.jaeksoft.searchlib;

import java.io.IOException;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.io.FileSystemUtils;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.util.XmlWriter;

public class Monitor {

	private Runtime runtime;

	public Monitor() {
		runtime = Runtime.getRuntime();
	}

	public int getAvailableProcessors() {
		return runtime.availableProcessors();
	}

	public long getFreeMemory() {
		return runtime.freeMemory();
	}

	public long getMaxMemory() {
		return runtime.maxMemory();
	}

	public long getTotalMemory() {
		return runtime.totalMemory();
	}

	public double getMemoryRate() {
		return ((double) getFreeMemory() / (double) getTotalMemory()) * 100;
	}

	public Long getFreeDiskSpace() {
		try {
			return FileSystemUtils.freeSpaceKb(ClientCatalog.getDataDir()
					.getAbsolutePath()) * 1000;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (SearchLibException e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getDataDirectoryPath() throws SearchLibException {
		return ClientCatalog.getDataDir().getAbsolutePath();
	}

	public Set<Entry<Object, Object>> getProperties() {
		return System.getProperties().entrySet();
	}

	public int getIndexCount() throws SearchLibException {
		Set<ClientCatalogItem> clients = ClientCatalog.getClientCatalog(null);
		if (clients == null)
			return 0;
		return clients.size();
	}

	public void writeXmlConfig(XmlWriter xmlWriter) throws SAXException,
			SearchLibException {
		xmlWriter.startElement("system");

		xmlWriter.startElement("availableProcessors", "value", Integer
				.toString(getAvailableProcessors()));
		xmlWriter.endElement();

		xmlWriter.startElement("freeMemory", "value", Long
				.toString(getFreeMemory()), "rate", Double
				.toString(getMemoryRate()));
		xmlWriter.endElement();

		xmlWriter.startElement("maxMemory", "value", Long
				.toString(getMaxMemory()));
		xmlWriter.endElement();

		xmlWriter.startElement("totalMemory", "value", Long
				.toString(getTotalMemory()));
		xmlWriter.endElement();

		xmlWriter.startElement("indexCount", "value", Integer
				.toString(getIndexCount()));
		xmlWriter.endElement();

		xmlWriter.startElement("freeDiskSpace", "value", getFreeDiskSpace()
				.toString());
		xmlWriter.endElement();

		xmlWriter.startElement("dataDirectoryPath", "value",
				getDataDirectoryPath());
		xmlWriter.endElement();

		xmlWriter.endElement();

		xmlWriter.startElement("properties");

		for (Entry<Object, Object> prop : getProperties()) {
			xmlWriter.startElement("property", "name",
					prop.getKey().toString(), "value", prop.getValue()
							.toString());
			xmlWriter.endElement();
		}

		xmlWriter.endElement();
	}
}
