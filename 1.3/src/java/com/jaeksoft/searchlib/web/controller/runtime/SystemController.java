/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.web.controller.runtime;

import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.Monitor;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.web.controller.CommonController;

public class SystemController extends CommonController {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6358001462419893725L;

	private transient Monitor monitor;

	public SystemController() throws SearchLibException {
		super();
	}

	@Override
	public void reset() {
		monitor = new Monitor();
	}

	@Override
	public void reloadPage() throws SearchLibException {
		reset();
		super.reloadPage();
	}

	public Monitor getMonitor() {
		return monitor;
	}

	public boolean isApiRateLimit() {
		return ClientFactory.INSTANCE.properties.isMaxApiRate();
	}

	public boolean isRequestPerMonthLimit() {
		return ClientFactory.INSTANCE.properties.isRequestPerMonth();
	}

}
