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
	private int maxDocBytes;
	private int maxSize;

	public HighlightField(Field field, String tag, int maxDocBytes, int maxSize) {
		super(field.getName());
		this.tag = tag;
		this.maxDocBytes = maxDocBytes;
		this.maxSize = maxSize;
	}

	public HighlightField(Field field) {
		this(field, "em", 0, 0);
	}

	public HighlightField(HighlightField field) {
		super(field.name);
		fragmenter = field.fragmenter;
		tag = field.tag;
		maxDocBytes = field.maxDocBytes;
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
		int maxDocBytes = XPathParser.getAttributeValue(node, "maxDocBytes");
		int maxSize = XPathParser.getAttributeValue(node, "maxSize");
		if (maxSize == 0)
			maxSize = 100;
		HighlightField field = new HighlightField(source.get(fieldName), tag,
				maxDocBytes, maxSize);
		int fragmentNumber = XPathParser.getAttributeValue(node,
				"maxFragmentNumber");
		if (fragmentNumber == 0)
			fragmentNumber = 1;

		String fragmentSeparator = XPathParser.getAttributeString(node,
				"separator");
		if (fragmentSeparator == null)
			fragmentSeparator = "...";
		field.setFragmenter(new SentenceFragmenter(fragmentNumber,
				fragmentSeparator));
		target.add(field);
	}

	private String[] getFragments(Request request, String content)
			throws IOException {
		Highlighter highlighter = new Highlighter(getNewFormater(),
				new DefaultEncoder(), new QueryScorer(request
						.getHighlightQuery()));
		if (maxDocBytes > 0)
			highlighter.setMaxDocBytesToAnalyze(maxDocBytes);
		Fragmenter frgmtr = fragmenter.newFragmenter();
		highlighter.setTextFragmenter(frgmtr);
		return highlighter.getBestFragments(request.getConfig().getSchema()
				.getHighlightPerFieldAnalyzer(request.getLang()), name,
				content, frgmtr.getFragmentNumber());
	}

	private static String getSnippet(String[] frags, int maxSize,
			String separator) {
		StringBuffer sb = new StringBuffer();
		for (String frag : frags)
			if (frag != null) {
				if (sb.length() + frag.length() + 1 > maxSize)
					break;
				if (sb.length() > 0)
					sb.append(separator);
				sb.append(frag);
			}
		sb.trimToSize();
		return sb.length() == 0 ? null : sb.toString();
	}

	private static String getSnippet(ArrayList<String> frags, int maxSize,
			String separator) {
		String[] a = new String[frags.size()];
		return getSnippet(frags.toArray(a), maxSize, separator);
	}

	public void setSnippets(Request request, ArrayList<String> values)
			throws IOException {
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
				String snippet = getSnippet(frags, maxSize, " ");
				if (snippet != null)
					snippets.add(snippet);
			}
		}
		String snippet = getSnippet(snippets, maxSize, fragmenter
				.getSeparator());
		if (snippet == null)
			snippet = valueContent.toString().trim();
		addValue(snippet);
	}
}
