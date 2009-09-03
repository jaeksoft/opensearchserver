/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.spellcheck;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.cache.CacheKeyInterface;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.schema.Field;
import com.jaeksoft.searchlib.schema.FieldList;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class SpellCheckField extends Field implements Externalizable,
		CacheKeyInterface<SpellCheckField> {

	private float minScore;

	public SpellCheckField() {
	}

	protected SpellCheckField(SpellCheckField field) {
		super(field);
		this.minScore = field.minScore;
	}

	public SpellCheckField(String name, float minScore) {
		super(name);
		this.minScore = minScore;
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

	public static void copySpellCheckFields(Node node,
			FieldList<SchemaField> source, FieldList<SpellCheckField> target) {
		String fieldName = XPathParser.getAttributeString(node, "name");
		String p = XPathParser.getAttributeString(node, "minScore");
		float minScore = 0.5F;
		if (p != null)
			if (p.length() > 0)
				minScore = Float.parseFloat(p);
		SpellCheckField spellCheckField = new SpellCheckField(source.get(
				fieldName).getName(), minScore);
		target.add(spellCheckField);
	}

	public static SpellCheckField buildSpellCheckField(String value,
			boolean multivalued) throws SyntaxError {
		float minScore = 0.5F;
		String fieldName = null;

		int i1 = value.indexOf('(');
		if (i1 != -1) {
			fieldName = value.substring(0, i1);
			int i2 = value.indexOf(')', i1);
			if (i2 == -1)
				throw new SyntaxError("closed braket missing");
			minScore = Float.parseFloat(value.substring(i1 + 1, i2));
		} else
			fieldName = value;

		return new SpellCheckField(fieldName, minScore);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super.readExternal(in);
		minScore = in.readFloat();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeFloat(minScore);
	}

	@Override
	public void writeXmlConfig(XmlWriter xmlWriter) throws SAXException {
		xmlWriter.startElement("spellCheckField", "name", name, "minScore",
				Float.toString(minScore));
		xmlWriter.endElement();
	}

	@Override
	public int compareTo(SpellCheckField o) {
		int i = name.compareTo(o.name);
		if (i != 0)
			return i;
		return Float.compare(minScore, o.minScore);
	}
}
