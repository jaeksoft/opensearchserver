/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.webservice;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.jaeksoft.searchlib.result.ResultSpellCheck;
import com.jaeksoft.searchlib.spellcheck.SpellCheck;

@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class SpellChecking {

	@XmlElement(name = "field")
	public List<SpellCheckArray> field;

	public SpellChecking() {
		field = null;
	}

	public SpellChecking(ResultSpellCheck result, String query) {
		field = new ArrayList<SpellCheckArray>();
		for (SpellCheck spellCheck : result.getSpellCheckList()) {
			SpellCheckArray spellcheckarray = new SpellCheckArray(
					spellCheck.getFieldName(), query, spellCheck.getList());
			field.add(spellcheckarray);
		}

	}
}
