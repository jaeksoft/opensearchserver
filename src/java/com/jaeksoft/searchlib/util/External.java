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

package com.jaeksoft.searchlib.util;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;

public class External {

	final public static <T> void writeList(List<T> list, ObjectOutput out)
			throws IOException {
		if (list != null) {
			int l = list.size();
			if (l > 0) {
				out.writeInt(l);
				for (T object : list)
					out.writeObject(object);
			}
		}
		out.writeInt(0);
	}

	@SuppressWarnings("unchecked")
	final public static <T> void readList(ObjectInput in, List<T> list)
			throws IOException, ClassNotFoundException {
		int l = in.readInt();
		while (l-- > 0)
			list.add((T) in.readObject());
	}

	final public static <T> void writeArray(T[] array, ObjectOutput out)
			throws IOException {
		if (array != null) {
			if (array.length > 0) {
				out.writeInt(array.length);
				for (T object : array)
					out.writeObject(object);
				return;
			}
		}
		out.writeInt(0);
	}

	@SuppressWarnings("unchecked")
	final public static <T> void readArray(ObjectInput in, T[] array)
			throws IOException, ClassNotFoundException {
		if (array == null)
			return;
		for (int i = 0; i < array.length; i++)
			array[i] = (T) in.readObject();
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