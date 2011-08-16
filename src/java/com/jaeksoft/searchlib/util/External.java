/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;

public class External {

	final public static void writeCollection(Collection<?> collection,
			ObjectOutput out) throws IOException {
		int l = collection != null ? collection.size() : 0;
		out.writeInt(l);
		if (l == 0)
			return;
		for (Object object : collection)
			out.writeObject(object);
	}

	public interface Collecter<T> {
		public void addObject(T object);
	}

	@SuppressWarnings("unchecked")
	final public static <T> void readCollection(ObjectInput in,
			Collecter<T> collecter) throws IOException, ClassNotFoundException {
		int l = in.readInt();
		while (l-- > 0)
			collecter.addObject((T) in.readObject());
	}

	final public static void readObjectArray(ObjectInput in,
			Externalizable[] array) throws IOException, ClassNotFoundException {
		if (array == null)
			return;
		for (int i = 0; i < array.length; i++)
			array[i] = (Externalizable) in.readObject();
	}

	final public static void writeObjectArray(Externalizable[] array,
			ObjectOutput out) throws IOException {
		if (array != null) {
			if (array.length > 0) {
				out.writeInt(array.length);
				for (Object object : array)
					out.writeObject(object);
				return;
			}
		}
		out.writeInt(0);
	}

	final public static String[] readStringArray(ObjectInput in)
			throws IOException, ClassNotFoundException {
		int l = in.readInt();
		if (l == 0)
			return null;
		String[] array = new String[l];
		for (int i = 0; i < array.length; i++)
			array[i] = in.readUTF();
		return array;
	}

	final public static void writeStringArray(String[] array, ObjectOutput out)
			throws IOException {
		if (array != null) {
			if (array.length > 0) {
				out.writeInt(array.length);
				for (String string : array)
					out.writeUTF(string);
				return;
			}
		}
		out.writeInt(0);
	}

	final public static <T> void writeObject(T object, ObjectOutput out)
			throws IOException {
		out.writeBoolean(object != null);
		if (object != null)
			out.writeObject(object);
	}

	@SuppressWarnings("unchecked")
	final public static <T> T readObject(ObjectInput in) throws IOException,
			ClassNotFoundException {
		if (!in.readBoolean())
			return null;
		return (T) in.readObject();
	}

	final public static void writeUTF(String string, ObjectOutput out)
			throws IOException {
		out.writeBoolean(string != null);
		if (string != null)
			out.writeUTF(string);
	}

	public static String readUTF(ObjectInput in) throws IOException {
		if (!in.readBoolean())
			return null;
		return in.readUTF();
	}

}