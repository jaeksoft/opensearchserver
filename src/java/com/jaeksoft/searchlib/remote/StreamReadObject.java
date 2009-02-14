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

package com.jaeksoft.searchlib.remote;

import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

public class StreamReadObject {

	// private GZIPInputStream gis;
	private ObjectInputStream ois;

	public StreamReadObject(InputStream is) throws IOException {
		// gis = new GZIPInputStream(is);
		ois = new ObjectInputStream(is);
	}

	public Externalizable read() throws IOException, ClassNotFoundException {
		return (Externalizable) ois.readObject();
	}

	public void close() {
		if (ois != null) {
			try {
				ois.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			ois = null;
		}
		/*
		 * if (gis != null) { try { gis.close(); } catch (IOException e) {
		 * e.printStackTrace(); } gis = null; }
		 */
	}

}
