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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.regex.Pattern;

public class Md5Spliter {

	private static MessageDigest md5 = null;

	private static String getMD5Hash(String data, String key)
			throws NoSuchAlgorithmException {
		if (md5 == null)
			md5 = MessageDigest.getInstance("MD5");
		byte result[] = null;
		synchronized (md5) {
			md5.update(data.getBytes());
			result = md5.digest(key.getBytes());
		}
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < result.length; i++) {
			String s = Integer.toHexString(result[i]);
			int length = s.length();
			if (length >= 2) {
				sb.append(s.substring(length - 2, length));
			} else {
				sb.append("0");
				sb.append(s);
			}
		}
		return sb.toString();
	}

	private Pattern keyPattern;

	public Md5Spliter(String keyPattern) {
		if (keyPattern != null)
			this.keyPattern = Pattern.compile(keyPattern);
	}

	public boolean acceptAnyKey(Collection<String> keys)
			throws NoSuchAlgorithmException {
		if (keyPattern == null)
			return true;
		for (String key : keys)
			if (keyPattern.matcher(getMD5Hash(key, "gisi")).matches())
				return true;
		return false;
	}

	protected Pattern getPattern() {
		return keyPattern;
	}

}
