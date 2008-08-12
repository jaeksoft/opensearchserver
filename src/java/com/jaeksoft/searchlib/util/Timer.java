/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.util;

import java.io.Serializable;

public class Timer implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5045504570428743500L;

	private long startTime;
	private long endTime;
	private String name;

	public Timer() {
		this.startTime = System.currentTimeMillis();
		this.endTime = 0;
		this.name = null;
	}

	public Timer(String name) {
		this();
		this.name = name;
	}

	public void end() {
		this.endTime = System.currentTimeMillis();
	}

	public long duration() {
		if (this.endTime == 0)
			this.end();
		return this.endTime - this.startTime;
	}

	public String toString() {
		return name + ": " + duration();
	}

}
