/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2012 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.webservice;

import java.io.IOException;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.ws.WebServiceException;

import com.jaeksoft.searchlib.Monitor;
import com.jaeksoft.searchlib.SearchLibException;

@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class MonitorSystem {

	@XmlElement(name = "availableProcessors")
	public int availableProcessors;

	@XmlElement(name = "freeMemory")
	public long freeMemory;

	@XmlElement(name = "memoryRate")
	public double memoryRate;

	@XmlElement(name = "maxMemory")
	public long maxMemory;

	@XmlElement(name = "totalMemory")
	public long totalMemory;

	@XmlElement(name = "indexCount")
	public int indexCount;

	@XmlElement(name = "freeDiskSpace")
	public long freeDiskSpace;

	@XmlElement(name = "freeDiskRate")
	public Double freeDiskRate;

	public MonitorSystem() {
		availableProcessors = 0;
		freeMemory = 0;
		memoryRate = 0;
		maxMemory = 0;
		totalMemory = 0;
		indexCount = 0;
		freeDiskSpace = 0;
		freeDiskRate = 0.0;
	}

	public MonitorSystem(Monitor monitor) {

		try {
			availableProcessors = monitor.getAvailableProcessors();
			freeMemory = monitor.getFreeMemory();
			memoryRate = monitor.getMemoryRate();
			maxMemory = monitor.getMaxMemory();
			totalMemory = monitor.getTotalMemory();
			indexCount = monitor.getIndexCount();
			freeDiskSpace = monitor.getFreeDiskSpace();
			freeDiskRate = monitor.getDiskRate();
		} catch (SearchLibException e) {
			throw new WebServiceException(e);
		} catch (SecurityException e) {
			throw new WebServiceException(e);
		} catch (IOException e) {
			throw new WebServiceException(e);
		}

	}
}
