/**   
 *
 * Copyright (C) 2013 Emmanuel Keller / Jaeksoft
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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

public class RegExpUtils {

	private final static String[] REGEXPS = { "(?s)<div id=\"filAriane\">.*?<span itemprop=\"title\">.*?<span itemprop=\"title\">(.*?)</span>" };
	private final static String FILE = "/Users/ekeller/Desktop/test.html";

	public final static void main(String[] args) throws FileNotFoundException,
			IOException {
		String text = IOUtils.toString(new FileReader(FILE));
		for (String regExp : REGEXPS) {
			Pattern pattern = Pattern.compile(regExp);
			for (String group : getGroups(pattern, text))
				System.out.println("FIND: " + group);
		}
	}

	public static interface MatchGroupListener {

		void match(int start, int end);

		void group(int start, int end, String content);
	}

	public static Matcher groupExtractor(Pattern pattern, String text,
			MatchGroupListener matchGroupListener) {
		Matcher matcher;
		synchronized (pattern) {
			matcher = pattern.matcher(text);
		}
		while (matcher.find()) {
			matchGroupListener.match(matcher.start(), matcher.end());
			int l = matcher.groupCount();
			for (int i = 1; i <= l; i++)
				matchGroupListener.group(matcher.start(i), matcher.end(i),
						matcher.group(i));
		}
		return matcher;
	}

	public static class ListMatcher implements MatchGroupListener {

		private final List<String> list;

		public ListMatcher(List<String> list) {
			this.list = list;
		}

		@Override
		public void match(int start, int end) {
		}

		@Override
		public void group(int start, int end, String content) {
			list.add(content);
		}

	}

	public static List<String> getGroups(Pattern pattern, String text) {
		List<String> list = new ArrayList<String>(0);
		groupExtractor(pattern, text, new ListMatcher(list));
		return list;
	}

	public static String replaceAll(String source, Pattern pattern,
			String replace) {
		Matcher matcher;
		synchronized (pattern) {
			matcher = pattern.matcher(source);
		}
		return matcher.replaceAll(replace);
	}

	public static String replaceFirst(String source, Pattern pattern,
			String replace) {
		Matcher matcher;
		synchronized (pattern) {
			matcher = pattern.matcher(source);
		}
		return matcher.replaceFirst(replace);
	}

	public static Matcher[] wildcardMatcherArray(String patternsText)
			throws IOException {
		if (patternsText == null)
			return null;
		if (patternsText.length() == 0)
			return null;
		StringReader sr = null;
		BufferedReader br = null;
		List<Matcher> matcherList = new ArrayList<Matcher>(0);
		try {
			sr = new StringReader(patternsText);
			br = new BufferedReader(sr);
			String line;
			while ((line = br.readLine()) != null)
				matcherList.add(StringUtils.wildcardPattern(line).matcher(""));
			return matcherList.toArray(new Matcher[matcherList.size()]);
		} finally {
			if (br != null)
				IOUtils.closeQuietly(br);
			if (sr != null)
				IOUtils.closeQuietly(sr);
		}
	}

	public static boolean find(CharSequence input, Matcher[] matcherArray) {
		for (Matcher matcher : matcherArray) {
			synchronized (matcher) {
				matcher.reset(input);
				if (matcher.find())
					return true;
			}
		}
		return false;
	}

}
