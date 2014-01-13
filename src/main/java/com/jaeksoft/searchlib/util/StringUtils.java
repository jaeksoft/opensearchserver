/**   
 *
 * Copyright (C) 2009-2013 Emmanuel Keller / Jaeksoft
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import com.jaeksoft.searchlib.util.FormatUtils.ThreadSafeDecimalFormat;

public class StringUtils extends org.apache.commons.lang3.StringUtils {

	private final static ThreadSafeDecimalFormat unitFormat = new ThreadSafeDecimalFormat(
			"0.0");

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
			return unitFormat.format(d) + ' ' + unit;
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
	private final static Pattern removeBrPattern1 = Pattern.compile(
			"\\.\\p{Space}+<br\\p{Space}*/?>", Pattern.CASE_INSENSITIVE);
	private final static Pattern removeEndTagBlockPattern1 = Pattern
			.compile(
					"\\.\\p{Space}+</(p|td|div|h1|h2|h3|h4|h5|h6|hr|li|option|pre|select|table|tbody|td|textarea|tfoot|thead|th|title|tr|ul)>",
					Pattern.CASE_INSENSITIVE);
	private final static Pattern removeEndTagBlockPattern2 = Pattern
			.compile(
					"</(p|td|div|h1|h2|h3|h4|h5|h6|hr|li|option|pre|select|table|tbody|td|textarea|tfoot|thead|th|title|tr|ul)>",
					Pattern.CASE_INSENSITIVE);
	private final static Pattern removeBrPattern2 = Pattern.compile(
			"<br\\p{Space}*/?>", Pattern.CASE_INSENSITIVE);
	private final static Pattern removeScriptObjectStylePattern = Pattern
			.compile(
					"<(script|object|style)[^>]*>[^<]*</(script|object|style)>",
					Pattern.CASE_INSENSITIVE);

	public static final String replaceConsecutiveSpaces(String source,
			String replace) {
		StringBuilder target = new StringBuilder();
		int l = source.length();
		boolean consecutiveSpace = false;
		for (int i = 0; i < l; i++) {
			char c = source.charAt(i);
			if (Character.isWhitespace(c)) {
				if (!consecutiveSpace) {
					if (replace != null)
						target.append(replace);
					consecutiveSpace = true;
				}
			} else {
				target.append(c);
				if (consecutiveSpace)
					consecutiveSpace = false;
			}
		}
		return target.toString();
	}

	public static final String removeTag(String text) {
		text = replaceConsecutiveSpaces(text, " ");
		synchronized (removeScriptObjectStylePattern) {
			text = removeScriptObjectStylePattern.matcher(text).replaceAll("");
		}
		synchronized (removeBrPattern1) {
			text = removeBrPattern1.matcher(text).replaceAll("</p>");
		}
		synchronized (removeEndTagBlockPattern1) {
			text = removeEndTagBlockPattern1.matcher(text).replaceAll("</p>");
		}
		synchronized (removeEndTagBlockPattern2) {
			text = removeEndTagBlockPattern2.matcher(text).replaceAll(". ");
		}
		synchronized (removeBrPattern2) {
			text = removeBrPattern2.matcher(text).replaceAll(". ");
		}
		synchronized (removeTagPattern) {
			text = removeTagPattern.matcher(text).replaceAll("");
		}
		text = replaceConsecutiveSpaces(text, " ");
		return text;
	}

	public static void main(String[] args) throws IOException {

		List<String> lines = FileUtils.readLines(new File(args[0]));
		FileWriter fw = new FileWriter(new File(args[1]));
		PrintWriter pw = new PrintWriter(fw);
		for (String line : lines)
			pw.println(StringEscapeUtils.unescapeHtml(line));
		pw.close();
		fw.close();
	}

	public static Pattern wildcardPattern(String s) {
		final CharSequence[] esc = { "\\", ".", "(", ")", "[", "]", "+", "?",
				"*" };
		final CharSequence[] replace = { "/", "\\.", "\\(", "\\)", "\\[",
				"\\]", "\\+", "\\?", ".*" };
		s = s.trim();
		int i = 0;
		for (CharSequence ch : esc)
			s = s.replace(ch, replace[i++]);
		return Pattern.compile(s);
	}

	public static final String removeTag(String text, String[] allowedTags) {
		if (allowedTags == null)
			text = replaceConsecutiveSpaces(text, " ");
		StringBuffer sb = new StringBuffer();
		Matcher matcher;
		synchronized (removeTagPattern) {
			matcher = removeTagPattern.matcher(text);
		}
		while (matcher.find()) {
			boolean allowed = false;
			String group = matcher.group();
			if (allowedTags != null) {
				for (String tag : allowedTags) {
					if (tag.equals(group)) {
						allowed = true;
						break;
					}
				}
			}
			matcher.appendReplacement(sb, allowed ? group : "");
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	/**
	 * 
	 * @param text
	 *            the text to encode
	 * @return a base64 encoded string
	 * @throws UnsupportedEncodingException
	 */
	public final static String base64encode(String text)
			throws UnsupportedEncodingException {
		if (text == null)
			return null;
		return Base64.encodeBase64URLSafeString(text.getBytes("UTF-8"));
	}

	/**
	 * 
	 * @param base64String
	 *            the base64 string to decode
	 * @return a decoded string
	 */
	public final static String base64decode(String base64String) {
		if (base64String == null)
			return null;
		return new String(Base64.decodeBase64(base64String));
	}

	public final static int compareNullValues(final Object v1, final Object v2) {
		if (v1 == null) {
			if (v2 == null)
				return 0;
			return -1;
		}
		if (v2 == null)
			return 1;
		return 0;
	}

	public final static int compareNullString(final String v1, final String v2) {
		if (v1 == null) {
			if (v2 == null)
				return 0;
			return -1;
		}
		if (v2 == null)
			return 1;
		return v1.compareTo(v2);
	}

	public final static String leftPad(int value, int size) {
		return org.apache.commons.lang.StringUtils.leftPad(
				Integer.toString(value), size, '0');
	}

	public final static String leftPad(long value, int size) {
		return org.apache.commons.lang.StringUtils.leftPad(
				Long.toString(value), size, '0');
	}

	public final static String charsetDetector(InputStream inputStream)
			throws IOException {
		CharsetDetector detector = new CharsetDetector();
		detector.setText(inputStream);
		CharsetMatch match = detector.detect();
		if (match == null)
			return null;
		return match.getName();
	}

	public final static String charsetDetector(byte[] bytes) {
		CharsetDetector detector = new CharsetDetector();
		detector.setText(bytes);
		CharsetMatch match = detector.detect();
		if (match == null)
			return null;
		return match.getName();
	}

	public final static String[] toStringArray(
			Collection<? extends Object> collection, boolean sort) {
		if (collection == null)
			return null;
		String[] array = new String[collection.size()];
		int i = 0;
		for (Object o : collection)
			array[i++] = o.toString();
		if (sort)
			Arrays.sort(array);
		return array;
	}

	public final static CharSequence fastConcatCharSequence(
			final CharSequence... charSeqs) {
		if (charSeqs == null)
			return null;
		if (charSeqs.length == 1)
			return charSeqs[0];
		StringBuilder sb = new StringBuilder();
		for (CharSequence charSeq : charSeqs)
			sb.append(charSeq);
		return sb;
	}

	public final static String fastConcat(final CharSequence... charSeqs) {
		CharSequence cs = fastConcatCharSequence(charSeqs);
		return cs == null ? null : cs.toString();
	}

	public final static String LINE_SEPARATOR = System
			.getProperty("line.separator");

	public final static String[] splitLines(String str) {
		return split(str, LINE_SEPARATOR);
	}

	public final static Charset CharsetUTF8 = Charset.forName("UTF-8");

	public final static CharsetEncoder newUTF8Encoder() {
		synchronized (CharsetUTF8) {
			return CharsetUTF8.newEncoder();
		}
	}

}
