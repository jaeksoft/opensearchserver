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

package com.jaeksoft.searchlib.web.controller.report;

import org.zkoss.bind.annotation.AfterCompose;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.logreport.LogReportManager;
import com.jaeksoft.searchlib.report.ReportsManager;
import com.jaeksoft.searchlib.web.controller.CommonController;

@AfterCompose(superclass = true)
public class ReportsController extends CommonController {

	public ReportsController() throws SearchLibException {
		super();
	}

	public ReportsManager getReportsManager() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		return client.getReportsManager();
	}

	public LogReportManager getLogReportManager() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		return client.getLogReportManager();
	}

	@Override
	protected void reset() throws SearchLibException {
	}

}