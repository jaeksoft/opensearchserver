/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013 Emmanuel Keller / Jaeksoft
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

import java.util.HashMap;
import java.util.Map;

import com.jaeksoft.searchlib.result.AbstractResultSearch;
import com.jaeksoft.searchlib.util.ReadWriteLock;

public class RendererResults {

	private final ReadWriteLock rwl = new ReadWriteLock();

	private Map<Integer, RendererResult> results;

	public RendererResults() {
		results = new HashMap<Integer, RendererResult>();
	}

	final public RendererResult addResult(Renderer renderer,
			AbstractResultSearch result) {
		rwl.w.lock();
		try {
			RendererResult rendererResult = new RendererResult(renderer, result);
			results.put(rendererResult.hashCode(), rendererResult);
			return rendererResult;
		} finally {
			rwl.w.unlock();
		}
	}

	final public RendererResult find(int hashCode) {
		rwl.r.lock();
		try {
			return results.get(hashCode);
		} finally {
			rwl.r.unlock();
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
