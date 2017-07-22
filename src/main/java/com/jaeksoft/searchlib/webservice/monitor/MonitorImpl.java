/*
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2011-2017 Emmanuel Keller / Jaeksoft
 * <p>
 * http://www.open-search-server.com
 * <p>
 * This file is part of OpenSearchServer.
 * <p>
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.jaeksoft.searchlib.webservice.monitor;

import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.user.User;
import com.jaeksoft.searchlib.webservice.CommonServices;

import javax.xml.ws.WebServiceException;
import java.io.IOException;

public class MonitorImpl extends CommonServices implements RestMonitor {

	public MonitorResult monitor(String login, String key, boolean full) {
		try {
			User user = getLoggedUser(login, key);
			if (user != null)
				if (!user.isMonitoring() && !user.isAdmin())
					throw new WebServiceException("Not allowed");
			ClientFactory.INSTANCE.properties.checkApi();
			return new MonitorResult(full);
		} catch (SearchLibException | InterruptedException | IOException e) {
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
