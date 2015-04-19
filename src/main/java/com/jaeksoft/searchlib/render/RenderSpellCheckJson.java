/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.render;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;

import com.jaeksoft.searchlib.request.SpellCheckRequest;
import com.jaeksoft.searchlib.result.ResultSpellCheck;
import com.jaeksoft.searchlib.spellcheck.SpellCheck;
import com.jaeksoft.searchlib.spellcheck.SpellCheckItem;
import com.jaeksoft.searchlib.spellcheck.SuggestionItem;

public class RenderSpellCheckJson extends
		AbstractRenderJson<SpellCheckRequest, ResultSpellCheck> {

	private Boolean indent = null;

	public RenderSpellCheckJson(ResultSpellCheck result, Boolean indent) {
		super(result);
		this.indent = indent;
	}

	@SuppressWarnings("unchecked")
	private void renderSpellCheck(SpellCheck spellCheck,
			ArrayList<JSONObject> jsonSpellCheckList) throws Exception {
		for (SpellCheckItem spellCheckItem : spellCheck) {
			JSONObject jsonSpellCheck = new JSONObject();
			jsonSpellCheck.put("name", spellCheckItem.getWord());
			ArrayList<JSONObject> jsonSpellcheckWords = new ArrayList<JSONObject>();
			for (SuggestionItem suggest : spellCheckItem.getSuggestions()) {
				JSONObject jsonSpellSuggest = new JSONObject();
				jsonSpellSuggest.put("suggest", suggest.getTerm());
				jsonSpellSuggest.put("freq", suggest.getFreq());
				jsonSpellcheckWords.add(jsonSpellSuggest);
			}
			jsonSpellCheck.put("suggestions", jsonSpellcheckWords);
			jsonSpellCheckList.add(jsonSpellCheck);
		}

	}

	@SuppressWarnings("unchecked")
	private void renderSpellChecks(JSONObject jsonResponse) throws Exception {
		List<SpellCheck> spellChecklist = result.getSpellCheckList();
		ArrayList<JSONObject> jsonSpellCheckArray = new ArrayList<JSONObject>();
		if (spellChecklist == null)
			return;

		for (SpellCheck spellCheck : spellChecklist) {
			JSONObject jsonSpellCheck = new JSONObject();
			ArrayList<JSONObject> jsonSpellcheckList = new ArrayList<JSONObject>();
			String fieldName = spellCheck.getFieldName();
			jsonSpellCheck.put("fieldName", fieldName);
			renderSpellCheck(spellCheck, jsonSpellcheckList);
			jsonSpellCheck.put("word", jsonSpellcheckList);
			jsonSpellCheckArray.add(jsonSpellCheck);
		}
		jsonResponse.put("spellcheck", jsonSpellCheckArray);
	}

	@Override
	public void render() throws Exception {
		JSONObject json = new JSONObject();
		renderPrefix(json, request.getQueryString());
		renderSpellChecks(json);
		if (indent)
			writer.println(new org.json.JSONObject(json.toJSONString())
					.toString(4));
		else
			writer.println(json);
	}
}
