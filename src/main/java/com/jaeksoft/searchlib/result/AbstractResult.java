/**
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
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
 **/

package com.jaeksoft.searchlib.result;

import com.jaeksoft.searchlib.render.Render;
import com.jaeksoft.searchlib.request.AbstractRequest;
import com.jaeksoft.searchlib.util.Timer;
import com.jaeksoft.searchlib.web.ServletTransaction;

public abstract class AbstractResult<T extends AbstractRequest> {

	protected T request;

	protected Timer timer;

	protected AbstractResult(final T request) {
		this.request = request;
		this.timer = new Timer(request.getNameType());
	}

	public T getRequest() {
		return request;
	}

	public Timer getTimer() {
		return timer;
	}

	protected abstract Render getRenderXml();

	protected abstract Render getRenderCsv();

	protected abstract Render getRenderJson(boolean indent);

	public Render getRender(ServletTransaction transaction) {
		final String render = transaction.getParameterString("render");
		if (render != null) {
			if ("json" .equalsIgnoreCase(render)) {
				boolean jsonIndent = transaction.getParameterBoolean("indent", false);
				return getRenderJson(jsonIndent);
			} else if ("csv" .equalsIgnoreCase(render))
				return getRenderCsv();
		}
		return getRenderXml();
	}
}
