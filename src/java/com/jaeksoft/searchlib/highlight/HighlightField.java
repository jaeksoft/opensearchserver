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

package com.jaeksoft.searchlib.highlight;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.highlight.DefaultEncoder;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.w3c.dom.Node;

import com.jaeksoft.searchlib.request.Request;
import com.jaeksoft.searchlib.schema.Field;
import com.jaeksoft.searchlib.schema.FieldList;
import com.jaeksoft.searchlib.schema.FieldValue;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.util.XPathParser;

public class HighlightField extends FieldValue {

	private Fragmenter fragmenter;
	private String tag;
	private int maxDocChar;
	private int maxSize;

	public HighlightField(Field field, String tag, int maxDocChar, int maxSize) {
		super(field.getName());
		this.tag = tag;
		this.maxDocChar = maxDocChar;
		this.maxSize = maxSize;
	}

	public HighlightField(Field field) {
		this(field, "em", 0, 0);
	}

	public HighlightField(HighlightField field) {
		super(field.name);
		fragmenter = field.fragmenter;
		tag = field.tag;
		maxDocChar = field.maxDocChar;
		maxSize = field.maxSize;
	}

	@Override
	public Object clone() {
		return new HighlightField(this);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 4048179036729127707L;

	public void setFragmenter(Fragmenter fragmenter) {
		this.fragmenter = fragmenter;
	}

	private Formatter getNewFormater() {
		return new SimpleHTMLFormatter("<" + tag + ">", "</" + tag + ">");
	}

	/**
	 * Retourne la liste des champs "highlighter".
	 * 
	 * @param xPath
	 * @param node
	 * @param target
	 */
	public static void copyHighlightFields(Node node,
			FieldList<SchemaField> source, FieldList<HighlightField> target) {
		String fieldName = XPathParser.getAttributeString(node, "name");
		String tag = XPathParser.getAttributeString(node, "tag");
		if (tag == null)
			tag = "em";
		int maxDocChar = XPathParser.getAttributeValue(node, "maxDocBytes");
		if (maxDocChar == 0)
			XPathParser.getAttributeValue(node, "maxDocChar");
		int maxSize = XPathParser.getAttributeValue(node, "maxSize");
		if (maxSize == 0)
			maxSize = 100;
		HighlightField field = new HighlightField(source.get(fieldName), tag,
				maxDocChar, maxSize);
		int fragmentNumber = XPathParser.getAttributeValue(node,
				"maxFragmentNumber");
		if (fragmentNumber == 0)
			fragmentNumber = 1;

		String fragmentSeparator = XPathParser.getAttributeString(node,
				"separator");
		if (fragmentSeparator == null)
			fragmentSeparator = "...";

		field.setFragmenter(new Fragmenter(fragmentNumber, fragmentSeparator,
				maxSize));

		target.add(field);
	}

	private String[] getFragments(Request request, String content)
			throws IOException, ParseException {
		QueryScorer qs = new QueryScorer(request.getHighlightQuery(), name);

		Highlighter highlighter = new Highlighter(getNewFormater(),
				new DefaultEncoder(), qs);
		if (maxDocChar > 0)
			highlighter.setMaxDocCharsToAnalyze(maxDocChar);
		Fragmenter frgmtr = fragmenter.newFragmenter();
		highlighter.setTextFragmenter(frgmtr);
		return highlighter.getBestFragments(request.getConfig().getSchema()
				.getQueryPerFieldAnalyzer(request.getLang()), name, content,
				frgmtr.getFragmentNumber());
	}

	private static int getBestFragPos(String[] frags, int maxSize) {
		int best_distance = maxSize;
		int best = -1;
		int pos = 0;
		for (String frag : frags) {
			if (frag != null) {
				int distance = maxSize - frag.length();
				if (distance >= 0 && distance < best_distance) {
					best = pos;
					best_distance = distance;
				}
			}
			pos++;
		}
		return best;
	}

	private static String getBestFrag(String[] frags, int maxSize) {
		int pos = getBestFragPos(frags, maxSize);
		return (pos == -1) ? null : frags[pos];
	}

	private static String getSnippet(ArrayList<String> frags, int maxSize,
			String separator) {
		String[] fragsArray = new String[frags.size()];
		String[] finalArray = new String[frags.size()];
		frags.toArray(fragsArray);
		int pos;
		while ((pos = getBestFragPos(fragsArray, maxSize)) != -1) {
			String frag = fragsArray[pos];
			finalArray[pos] = frag;
			fragsArray[pos] = null;
			maxSize -= frag.length();
		}
		StringBuffer snippet = new StringBuffer();
		boolean bAddSep = false;
		for (String frag : finalArray) {
			if (frag == null)
				continue;
			if (bAddSep)
				snippet.append(separator);
			else
				bAddSep = true;
			snippet.append(frag);
		}
		return snippet.toString();
	}

	public void setSnippets(Request request, ArrayList<String> values)
			throws IOException, ParseException {
		if (values == null)
			return;
		ArrayList<String> snippets = new ArrayList<String>();
		StringBuffer valueContent = new StringBuffer();
		for (String value : values) {
			if (value.length() + valueContent.length() <= maxSize) {
				if (valueContent.length() > 0)
					valueContent.append(". ");
				valueContent.append(value);
			}
			String[] frags = getFragments(request, value);
			if (frags != null) {
				String snippet = getBestFrag(frags, maxSize);
				if (snippet != null)
					snippets.add(snippet);
			}
		}
		String snippet = getSnippet(snippets, maxSize, fragmenter
				.getSeparator());
		if (snippet == null || snippet.length() == 0)
			snippet = valueContent.toString().trim();
		addValue(snippet);
	}
}
