package com.jaeksoft.searchlib.web.controller.query;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.ForwardEvent;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.request.BoostQuery;
import com.jaeksoft.searchlib.request.SearchRequest;

public class BoostQueriesController extends AbstractQueryController {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7985584537309902023L;

	private BoostQuery currentBoostQuery;

	private BoostQuery selectedBoostQuery;

	public BoostQueriesController() throws SearchLibException {
		super();
		reset();
	}

	@Override
	protected void reset() throws SearchLibException {
		currentBoostQuery = new BoostQuery("", 1.0f);
		selectedBoostQuery = null;
	}

	/**
	 * @return the currentBoostQuery
	 */
	public BoostQuery getCurrentBoostQuery() {
		return currentBoostQuery;
	}

	/**
	 * @return the selectedBoostQuery
	 */
	public BoostQuery getSelectedBoostQuery() {
		return selectedBoostQuery;
	}

	/**
	 * @param selectedBoostQuery
	 *            the selectedBoostQuery to set
	 */
	public void setSelectedBoostQuery(BoostQuery selectedBoostQuery) {
		this.selectedBoostQuery = selectedBoostQuery;
		currentBoostQuery.copyFrom(selectedBoostQuery);
		reloadPage();
	}

	public boolean isSelected() {
		return selectedBoostQuery != null;
	}

	public boolean isNotSelected() {
		return !isSelected();
	}

	public void onCancel() throws SearchLibException {
		reset();
		reloadPage();
	}

	public void onSave() throws SearchLibException {
		SearchRequest request = getRequest();
		request.setBoostingQuery(selectedBoostQuery, currentBoostQuery);
		onCancel();
	}

	public void onRemove(Event event) throws SearchLibException {
		if (event instanceof ForwardEvent)
			event = ((ForwardEvent) event).getOrigin();
		BoostQuery boostQuery = (BoostQuery) event.getTarget().getAttribute(
				"boostQuery");
		SearchRequest request = getRequest();
		request.setBoostingQuery(boostQuery, null);
		onCancel();
	}
}
