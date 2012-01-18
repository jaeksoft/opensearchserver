/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.spellcheck;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.cache.CacheKeyInterface;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.schema.Field;
import com.jaeksoft.searchlib.schema.FieldList;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class SpellCheckField extends Field implements CacheKeyInterface<Field> {

	private float minScore;

	private int suggestionNumber;

	public SpellCheckField() {
		minScore = 0.5F;
		suggestionNumber = 5;
	}

	protected SpellCheckField(SpellCheckField field) {
		super(field);
		this.minScore = field.minScore;
		this.suggestionNumber = field.suggestionNumber;
	}

	public SpellCheckField(String name, float minScore, int suggestionNumber) {
		super(name);
		this.minScore = minScore;
		this.suggestionNumber = suggestionNumber;
	}

	@Override
	public Field duplicate() {
		return new SpellCheckField(this);
	}

	public float getMinScore() {
		return minScore;
	}

	public void setMinScore(float score) {
		minScore = score;
	}

	public int getSuggestionNumber() {
		return suggestionNumber;
	}

	public void setSuggestionNumber(int n) {
		suggestionNumber = n;
	}

	public static void copySpellCheckFields(Node node,
			FieldList<SchemaField> source, FieldList<SpellCheckField> target) {
		String fieldName = XPathParser.getAttributeString(node, "name");
		String p = XPathParser.getAttributeString(node, "minScore");
		float minScore = 0.5F;
		if (p != null)
			if (p.length() > 0)
				minScore = Float.parseFloat(p);
		int suggestionNumber = XPathParser.getAttributeValue(node,
				"suggestionNumber");
		SpellCheckField spellCheckField = new SpellCheckField(source.get(
				fieldName).getName(), minScore, suggestionNumber);
		target.add(spellCheckField);
	}

	public static SpellCheckField buildSpellCheckField(String value,
			boolean multivalued) throws SyntaxError {
		float minScore = 0.5F;
		int suggestionNumber = 5;
		String fieldName = null;

		int i1 = value.indexOf('(');
		if (i1 != -1) {
			fieldName = value.substring(0, i1);
			int i2 = value.indexOf(')', i1);
			if (i2 == -1)
				throw new SyntaxError("closed braket missing");
			int i3 = value.indexOf(',', i2);
			if (i3 == -1)
				throw new SyntaxError("suggestion number missing");
			minScore = Float.parseFloat(value.substring(i1 + 1, i2));
			suggestionNumber = Integer.parseInt(value.substring(i2 + 1, i3));
		} else
			fieldName = value;

		return new SpellCheckField(fieldName, minScore, suggestionNumber);
	}

	@Override
	public void writeXmlConfig(XmlWriter xmlWriter) throws SAXException {
		xmlWriter.startElement("spellCheckField", "name", name, "minScore",
				Float.toString(minScore), "suggestionNumber", Integer
						.toString(suggestionNumber));
		xmlWriter.endElement();
	}

	@Override
	public int compareTo(Field o) {
		int i = super.compareTo(o);
		if (i != 0)
			return i;
		SpellCheckField f = (SpellCheckField) o;
		i = Float.compare(minScore, f.minScore);
		if (i != 0)
			return i;
		return suggestionNumber - f.suggestionNumber;
	}
}
