/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
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

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

import com.jaeksoft.searchlib.schema.FieldValueItem;

public class Md5Spliter {

	final private static String generateHash(byte[] result) {
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

	final public static String getMD5Hash(byte[] data, int offset, int length)
			throws NoSuchAlgorithmException {
		MessageDigest md5 = MessageDigest.getInstance("MD5");
		md5.update(data);
		return generateHash(md5.digest());
	}

	final public static String getMD5Hash(String str)
			throws NoSuchAlgorithmException, UnsupportedEncodingException {
		byte[] bytes = str.getBytes("UTF-8");
		return getMD5Hash(bytes, 0, bytes.length);
	}

	final public static String getMD5Hash(String data, String key)
			throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest md5 = MessageDigest.getInstance("MD5");
		md5.update(data.getBytes("UTF-8"));
		return generateHash(md5.digest(key.getBytes("UTF-8")));
	}

	private Pattern keyPattern;

	public Md5Spliter(String keyPattern) {
		if (keyPattern != null)
			this.keyPattern = Pattern.compile(keyPattern);
	}

	public boolean acceptAnyKey(FieldValueItem[] keys)
			throws NoSuchAlgorithmException, UnsupportedEncodingException {
		if (keyPattern == null)
			return true;
		for (FieldValueItem key : keys)
			if (keyPattern.matcher(getMD5Hash(key.getValue(), "gisi"))
					.matches())
				return true;
		return false;
	}

	protected Pattern getPattern() {
		return keyPattern;
	}

}
