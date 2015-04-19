/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013-2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.renderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.renderer.plugin.AuthPluginInterface;
import com.jaeksoft.searchlib.util.ReadWriteLock;

public class RendererResults {

	private final ReadWriteLock rwl = new ReadWriteLock();

	private Map<Integer, RendererResult> results;

	public RendererResults() {
		results = new HashMap<Integer, RendererResult>();
	}

	final public RendererResult addResult(Client client, Renderer renderer,
			String keywords, AuthPluginInterface.User loggedUser) {
		rwl.w.lock();
		try {
			RendererResult rendererResult = new RendererResult(client,
					renderer, keywords, loggedUser);
			results.put(rendererResult.hashCode(), rendererResult);
			return rendererResult;
		} finally {
			rwl.w.unlock();
		}
	}

	final public RendererResult find(Integer hashCode) {
		purge();
		rwl.r.lock();
		try {
			return results.get(hashCode);
		} finally {
			rwl.r.unlock();
		}
	}

	final public void purge() {
		List<Integer> deleteList = new ArrayList<Integer>(0);
		rwl.r.lock();
		try {
			long expireTime = System.currentTimeMillis() - 60 * 60 * 1000;
			for (RendererResult result : results.values())
				if (result.getCreationTime() < expireTime)
					deleteList.add(result.hashCode());
			if (deleteList.size() == 0)
				return;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			for (Integer i : deleteList)
				results.remove(i);
		} finally {
			rwl.w.unlock();
		}
	}

	public void release() {
		rwl.w.lock();
		try {
			results.clear();
		} finally {
			rwl.w.unlock();
		}
	}
}
