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

package com.jaeksoft.searchlib.web.controller.runtime;

import java.io.IOException;

import org.zkoss.zk.ui.event.Event;

import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.hadoop.HadoopManager;
import com.jaeksoft.searchlib.web.controller.CommonComposer;

public class HadoopComposer extends CommonComposer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2114175890264462114L;

	@Override
	protected void reset() throws SearchLibException {
	}

	public HadoopManager getHadoopManager() throws SearchLibException,
			IOException {
		return ClientCatalog.getHadoopManager();
	}

	public void onReload$window(Event event) throws SearchLibException,
			IOException {
		HadoopManager manager = ClientCatalog.getHadoopManager();
		if (manager == null)
			return;
		manager.reloadConfiguration();
		reloadPage();
	}

	public void onFlush$window(Event event) throws SearchLibException,
			IOException {
		HadoopManager manager = ClientCatalog.getHadoopManager();
		if (manager == null)
			return;
		manager.flushCache();
		reloadPage();
	}

}
