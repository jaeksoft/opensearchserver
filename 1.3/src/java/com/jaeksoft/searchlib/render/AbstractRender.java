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

package com.jaeksoft.searchlib.render;

import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import com.jaeksoft.searchlib.request.AbstractRequest;
import com.jaeksoft.searchlib.result.AbstractResult;
import com.jaeksoft.searchlib.util.Timer;

public abstract class AbstractRender<T1 extends AbstractRequest, T2 extends AbstractResult<T1>>
		implements Render {

	final protected T2 result;
	final protected T1 request;
	final private NumberFormat scoreFormat;
	final protected Timer renderingTimer;

	protected AbstractRender(T2 result) {
		this.result = result;
		this.request = result.getRequest();
		scoreFormat = new DecimalFormat();
		scoreFormat.setGroupingUsed(false);
		renderingTimer = new Timer(result.getTimer(), "Rendering");

	}

	final protected void writeScore(PrintWriter writer, double score) {
		writer.print(scoreFormat.format(score));
	}

}
