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
package com.jaeksoft.searchlib.webservice.monitor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import com.jaeksoft.searchlib.Monitor;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.webservice.CommonResult;

@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@XmlRootElement(name = "result")
public class MonitorResult extends CommonResult {

	@XmlElement
	final public MonitorBasic basic;

	@XmlElement(name = "property")
	final public List<MonitorProperties> properties;

	public MonitorResult() {
		basic = null;
		properties = null;
	}

	public MonitorResult(boolean full) throws SearchLibException,
			SecurityException, IOException {
		super(true, null);
		Monitor monitor = new Monitor();
		basic = new MonitorBasic(monitor);
		if (full) {
			properties = new ArrayList<MonitorProperties>();
			for (Entry<Object, Object> prop : monitor.getProperties()) {
				MonitorProperties monitorProperties = new MonitorProperties(
						prop.getKey().toString(), prop.getValue().toString());
				properties.add(monitorProperties);
			}
		} else
			properties = null;
	}

	@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
	public static class MonitorBasic {
		@XmlAttribute
		final public int availableProcessors;

		@XmlAttribute
		final public long freeMemory;

		@XmlAttribute
		final public double memoryRate;

		@XmlAttribute
		final public long maxMemory;

		@XmlAttribute
		final public long totalMemory;

		@XmlAttribute
		final public int indexCount;

		@XmlAttribute
		final public long freeDiskSpace;

		@XmlAttribute
		final public Double freeDiskRate;

		public MonitorBasic() {
			availableProcessors = 0;
			freeMemory = 0;
			memoryRate = 0;
			maxMemory = 0;
			totalMemory = 0;
			indexCount = 0;
			freeDiskSpace = 0;
			freeDiskRate = null;
		}

		public MonitorBasic(Monitor monitor) throws SearchLibException,
				SecurityException, IOException {
			availableProcessors = monitor.getAvailableProcessors();
			freeMemory = monitor.getFreeMemory();
			memoryRate = monitor.getMemoryRate();
			maxMemory = monitor.getMaxMemory();
			totalMemory = monitor.getTotalMemory();
			indexCount = monitor.getIndexCount();
			freeDiskSpace = monitor.getFreeDiskSpace();
			freeDiskRate = monitor.getDiskRate();
		}
	}

	@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
	public static class MonitorProperties {

		@XmlAttribute
		final public String name;

		@XmlValue
		final public String value;

		public MonitorProperties() {
			name = null;
			value = null;
		}

		public MonitorProperties(String name, String value) {
			this.name = name;
			this.value = value;
		}
	}
}
