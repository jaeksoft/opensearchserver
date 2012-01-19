/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2010 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import com.jaeksoft.searchlib.Logging;

public class Timer {

	private long startTime;
	private long endTime;
	private String info;
	private String error;

	public Timer(String info) {
		reset();
		setInfo(info);
	}

	public void reset() {
		startTime = System.currentTimeMillis();
		endTime = 0;
		info = null;
		error = null;
	}

	public long getStartTime() {
		return startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEnd() {
		this.endTime = System.currentTimeMillis();
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public long duration() {
		if (this.endTime == 0)
			this.setEnd();
		return this.endTime - this.startTime;
	}

	public void setError(Exception exception) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		exception.printStackTrace(pw);
		pw.close();
		try {
			sw.close();
		} catch (IOException e) {
			Logging.warn(e.getMessage(), e);
		}
		this.error = sw.toString();
	}

	public String getError() {
		return this.error;
	}

}
