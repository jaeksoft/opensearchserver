/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2013 Emmanuel Keller / Jaeksoft
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

import java.util.List;

import javax.naming.NamingException;

import org.zkoss.bind.annotation.AfterCompose;

import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.util.ThreadUtils;
import com.jaeksoft.searchlib.util.ThreadUtils.ThreadInfo;
import com.jaeksoft.searchlib.web.controller.CommonController;

@AfterCompose(superclass = true)
public class ThreadsController extends CommonController {

	private List<ThreadInfo> threadList = null;

	public ThreadsController() throws SearchLibException {
		super();
	}

	public List<ThreadInfo> getList() throws SearchLibException,
			NamingException {
		if (threadList != null)
			return threadList;

		threadList = ThreadUtils.getInfos(ClientCatalog.getThreadGroup());
		return threadList;
	}

	@Override
	protected void reset() throws SearchLibException {
		threadList = null;
	}

}
