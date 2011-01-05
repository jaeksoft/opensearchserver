/**   
 *
 * Copyright (C) 2009-2010 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.util;

import java.text.DecimalFormat;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;

public class StringUtils {

	private enum SizeUnit {
		BYTE("B", 1), KILOBYTE("KB", 1024), MEGABYTE("MB", 1024 * 1024), GIGABYTE(
				"GB", 1024 * 1024 * 1024);

		private String unit;
		private float div;

		SizeUnit(String unit, float div) {
			this.unit = unit;
			this.div = div;
		}

		boolean check(long size) {
			return size >= div;
		}

		String render(long size) {
			float d = (float) size / div;
			return new DecimalFormat("0.0").format(d) + ' ' + unit;
		}
	}

	public static String humanBytes(Long size) {
		if (size == null)
			return null;
		SizeUnit selectedUnit = SizeUnit.BYTE;
		for (SizeUnit unit : SizeUnit.values()) {
			if (unit.check(size))
				selectedUnit = unit;
		}
		return selectedUnit.render(size);
	}

	private final static Pattern removeTagPattern = Pattern.compile("<[^>]*>");
	private final static Pattern removeSpacePattern = Pattern
			.compile("\\p{Space}");
	private final static Pattern removeEndTagBlockPattern = Pattern
			.compile("[^\\p{Punct}].<\\/(p|td|div|h1|h2|h3|h4|h5|h6|hr|li|option|pre|select|table|tbody|td|textarea|tfoot|thead|th|title|tr|ul)>");

	public static String removeTag(String text) {
		text = removeSpacePattern.matcher(text).replaceAll(" ");
		text = removeEndTagBlockPattern.matcher(text).replaceAll(". ");
		return removeTagPattern.matcher(text).replaceAll("");
	}

	/**
	 * 
	 * @param text
	 *            the text to encode
	 * @return a base64 encoded string
	 */
	public final static String base64encode(String text) {
		return Base64.encodeBase64URLSafeString(text.getBytes());
	}

	/**
	 * 
	 * @param base64String
	 *            the base64 string to decode
	 * @return a decoded string
	 */
	public final static String base64decode(String base64String) {
		return new String(Base64.decodeBase64(base64String));
	}

	private final static String[] zeroArray = {
			org.apache.commons.lang.StringUtils.repeat("0", 1),
			org.apache.commons.lang.StringUtils.repeat("0", 2),
			org.apache.commons.lang.StringUtils.repeat("0", 3),
			org.apache.commons.lang.StringUtils.repeat("0", 4),
			org.apache.commons.lang.StringUtils.repeat("0", 5),
			org.apache.commons.lang.StringUtils.repeat("0", 6),
			org.apache.commons.lang.StringUtils.repeat("0", 7),
			org.apache.commons.lang.StringUtils.repeat("0", 8),
			org.apache.commons.lang.StringUtils.repeat("0", 9),
			org.apache.commons.lang.StringUtils.repeat("0", 10),
			org.apache.commons.lang.StringUtils.repeat("0", 11),
			org.apache.commons.lang.StringUtils.repeat("0", 12),
			org.apache.commons.lang.StringUtils.repeat("0", 13),
			org.apache.commons.lang.StringUtils.repeat("0", 14),
			org.apache.commons.lang.StringUtils.repeat("0", 15),
			org.apache.commons.lang.StringUtils.repeat("0", 16) };

	public final static String longToHexString(long value) {
		String s = Long.toString(value, 16);
		int l = 16 - s.length();
		if (l <= 0)
			return s;
		return zeroArray[l - 1] + s;
	}

	public final static long hexStringToLong(String s) {
		return Long.parseLong(s, 16);
	}
}
