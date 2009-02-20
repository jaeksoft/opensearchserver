/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.schema;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.jaeksoft.searchlib.util.External;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlInfo;
import com.jaeksoft.searchlib.util.External.Collecter;

public class FieldList<T extends Field> implements FieldSelector, XmlInfo,
		Externalizable, Iterable<T>, Collecter<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3706856755116432969L;

	private List<T> fieldList;
	private transient Map<String, T> fieldsName;
	private T defaultField;
	private T uniqueField;

	/**
	 * Constructeur de base.
	 */
	public FieldList() {
		this.fieldsName = new HashMap<String, T>();
		this.fieldList = new ArrayList<T>();
	}

	/**
	 * Ce constructeur cr�� une liste contenant les m�mes champs que la liste
	 * pass�e en param�tres (fl).
	 * 
	 * @param fl
	 */
	public FieldList(FieldList<T> fl) {
		this();
		add(fl);
	}

	public void add(FieldList<T> fl) {
		for (T field : fl)
			add(field);
	}

	/**
	 * Retourne le champ par d�fault du fichier de config XML. <gisearch><schema
	 * defaultField="nomchamp">...
	 * 
	 * @param document
	 * @param xPath
	 * @return Field
	 * @throws XPathExpressionException
	 */
	public T getDefaultField(Document document, XPathParser xpp)
			throws XPathExpressionException {
		Node node = xpp.getNode("/gisearch/schema");
		if (node == null)
			return null;
		return get(XPathParser.getAttributeString(node, "defaultField"));
	}

	/**
	 * Ajoute un champ � la liste
	 */
	public boolean add(T field) {
		if (!this.fieldList.add(field))
			return false;
		this.fieldsName.put(field.name, field);
		return true;
	}

	/**
	 * Renvoie le champ � la position "index"
	 */
	public T get(int index) {
		return fieldList.get(index);
	}

	/**
	 * Renvoie le champ nomm� "name"
	 * 
	 * @param name
	 * @return Field
	 */
	public T get(String name) {
		return fieldsName.get(name);
	}

	public T get(Field field) {
		return fieldsName.get(field.name);
	}

	/**
	 * Renvoie la taille de la liste de champs.
	 */
	public int size() {
		return fieldList.size();
	}

	public FieldSelectorResult accept(String fieldName) {
		if (this.fieldsName.containsKey(fieldName)) {
			return FieldSelectorResult.LOAD;
		}
		return FieldSelectorResult.NO_LOAD;
	}

	public String[] toArrayName() {
		Set<String> set = fieldsName.keySet();
		String[] names = new String[set.size()];
		return set.toArray(names);
	}

	protected void setDefaultField(String fieldName) {
		this.defaultField = this.get(fieldName);
	}

	protected void setUniqueField(String fieldName) {
		this.uniqueField = this.get(fieldName);
	}

	public T getDefaultField() {
		return this.defaultField;
	}

	public T getUniqueField() {
		return this.uniqueField;
	}

	@Override
	public String toString() {
		String s = null;
		for (Field f : fieldList) {
			if (s == null)
				s = f.name;
			else
				s += ", " + f.name;
		}
		return s;
	}

	public void xmlInfo(PrintWriter writer) {
		writer.print("<fields");
		if (defaultField != null)
			writer.print(" default=\"" + defaultField.getName() + "\"");
		if (uniqueField != null)
			writer.print(" unique=\"" + uniqueField.getName() + "\"");
		writer.println(">");
		for (Field field : this.fieldList)
			field.xmlInfo(writer);
		writer.println("</fields>");
	}

	public Iterator<T> iterator() {
		return this.fieldList.iterator();
	}

	public List<T> array() {
		return fieldList;
	}

	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		External.readCollection(in, this);
		defaultField = External.<T> readObject(in);
		uniqueField = External.<T> readObject(in);
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		External.writeCollection(fieldList, out);
		External.writeObject(defaultField, out);
		External.writeObject(uniqueField, out);
	}

	public void addObject(T field) {
		fieldList.add(field);
		fieldsName.put(field.name, field);
	}
}
