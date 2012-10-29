/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.web;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.common.process.CrawlMasterAbstract;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.user.User;

public class WebCrawlerServlet extends AbstractServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8453924728832924760L;

	public enum Action {
		START, STOP, STATUS, EMTPY;
	}

	public enum InfoStatus {
		STARTED, STARTING, STOPPED, STOPPING;
	}

	protected void doCrawlMaster(CrawlMasterAbstract crawlMaster,
			ServletTransaction transaction) {

		String action = transaction.getParameterString("action");

		if (action == null) {
			action = transaction.getParameterString("cmd");
		}
		int timeOut = transaction.getParameterInteger("timeout", 1200);

		if (Action.STOP.name().equalsIgnoreCase(action)) {
			crawlMaster.abort();
			if (crawlMaster.waitForEnd(timeOut))
				transaction.addXmlResponse("Info", InfoStatus.STOPPED.name());
			else
				transaction.addXmlResponse("Info", InfoStatus.STOPPING.name());
		} else if (Action.START.name().equalsIgnoreCase(action)) {
			crawlMaster.start();
			if (crawlMaster.waitForStart(timeOut))
				transaction.addXmlResponse("Info", InfoStatus.STARTED.name());
			else
				transaction.addXmlResponse("Info", InfoStatus.STARTING.name());
		} else if (Action.STATUS.name().equalsIgnoreCase(action)) {
			if (crawlMaster.isAborting())
				transaction.addXmlResponse("Info", InfoStatus.STOPPING.name());
			else if (crawlMaster.isRunning())
				transaction.addXmlResponse("Info", InfoStatus.STARTED.name());
			else
				transaction.addXmlResponse("Info", InfoStatus.STOPPED.name());
		} else
			transaction.addXmlResponse("Info", Action.EMTPY.name());
		transaction.addXmlResponse("status", "OK");
	}

	@Override
	protected void doRequest(ServletTransaction transaction)
			throws ServletException {
		try {

			User user = transaction.getLoggedUser();
			if (user != null
					&& !user.hasRole(transaction.getIndexName(),
							Role.WEB_CRAWLER_START_STOP))
				throw new SearchLibException("Not permitted");

			Client client = transaction.getClient();
			doCrawlMaster(client.getWebCrawlMaster(), transaction);

		} catch (Exception e) {
			throw new ServletException(e);
		}
	}
}
