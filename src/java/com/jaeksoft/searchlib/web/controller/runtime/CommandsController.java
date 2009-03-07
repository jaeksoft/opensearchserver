/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft SearchLib Community.
 *
 * Jaeksoft SearchLib Community is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft SearchLib Community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft SearchLib Community. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.web.controller.runtime;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.LockObtainFailedException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.web.controller.CommonController;

public class CommandsController extends CommonController {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7911006190658783502L;

	private Date lastReload;

	private Date lastOptimize;

	public CommandsController() throws SearchLibException {
		super();
		lastReload = null;
		lastOptimize = null;
	}

	public void onReload() throws IOException, URISyntaxException,
			SearchLibException {
		synchronized (this) {
			Date t = new Date();
			getClient().reload();
			lastReload = t;
			reloadPage();
		}
	}

	public void onOptimize() throws CorruptIndexException,
			LockObtainFailedException, IOException, URISyntaxException,
			SearchLibException {
		synchronized (this) {
			Date t = new Date();
			getClient().getIndex().optimize(null);
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
