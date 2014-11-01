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

import javax.xml.ws.WebServiceException;

import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.user.User;
import com.jaeksoft.searchlib.webservice.CommonServices;

public class MonitorImpl extends CommonServices implements RestMonitor {

	@Override
	public MonitorResult monitor(String login, String key, boolean full) {
		try {
			User user = getLoggedUser(login, key);
			if (user != null)
				if (!user.isMonitoring() && !user.isAdmin())
					throw new WebServiceException("Not allowed");
			ClientFactory.INSTANCE.properties.checkApi();
			return new MonitorResult(full);
		} catch (SearchLibException e) {
			throw new WebServiceException(e);
		} catch (InterruptedException e) {
			throw new WebServiceException(e);
		} catch (IOException e) {
			throw new WebServiceException(e);
		}
	}

	@Override
	public MonitorResult getMonitorXML(String login, String key, boolean full) {
		return monitor(login, key, full);
	}

	@Override
	public MonitorResult getMonitorJSON(String login, String key, boolean full) {
		return monitor(login, key, full);
	}
}
