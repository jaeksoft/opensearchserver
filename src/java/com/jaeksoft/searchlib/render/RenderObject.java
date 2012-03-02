/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2011 Emmanuel Keller / Jaeksoft
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

import java.io.Externalizable;
import java.io.IOException;

import com.jaeksoft.searchlib.remote.StreamWriteObject;
import com.jaeksoft.searchlib.web.ServletTransaction;

public class RenderObject implements Render {

	private Externalizable object;

	public RenderObject(Externalizable object) throws IOException {
		this.object = object;
	}

	@Override
	public void render(ServletTransaction servletTransaction) throws Exception {

		servletTransaction
				.setResponseContentType("application/x-java-serialized-object");
		// response.setHeader("Content-Encoding", "gzip");
		StreamWriteObject writeObject = null;
		IOException err = null;
		try {
			writeObject = new StreamWriteObject(
					servletTransaction.getOutputStream());
			writeObject.write(object);
			writeObject.close(true);
		} catch (IOException e) {
			err = e;
		} finally {
			if (writeObject != null)
				writeObject.close(false);
			if (err != null)
				throw err;
		}
	}

}
