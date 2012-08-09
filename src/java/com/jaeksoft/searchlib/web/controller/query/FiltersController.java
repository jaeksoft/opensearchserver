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

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Listbox;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.filter.FilterAbstract;
import com.jaeksoft.searchlib.filter.GeoFilter;
import com.jaeksoft.searchlib.filter.GeoFilter.CoordUnit;
import com.jaeksoft.searchlib.filter.GeoFilter.Type;
import com.jaeksoft.searchlib.filter.GeoFilter.Unit;
import com.jaeksoft.searchlib.filter.QueryFilter;
import com.jaeksoft.searchlib.request.AbstractRequest;
import com.jaeksoft.searchlib.request.RequestInterfaces;
import com.jaeksoft.searchlib.request.RequestTypeEnum;

public class FiltersController extends AbstractQueryController {

	/**
	 * 
	 */
	private static final long serialVersionUID = 989287631079056922L;

	private FilterAbstract<?> currentItem;

	private FilterAbstract<?> selectedItem;

	private String filterType;

	@Override
	protected void reset() throws SearchLibException {
		selectedItem = null;
		filterType = FilterAbstract.QUERY_FILTER;
		currentItem = new QueryFilter();
	}

	public FiltersController() throws SearchLibException {
		super(RequestTypeEnum.SearchRequest,
				RequestTypeEnum.MoreLikeThisRequest);
		reset();
	}

	public String[] getFilterTypeList() {
		return FilterAbstract.FILTER_TYPES;
	}

	public FilterAbstract<?> getCurrent() {
		return currentItem;
	}

	public boolean isSelected() {
		return selectedItem != null;
	}

	public boolean isNotSelected() {
		return !isSelected();
	}

	public FilterAbstract<?> getSelected() {
		return selectedItem;
	}

	private void reloadListbox() {
		Listbox listbox = (Listbox) getFellow("filterListbox");
		listbox.invalidate();
		reloadComponent(listbox);

	}

	public void setSelected(FilterAbstract<?> item) {
		this.selectedItem = item;
		this.currentItem = item.duplicate();
		reloadListbox();
	}

	private RequestInterfaces.FilterListInterface getFilterListInterface()
			throws SearchLibException {
		AbstractRequest req = getAbstractRequest();
		if (req instanceof RequestInterfaces.FilterListInterface)
			return (RequestInterfaces.FilterListInterface) req;
		return null;
	}

	public void onCancel() throws SearchLibException {
		reset();
		reloadListbox();
	}

	public void onSave() throws SearchLibException {
		if (selectedItem != null)
			currentItem.copyTo(selectedItem);
		else
			getFilterListInterface().getFilterList().add(currentItem);
		onCancel();
	}

	public void onRemove(Component comp) throws SearchLibException {
		FilterAbstract<?> filter = (FilterAbstract<?>) getRecursiveComponentAttribute(
				comp, "filterItem");
		getFilterListInterface().getFilterList().remove(filter);
		onCancel();
	}

	/**
	 * @return the filterType
	 */
	public String getFilterType() {
		return filterType;
	}

	/**
	 * @param filterType
	 *            the filterType to set
	 */
	public void setFilterType(String filterType) {
		this.filterType = filterType;
		if (FilterAbstract.QUERY_FILTER.equals(filterType))
			currentItem = new QueryFilter();
		else if (FilterAbstract.GEO_FILTER.equals(filterType))
			currentItem = new GeoFilter();
		reloadListbox();
	}

	public Type[] getGeoTypes() {
		return GeoFilter.Type.values();
	}

	public Unit[] getGeoUnits() {
		return GeoFilter.Unit.values();
	}

	public CoordUnit[] getGeoCoordUnits() {
		return GeoFilter.CoordUnit.values();
	}

	public List<String> getIndexedFieldList() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		List<String> fieldList = new ArrayList<String>();
		client.getSchema().getFieldList().getIndexedFields(fieldList);
		return fieldList;
	}

}
