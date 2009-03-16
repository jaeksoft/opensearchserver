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

package com.jaeksoft.searchlib.parser;

import java.io.IOException;
import java.io.InputStream;

public class LimitInputStream extends InputStream {

	private boolean isComplete;
	private InputStream inputStream;
	private long limit;

	public LimitInputStream(InputStream inputStream, long limit) {
		this.inputStream = inputStream;
		this.limit = limit;
		this.isComplete = true;
	}

	@Override
	public int read() throws IOException {
		if (limit-- == 0) {
			isComplete = false;
			throw new LimitException();
		}
		return inputStream.read();
	}

	public boolean isComplete() {
		return isComplete;
	}

}
