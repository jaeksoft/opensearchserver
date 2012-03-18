/**   
 *
 * Copyright (C) 2009-2012 Emmanuel Keller / Jaeksoft
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

import java.text.DecimalFormat;
import java.util.regex.Matcher;
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
		StringBuffer target = new StringBuffer();
		int l = source.length();
		boolean consecutiveSpace = false;
		for (int i = 0; i < l; i++) {
			char c = source.charAt(i);
			if (Character.isWhitespace(c)) {
				if (!consecutiveSpace) {
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

	public static void main(String[] args) {
		String text = "<p style=\"text-align: right;\"><em>Travailler   dans la tolérance et en cohérence    apportera respect à tous</em><br />Florence, directrice</p><p>Accueil des enfants du lundi au vendredi de 7h30 à 18h30. La crèche ferme une semaine à Noël et quatre semaines en été.</p>";
		System.out.println(removeTag(text));
		text = "<p style=\"text-align: right;\"><em>Travailler     dans la tolérance    et en cohérence apportera   respect à tous</em><br />Florence, directrice</p><p>Accueil des enfants du lundi au vendredi de 7h30 à 18h30. La crèche ferme une semaine à Noël et quatre semaines en été.</p>";
		System.out.println(removeTag(text));
		text = "test<script>script content</SCRIPT>test<object>object    content</object>test";
		System.out.println(removeScriptObjectStylePattern.matcher(text)
				.replaceAll(""));
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
			for (String tag : allowedTags) {
				if (tag.equals(group)) {
					allowed = true;
					break;
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

	public final static int compareNullValues(Object v1, Object v2) {
		if (v1 == null) {
			if (v2 == null)
				return 0;
			return -1;
		}
		if (v2 == null)
			return 1;
		return 0;
	}

	public final static String leftPad(int value, int size) {
		return org.apache.commons.lang.StringUtils.leftPad(
				Integer.toString(value), size, '0');
	}

	public final static String leftPad(long value, int size) {
		return org.apache.commons.lang.StringUtils.leftPad(
				Long.toString(value), size, '0');
	}
}
