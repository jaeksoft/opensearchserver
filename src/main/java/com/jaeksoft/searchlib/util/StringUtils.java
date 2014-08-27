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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.text.WordUtils;

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
		if (isEmpty(text))
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

	public final static String fastConcat(final CharSequence... charSeqs) {
		if (charSeqs == null)
			return null;
		StringBuilder sb = new StringBuilder();
		for (CharSequence charSeq : charSeqs)
			sb.append(charSeq);
		return sb.toString();
	}

	public final static CharSequence fastConcatCharSequence(
			final Object... objects) {
		if (objects == null)
			return null;
		if (objects.length == 1)
			return objects.toString();
		StringBuilder sb = new StringBuilder();
		for (Object object : objects)
			if (object != null)
				sb.append(object);
		return sb;
	}

	public final static String fastConcat(final Object... objects) {
		CharSequence cs = fastConcatCharSequence(objects);
		return cs == null ? null : cs.toString();
	}

	public final static String LINE_SEPARATOR = System
			.getProperty("line.separator");

	public final static String[] splitLines(String str) {
		return split(str, LINE_SEPARATOR);
	}

	public final static String htmlWrap(String text, int wrapLength) {
		if (isEmpty(text))
			return text;
		if (text.length() < wrapLength)
			return text;
		text = replace(text, "&shy;", "");
		return WordUtils.wrap(text, wrapLength, "&shy;", true);
	}

	public final static String htmlWrapReduce(String text, int wrapLength,
			int maxSize) {
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

	public final static String urlHostPathWrapReduce(String url, int maxSize) {
		URL u;
		try {
			u = new URL(url);
		} catch (MalformedURLException e) {
			return url;
		}
		String path = fastConcat(u.getHost(), '/', u.getPath());
		String[] frags = split(path, '/');
		if (frags.length < 2)
			return path;
		int startPos = 1;
		int endPos = frags.length - 2;
		StringBuilder sbStart = new StringBuilder(frags[0]);
		StringBuilder sbEnd = new StringBuilder(frags[frags.length - 1]);
		int length = sbStart.length() + sbEnd.length();
		for (;;) {
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
		return fastConcat(sbStart, "/…/", sbEnd);
	}

	public static void main(String args[]) throws IOException {
		if (args != null && args.length == 2) {
			List<String> lines = FileUtils.readLines(new File(args[0]));
			FileWriter fw = new FileWriter(new File(args[1]));
			PrintWriter pw = new PrintWriter(fw);
			for (String line : lines)
				pw.println(StringEscapeUtils.unescapeHtml(line));
			pw.close();
			fw.close();
		}
		String text = "file://&shy;Users/ekeller/Moteur/infotoday_enterprisesearchsourcebook08/Open_on_Windows.exe";
		System.out.println(htmlWrap(text, 20));
		System.out.println(htmlWrapReduce(text, 20, 80));
		String url = "file://Users/ekeller/Moteur/infotoday_enterprisesearchsourcebook08/Open_on_Windows.exe?test=2";
		System.out.println(urlHostPathWrapReduce(url, 80));
	}
}
