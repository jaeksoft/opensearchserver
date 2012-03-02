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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;

import org.apache.http.HttpException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.web.controller.CommonController;

public class CommandsController extends CommonController {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7911006190658783502L;

	private transient Date lastReload;

	private transient Date lastOptimize;

	public CommandsController() throws SearchLibException {
		super();
	}

	@Override
	public void reset() {
		lastReload = null;
		lastOptimize = null;
	}

	@Override
	public void onReload() throws IOException, URISyntaxException,
			SearchLibException, InstantiationException, IllegalAccessException,
			ClassNotFoundException, HttpException {
		synchronized (this) {
			Date t = new Date();
			getClient().reload();
			lastReload = t;
			reloadPage();
		}
	}

	public void onOptimize() throws IOException, URISyntaxException,
			SearchLibException, InstantiationException, IllegalAccessException,
			ClassNotFoundException, HttpException {
		synchronized (this) {
			Date t = new Date();
			getClient().optimize();
			lastOptimize = t;
			reloadPage();
		}
	}

	public Date getLastReload() {
		synchronized (this) {
			return lastReload;
		}
	}

	public Date getLastOptimize() {
		synchronized (this) {
			return lastOptimize;
		}
	}

}
