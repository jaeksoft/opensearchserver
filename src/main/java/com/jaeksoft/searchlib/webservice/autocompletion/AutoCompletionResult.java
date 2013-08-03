/**   
 * License Agreement for OpenSearchServer
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
package com.jaeksoft.searchlib.webservice.autocompletion;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jaeksoft.searchlib.autocompletion.AutoCompletionItem;
import com.jaeksoft.searchlib.result.AbstractResultSearch;
import com.jaeksoft.searchlib.result.ResultDocument;
import com.jaeksoft.searchlib.webservice.CommonResult;

@XmlRootElement(name = "result")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class AutoCompletionResult extends CommonResult {

	public List<String> terms;

	public AutoCompletionResult() {
		terms = null;
	}

	public AutoCompletionResult(AbstractResultSearch result) {
		super(true, null);
		if (result == null)
			return;
		if (result.getDocumentCount() <= 0)
			return;
		terms = new ArrayList<String>();
		for (ResultDocument document : result)
			terms.add(document.getValueContent(
					AutoCompletionItem.autoCompletionSchemaFieldTerm, 0));
	}

	public AutoCompletionResult(JSONObject json) throws JSONException,
			UnsupportedEncodingException, ParseException {
		terms = new ArrayList<String>(0);
		JSONObject jsonResult = json.getJSONObject("result");
		JSONArray array = jsonResult.optJSONArray("terms");
		if (array != null)
			addTerms(array);
		else
			addTerms(jsonResult.optString("terms"));
	}

	private void addTerms(JSONArray array) throws JSONException,
			ParseException, UnsupportedEncodingException {
		if (array == null)
			return;
		for (int i = 0; i < array.length(); i++)
			addTerms(array.getString(i));
	}

	private void addTerms(String term) throws JSONException, ParseException,
			UnsupportedEncodingException {
		if (term == null)
			return;
		if (term.length() == 0)
			return;
		terms.add(term);
	}

}
