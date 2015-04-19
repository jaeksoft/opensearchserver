/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2015 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileSystemUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.util.XmlWriter;
import com.jaeksoft.searchlib.web.StartStopListener;
import com.jaeksoft.searchlib.web.Version;

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

	public String getVersion() {
		Version version = StartStopListener.getVersion();
		if (version == null)
			return null;
		return version.toString();
	}

	public double getMemoryRate() {
		return ((double) getFreeMemory() / (double) getMaxMemory()) * 100;
	}

	public Long getFreeDiskSpace() throws SecurityException, IOException,
			SearchLibException {
		try {
			long l = ClientFactory.INSTANCE.properties.getMaxStorage();
			if (l > 0) {
				l -= ClientCatalog.getInstanceSize();
				if (l < 0)
					l = 0;
				return l;
			}
			if (StartStopListener.OPENSEARCHSERVER_DATA_FILE.getClass()
					.getDeclaredMethod("getFreeSpace") != null)
				return StartStopListener.OPENSEARCHSERVER_DATA_FILE
						.getFreeSpace();
		} catch (NoSuchMethodException e) {
		}
		return FileSystemUtils
				.freeSpaceKb(StartStopListener.OPENSEARCHSERVER_DATA_FILE
						.getAbsolutePath()) * 1000;
	}

	public Long getTotalDiskSpace() throws SecurityException, IOException {
		try {
			long l = ClientFactory.INSTANCE.properties.getMaxStorage();
			if (l > 0)
				return l;
			if (StartStopListener.OPENSEARCHSERVER_DATA_FILE.getClass()
					.getDeclaredMethod("getTotalSpace") != null)
				return StartStopListener.OPENSEARCHSERVER_DATA_FILE
						.getTotalSpace();
		} catch (NoSuchMethodException e) {
		}
		return FileSystemUtils
				.freeSpaceKb(StartStopListener.OPENSEARCHSERVER_DATA_FILE
						.getAbsolutePath()) * 1000;
	}

	public Double getDiskRate() throws SecurityException, IOException,
			SearchLibException {
		Long free = getFreeDiskSpace();
		Long total = getTotalDiskSpace();
		if (free == null || total == null)
			return null;
		return ((double) free / (double) total) * 100;
	}

	public double getApiWaitRate() {
		return ClientFactory.INSTANCE.properties.getApiWaitRate();
	}

	public long getRequestPerMonthCount() {
		return ClientFactory.INSTANCE.properties.getRequestPerMonthCount();
	}

	public final boolean isRequestPerMonthOver() {
		return getRequestPerMonthCount() > ClientFactory.INSTANCE.properties
				.getRequestPerMonth();
	}

	public final boolean isRequestPerMonthUnder() {
		return !isRequestPerMonthOver();
	}

	public final String getRequestPerMonthLabel() {
		StringBuilder sb = new StringBuilder();
		sb.append(getRequestPerMonthCount());
		sb.append(" / ");
		sb.append(ClientFactory.INSTANCE.properties.getRequestPerMonth());
		return sb.toString();
	}

	public String getDataDirectoryPath() {
		return StartStopListener.OPENSEARCHSERVER_DATA_FILE.getAbsolutePath();
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
			SearchLibException, SecurityException, IOException {
		xmlWriter.startElement("system");

		xmlWriter.startElement("version", "value", getVersion());
		xmlWriter.endElement();

		xmlWriter.startElement("availableProcessors", "value",
				Integer.toString(getAvailableProcessors()));
		xmlWriter.endElement();

		xmlWriter.startElement("freeMemory", "value",
				Long.toString(getFreeMemory()), "rate",
				Double.toString(getMemoryRate()));
		xmlWriter.endElement();

		xmlWriter.startElement("maxMemory", "value",
				Long.toString(getMaxMemory()));
		xmlWriter.endElement();

		xmlWriter.startElement("totalMemory", "value",
				Long.toString(getTotalMemory()));
		xmlWriter.endElement();

		xmlWriter.startElement("indexCount", "value",
				Integer.toString(getIndexCount()));
		xmlWriter.endElement();

		Double rate = getDiskRate();
		if (rate == null)
			xmlWriter.startElement("freeDiskSpace", "value", getFreeDiskSpace()
					.toString());
		else
			xmlWriter.startElement("freeDiskSpace", "value", getFreeDiskSpace()
					.toString(), "rate", rate.toString());

		xmlWriter.endElement();

		xmlWriter.startElement("dataDirectoryPath", "value",
				getDataDirectoryPath());
		xmlWriter.endElement();

		xmlWriter.startElement("apiWaitRate", "rate",
				Double.toString(getApiWaitRate()));
		xmlWriter.endElement();

		xmlWriter.startElement("requestPerMonthCount", "value",
				Long.toString(getRequestPerMonthCount()));
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

	private final void addIfNotNull(List<NameValuePair> list, String name,
			String value) throws UnsupportedEncodingException {
		if (value == null)
			return;
		list.add(new BasicNameValuePair(name.replace('.', '_'), value));
	}

	public void writeToPost(List<NameValuePair> list) throws SearchLibException {

		try {
			addIfNotNull(list, "version", getVersion());
			addIfNotNull(list, "availableProcessors",
					Integer.toString(getAvailableProcessors()));
			addIfNotNull(list, "freeMemory", Long.toString(getFreeMemory()));
			addIfNotNull(list, "freeMemoryRate",
					Double.toString(getMemoryRate()));
			addIfNotNull(list, "maxMemory", Long.toString(getMaxMemory()));
			addIfNotNull(list, "totalMemory", Long.toString(getTotalMemory()));
			addIfNotNull(list, "indexCount", Integer.toString(getIndexCount()));
			addIfNotNull(list, "freeDiskSpace", getFreeDiskSpace().toString());

			Double rate = getDiskRate();
			if (rate != null)
				addIfNotNull(list, "freeDiskRate", rate.toString());

			addIfNotNull(list, "dataDirectoryPath", getDataDirectoryPath());

			for (Entry<Object, Object> prop : getProperties())
				addIfNotNull(list, "property_" + prop.getKey().toString(), prop
						.getValue().toString());

			addIfNotNull(list, "apiWaitRate", Double.toString(getApiWaitRate()));

		} catch (SecurityException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		}

	}
}
