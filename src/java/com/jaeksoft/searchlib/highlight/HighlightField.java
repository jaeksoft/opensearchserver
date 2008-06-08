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

	public HighlightField(Field field, String tag, int maxDocBytes) {
		super(field.getName());
		this.tag = tag;
		this.maxDocBytes = maxDocBytes;
	}

	public HighlightField(Field field) {
		this(field, "em", 0);
	}

	public HighlightField(HighlightField field) {
		super(field.name);
		fragmenter = field.fragmenter;
		tag = field.tag;
		maxDocBytes = field.maxDocBytes;
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
		HighlightField field = new HighlightField(source.get(fieldName), tag,
				maxDocBytes);
		int fragmentSize = XPathParser.getAttributeValue(node, "maxSize");
		if (fragmentSize == 0)
			fragmentSize = 100;
		int fragmentNumber = XPathParser.getAttributeValue(node,
				"maxFragmentNumber");
		if (fragmentNumber == 0)
			fragmentNumber = 1;

		String fragmentSeparator = XPathParser.getAttributeString(node,
				"separator");
		if (fragmentSeparator == null)
			fragmentSeparator = "...";
		field.setFragmenter(new SentenceFragmenter(fragmentNumber,
				fragmentSize, fragmentSeparator));
		target.add(field);
	}

	private String getSnippet(Request request, String content)
			throws IOException {
		long t = System.currentTimeMillis();
		Highlighter highlighter = new Highlighter(getNewFormater(),
				new DefaultEncoder(), new QueryScorer(request
						.getHighlightQuery()));
		if (maxDocBytes > 0)
			highlighter.setMaxDocBytesToAnalyze(maxDocBytes);
		Fragmenter frgmtr = fragmenter.newFragmenter();
		String[] frags = highlighter.getBestFragments(request.getConfig()
				.getSchema().getHighlightPerFieldAnalyzer(request.getLang()),
				name, content, frgmtr.getFragmentNumber());
		String snippet = frgmtr.getSnippet(frags);
		if (snippet == null)
			snippet = fragmenter.format(content, fragmenter.getMaxSize())
					+ fragmenter.getSeparator();
		t = System.currentTimeMillis() - t;
		return snippet;
	}

	public void setSnippets(Request request, ArrayList<String> values)
			throws IOException {
		if (values == null)
			return;
		for (String value : values)
			addValue(getSnippet(request, value));
	}
}
