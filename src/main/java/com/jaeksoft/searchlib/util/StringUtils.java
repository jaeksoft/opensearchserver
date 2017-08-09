/*
 * Copyright (C) 2009-2017 Emmanuel Keller / Jaeksoft
 * <p>
 * http://www.open-search-server.com
 * <p>
 * This file is part of OpenSearchServer.
 * <p>
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.jaeksoft.searchlib.util;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import com.jaeksoft.searchlib.util.FormatUtils.ThreadSafeDecimalFormat;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.text.WordUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils extends org.apache.commons.lang3.StringUtils {

	private final static ThreadSafeDecimalFormat unitFormat = new ThreadSafeDecimalFormat("0.0");

	private enum SizeUnit {
		BYTE("B", 1), KILOBYTE("KB", 1024), MEGABYTE("MB", 1024 * 1024), GIGABYTE("GB", 1024 * 1024 * 1024);

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
	private final static Pattern removeBrPattern1 = Pattern.compile("\\.\\p{Space}+<br\\p{Space}*/?>",
			Pattern.CASE_INSENSITIVE);
	private final static Pattern removeEndTagBlockPattern1 = Pattern.compile(
			"\\.\\p{Space}+</(p|td|div|h1|h2|h3|h4|h5|h6|hr|li|option|pre|select|table|tbody|td|textarea|tfoot|thead|th|title|tr|ul)>",
			Pattern.CASE_INSENSITIVE);
	private final static Pattern removeEndTagBlockPattern2 = Pattern.compile(
			"</(p|td|div|h1|h2|h3|h4|h5|h6|hr|li|option|pre|select|table|tbody|td|textarea|tfoot|thead|th|title|tr|ul)>",
			Pattern.CASE_INSENSITIVE);
	private final static Pattern removeBrPattern2 = Pattern.compile("<br\\p{Space}*/?>", Pattern.CASE_INSENSITIVE);
	private final static Pattern removeScriptObjectStylePattern = Pattern.compile(
			"<(script|object|style)[^>]*>[^<]*</(script|object|style)>", Pattern.CASE_INSENSITIVE);

	public static final String replaceConsecutiveSpaces(String source, String replace) {
		if (isEmpty(source))
			return source;
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
		if (isEmpty(text))
			return text;
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

	public static Pattern wildcardPattern(String s) {
		final CharSequence[] esc = { "\\", ".", "(", ")", "[", "]", "+", "?", "*" };
		final CharSequence[] replace = { "/", "\\.", "\\(", "\\)", "\\[", "\\]", "\\+", "\\?", ".*" };
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
	 * @param text the text to encode
	 * @return a base64 encoded string
	 * @throws UnsupportedEncodingException
	 */
	public final static String base64encode(String text) throws UnsupportedEncodingException {
		if (isEmpty(text))
			return null;
		return Base64.encodeBase64URLSafeString(text.getBytes("UTF-8"));
	}

	/**
	 * @param base64String the base64 string to decode
	 * @return a decoded string
	 */
	public final static String base64decode(String base64String) {
		if (isEmpty(base64String))
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

	public static int compareNullHashCode(Object o1, Object o2) {
		if (o1 == null) {
			if (o2 == null)
				return 0;
			return -1;
		}
		if (o2 == null)
			return 1;
		return o2.hashCode() - o1.hashCode();
	}

	public static String leftPad(int value, int size) {
		return org.apache.commons.lang3.StringUtils.leftPad(Integer.toString(value), size, '0');
	}

	public static String leftPad(long value, int size) {
		return org.apache.commons.lang3.StringUtils.leftPad(Long.toString(value), size, '0');
	}

	public static String detect(CharsetDetector detector) {
		final CharsetMatch match = detector.detect();
		return match == null ? null : match.getName();
	}

	public static String charsetDetector(InputStream inputStream) throws IOException {
		CharsetDetector detector = new CharsetDetector();
		detector.setText(inputStream);
		return detect(detector);
	}

	public static String charsetDetector(byte[] bytes) {
		CharsetDetector detector = new CharsetDetector();
		detector.setText(bytes);
		return detect(detector);
	}

	public static String[] toStringArray(Collection<? extends Object> collection, boolean sort) {
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

	public static String fastConcat(final CharSequence... charSeqs) {
		if (charSeqs == null)
			return null;
		StringBuilder sb = new StringBuilder();
		for (CharSequence charSeq : charSeqs)
			sb.append(charSeq);
		return sb.toString();
	}

	public static CharSequence fastConcatCharSequence(final Object... objects) {
		if (objects == null)
			return null;
		if (objects.length == 1)
			return objects[0].toString();
		StringBuilder sb = new StringBuilder();
		for (Object object : objects)
			if (object != null)
				sb.append(object);
		return sb;
	}

	public static String fastConcat(final Object... objects) {
		CharSequence cs = fastConcatCharSequence(objects);
		return cs == null ? null : cs.toString();
	}

	public final static String LINE_SEPARATOR = System.getProperty("line.separator");

	public static String[] splitLines(String str) {
		return split(str, LINE_SEPARATOR);
	}

	public static String htmlWrap(String text, int wrapLength) {
		if (isEmpty(text))
			return text;
		if (text.length() < wrapLength)
			return text;
		text = replace(text, "&shy;", "");
		return WordUtils.wrap(text, wrapLength, "&shy;", true);
	}

	public static String htmlWrapReduce(String text, int wrapLength, int maxSize) {
		if (isEmpty(text))
			return text;
		if (text.length() < maxSize)
			return text;
		text = replace(text, "&shy;", "");
		text = WordUtils.wrap(text, wrapLength, "\u00AD", true);
		String[] frags = split(text, '\u00AD');
		StringBuilder sb = new StringBuilder();
		int l = frags[0].length();
		for (int i = frags.length - 1; i > 0; i--) {
			String frag = frags[i];
			l += frag.length();
			if (l >= maxSize)
				break;
			sb.insert(0, frag);
		}
		sb.insert(0, '…');
		sb.insert(0, frags[0]);
		return sb.toString();
	}

	public static String joinWithSeparator(final String separator, final Object... objects) {
		if (objects == null)
			throw new IllegalArgumentException("Object varargs must not be null");
		final StringBuilder result = new StringBuilder();
		int i = 1;
		for (Object object : objects) {
			if (object == null)
				continue;
			final String value = object.toString();
			if (!value.equals(separator))
				result.append(value);
			if (!value.endsWith(separator) && i != objects.length)
				result.append(separator);
			i++;
		}
		return result.toString();
	}

	public static String urlHostPathWrapReduce(final String url, final int maxSize) {
		final URL u;
		try {
			u = new URL(url);
		} catch (MalformedURLException e) {
			return url;
		}
		final String path = joinWithSeparator("/", u.getHost(), u.getPath());
		final String[] frags = split(path, '/');
		if (frags.length < 2)
			return frags[0];
		int startPos = 1;
		int endPos = frags.length - 2;
		final StringBuilder sbStart = new StringBuilder(frags[0]);
		final StringBuilder sbEnd = new StringBuilder(frags[frags.length - 1]);
		final int length = sbStart.length() + sbEnd.length();
		for (; ; ) {
			boolean bHandled = false;
			if (startPos != -1 && startPos < endPos) {
				if (frags[startPos].length() + length < maxSize) {
					sbStart.append('/');
					sbStart.append(frags[startPos++]);
					bHandled = true;
				}
			}
			if (endPos != -1 && endPos > startPos) {
				if (frags[endPos].length() + length < maxSize) {
					sbEnd.insert(0, '/');
					sbEnd.insert(0, frags[endPos--]);
					bHandled = true;
				}
			}
			if (!bHandled)
				break;
		}
		return joinWithSeparator("/", sbStart, "…", sbEnd);
	}

}
