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

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.common.database.TimeInterval;
import com.jaeksoft.searchlib.filter.FilterAbstract;
import com.jaeksoft.searchlib.filter.FilterAbstract.FilterType;
import com.jaeksoft.searchlib.filter.GeoFilter;
import com.jaeksoft.searchlib.filter.GeoFilter.CoordUnit;
import com.jaeksoft.searchlib.filter.GeoFilter.Type;
import com.jaeksoft.searchlib.filter.GeoFilter.Unit;
import com.jaeksoft.searchlib.filter.QueryFilter;
import com.jaeksoft.searchlib.request.AbstractRequest;
import com.jaeksoft.searchlib.request.RequestInterfaces;
import com.jaeksoft.searchlib.request.RequestTypeEnum;

public class FiltersController extends AbstractQueryController {

	private FilterAbstract<?> currentItem;

	private FilterAbstract<?> selectedItem;

	private FilterType filterType;

	@Override
	protected void reset() throws SearchLibException {
		selectedItem = null;
		filterType = FilterAbstract.FilterType.QUERY_FILTER;
		currentItem = new QueryFilter();
	}

	public FiltersController() throws SearchLibException {
		super(RequestTypeEnum.SearchRequest,
				RequestTypeEnum.SearchFieldRequest,
				RequestTypeEnum.MoreLikeThisRequest);
		reset();
	}

	public FilterAbstract.FilterType[] getFilterTypeList() {
		return FilterAbstract.FilterType.values();
	}

	public FilterAbstract<?> getCurrent() {
		return currentItem;
	}

	public boolean isSelection() {
		return selectedItem != null;
	}

	public boolean isNotSelection() {
		return !isSelection();
	}

	public FilterAbstract<?> getSelected() {
		return selectedItem;
	}

	@NotifyChange("*")
	public void setSelected(FilterAbstract<?> item) {
		this.selectedItem = item;
		this.filterType = item.getFilterType();
		this.currentItem = item.duplicate();
	}

	private RequestInterfaces.FilterListInterface getFilterListInterface()
			throws SearchLibException {
		AbstractRequest req = getAbstractRequest();
		if (req instanceof RequestInterfaces.FilterListInterface)
			return (RequestInterfaces.FilterListInterface) req;
		return null;
	}

	@Command
	public void onCancel() throws SearchLibException {
		reset();
		reload();
	}

	@Command
	public void onSave() throws SearchLibException {
		if (selectedItem != null)
			currentItem.copyTo(selectedItem);
		else
			getFilterListInterface().getFilterList().add(currentItem);
		onCancel();
	}

	@Command
	public void onRemove(@BindingParam("filter") FilterAbstract<?> filter)
			throws SearchLibException {
		getFilterListInterface().getFilterList().remove(filter);
		onCancel();
	}

	/**
	 * @return the filterType
	 */
	public FilterAbstract.FilterType getFilterType() {
		return filterType;
	}

	/**
	 * @param filterType
	 *            the filterType to set
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	@NotifyChange("currentTemplate")
	public void setFilterType(FilterAbstract.FilterType filterType)
			throws InstantiationException, IllegalAccessException {
		this.filterType = filterType;
		if (filterType != null)
			currentItem = filterType.newInstance();
	}

	public String getCurrentTemplate() {
		if (filterType == null)
			return null;
		return filterType.getTemplatePath();
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

	public TimeInterval.IntervalUnit[] getIntervalUnits() {
		return TimeInterval.IntervalUnit.values();
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
