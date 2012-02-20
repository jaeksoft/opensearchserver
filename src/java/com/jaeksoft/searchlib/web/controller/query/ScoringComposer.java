/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
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

import org.zkoss.zk.ui.event.Event;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.scoring.AdvancedScore;
import com.jaeksoft.searchlib.scoring.AdvancedScoreItem;
import com.jaeksoft.searchlib.web.controller.CommonComposer;

public class ScoringComposer extends CommonComposer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2524402475854940710L;

	private AdvancedScoreItem currentScoreItem;

	private AdvancedScoreItem selectedScoreItem;

	@Override
	protected void reset() throws SearchLibException {
		currentScoreItem = null;
		selectedScoreItem = null;
		reloadPage();
	}

	public AdvancedScore getAdvancedScore() throws SearchLibException {
		SearchRequest searchRequest = (SearchRequest) getRequest();
		if (searchRequest == null)
			return null;
		AdvancedScore av = searchRequest.getAdvancedScore();
		if (av != null)
			return av;
		av = new AdvancedScore();
		searchRequest.setAdvancedScore(av);
		return av;
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
			if (schemaField.isIndexed())
				fieldList.add(schemaField.getName());
		return fieldList;
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

	public void onSave() throws SearchLibException {
		AdvancedScore advancedScore = getAdvancedScore();
		if (selectedScoreItem == null)
			advancedScore.add(currentScoreItem);
		else
			selectedScoreItem.copy(currentScoreItem);
		onCancel();
	}

	public void onCancel() throws SearchLibException {
		reset();
	}

	public void onRemove(Event event) throws SearchLibException {
		AdvancedScoreItem scoreItem = (AdvancedScoreItem) getListItemValue(event);
		getAdvancedScore().remove(scoreItem);
		reloadPage();
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
	 */
	public void setSelectedScoreItem(AdvancedScoreItem selectedScoreItem) {
		this.selectedScoreItem = selectedScoreItem;
		currentScoreItem = new AdvancedScoreItem(selectedScoreItem);
		reloadPage();
	}
}
