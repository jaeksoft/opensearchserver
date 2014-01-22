/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.web.controller.query;

import java.util.ArrayList;
import java.util.List;

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.schema.Indexed;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.scoring.AdvancedScore;
import com.jaeksoft.searchlib.scoring.AdvancedScoreItem;
import com.jaeksoft.searchlib.webservice.query.search.SearchQueryAbstract.Scoring.Type;

public class ScoringComposer extends AbstractQueryController {

	private AdvancedScoreItem currentScoreItem;

	private AdvancedScoreItem selectedScoreItem;

	public ScoringComposer() throws SearchLibException {
		super();
	}

	@Override
	protected void reset() throws SearchLibException {
		currentScoreItem = null;
		selectedScoreItem = null;
		reload();
	}

	public AdvancedScore getAdvancedScore() throws SearchLibException {
		AbstractSearchRequest searchRequest = (AbstractSearchRequest) getRequest();
		if (searchRequest == null)
			return null;
		return searchRequest.getAdvancedScore();
	}

	public AdvancedScoreItem getScoreItem() {
		if (currentScoreItem == null)
			currentScoreItem = new AdvancedScoreItem();
		return currentScoreItem;
	}

	/**
	 * @param scoreItem
	 *            the scoreItem to set
	 */
	public void setScoreItem(AdvancedScoreItem scoreItem) {
		currentScoreItem = scoreItem;
	}

	public List<String> getFieldList() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		List<String> fieldList = new ArrayList<String>();
		for (SchemaField schemaField : client.getSchema().getFieldList())
			if (schemaField.checkIndexed(Indexed.YES))
				fieldList.add(schemaField.getName());
		return fieldList;
	}

	public Type[] getTypeList() {
		return Type.values();
	}

	public String[] getDirectionList() {
		return AdvancedScoreItem.DIRECTION;
	}

	public boolean isSelected() {
		return selectedScoreItem != null;
	}

	public boolean isNotSelected() {
		return !isSelected();
	}

	@Command
	public void onSave() throws SearchLibException {
		AbstractSearchRequest searchRequest = (AbstractSearchRequest) getRequest();
		if (selectedScoreItem == null)
			searchRequest.addAdvancedScore(currentScoreItem);
		else
			selectedScoreItem.copy(currentScoreItem);
		onCancel();
	}

	@Command
	public void onCancel() throws SearchLibException {
		reset();
	}

	@Command
	public void onRemove(@BindingParam("item") AdvancedScoreItem scoreItem)
			throws SearchLibException {
		AbstractSearchRequest searchRequest = (AbstractSearchRequest) getRequest();
		searchRequest.removeAdvancedScore(scoreItem);
		reload();
	}

	/**
	 * @return the selectedScoreItem
	 */
	public AdvancedScoreItem getSelectedScoreItem() {
		return selectedScoreItem;
	}

	/**
	 * @param selectedScoreItem
	 *            the selectedScoreItem to set
	 * @throws SearchLibException
	 */
	public void setSelectedScoreItem(AdvancedScoreItem selectedScoreItem)
			throws SearchLibException {
		this.selectedScoreItem = selectedScoreItem;
		currentScoreItem = new AdvancedScoreItem(selectedScoreItem);
		reload();
	}
}
