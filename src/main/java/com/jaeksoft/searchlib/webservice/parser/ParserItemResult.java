/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.webservice.parser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.jaeksoft.searchlib.analysis.ClassProperty;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.parser.ParserFactory;
import com.jaeksoft.searchlib.parser.ParserFieldEnum;
import com.jaeksoft.searchlib.parser.ParserType;
import com.jaeksoft.searchlib.webservice.CommonResult;

@XmlRootElement(name = "result")
@JsonInclude(Include.NON_NULL)
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class ParserItemResult extends CommonResult {

	public final String name;
	public final Map<String, PropertyDefinition> properties;
	public final String[] fields;

	public ParserItemResult() {
		name = null;
		properties = null;
		fields = null;
	}

	public ParserItemResult(ParserType parserType, ParserFactory parserFactory) {
		name = parserType.getName();
		List<ClassProperty> props = parserFactory.getUserProperties();
		if (props == null) {
			properties = null;
			fields = null;
			return;
		}
		properties = new HashMap<String, PropertyDefinition>();
		for (ClassProperty prop : props) {
			ClassPropertyEnum propEnum = prop.getClassPropertyEnum();
			properties.put(propEnum.getName(), new PropertyDefinition(prop,
					propEnum));
		}
		ParserFieldEnum[] fieldList = parserFactory.getFieldList();
		if (fieldList == null)
			fields = null;
		else {
			fields = new String[fieldList.length];
			int i = 0;
			for (ParserFieldEnum field : fieldList)
				fields[i++] = field.name();
		}
	}

	@JsonInclude(Include.NON_EMPTY)
	@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
	public static class PropertyDefinition {

		public final String label;
		public final String description;
		public final Object[] possibleValues;
		public final String defaultValue;

		public PropertyDefinition() {
			label = null;
			description = null;
			possibleValues = null;
			defaultValue = null;
		}

		public PropertyDefinition(ClassProperty prop, ClassPropertyEnum propEnum) {
			label = propEnum.getLabel();
			description = propEnum.getInfo();
			possibleValues = prop.getValueList();
			defaultValue = prop.getValue();
		}
	}

}
