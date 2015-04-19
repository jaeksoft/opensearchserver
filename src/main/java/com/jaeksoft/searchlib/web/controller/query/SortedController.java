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

import org.zkoss.bind.BindContext;
import org.zkoss.bind.Converter;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.join.JoinItem;
import com.jaeksoft.searchlib.join.JoinList;
import com.jaeksoft.searchlib.request.AbstractSearchRequest;
import com.jaeksoft.searchlib.schema.Indexed;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.sort.SortField;
import com.jaeksoft.searchlib.util.StringUtils;

@AfterCompose(superclass = true)
public class SortedController extends AbstractQueryController {

	private transient int selectedJoinNumber;

	private transient String selectedSort;

	private transient String selectedDirection;

	private transient String selectedNull;

	private transient SortField selectedSortField;

	public SortedController() throws SearchLibException {
		super();
	}

	@Override
	protected void reset() throws SearchLibException {
		selectedSort = null;
		selectedJoinNumber = 0;
		selectedDirection = null;
		selectedSortField = null;
	}

	public boolean isSelected() {
		return selectedSortField != null;
	}

	@NotifyChange({ "selectedJoinSelect", "selectedDirection", "selectedSort",
			"selectedNull" })
	public void setSelectedSortField(SortField sortField)
			throws SearchLibException {
		this.selectedSortField = sortField;
		this.selectedSort = sortField.getName();
		this.selectedJoinNumber = sortField.getJoinNumber();
		this.selectedDirection = sortField.getDirection();
		this.selectedNull = NULLS_ARRAY[sortField.isNullFirst() ? 1 : 0];
		reload();
	}

	public SortField getSelectedSortField() {
		return selectedSortField;
	}

	public void setSelectedSort(String value) {
		selectedSort = value;
	}

	public String getSelectedSort() {
		return selectedSort;
	}

	public void setSelectedDirection(String value) {
		selectedDirection = value;
	}

	public String getSelectedDirection() {
		return selectedDirection;
	}

	public void setSelectedNull(String value) {
		selectedNull = value;
	}

	public String getSelectedNull() {
		return selectedNull;
	}

	@NotifyChange("sortFieldList")
	public void setSelectedJoinNumber(int joinNumber) {
		selectedJoinNumber = joinNumber;
		resize();
	}

	public int getSelectedJoinNumber() {
		return selectedJoinNumber;
	}

	public List<Integer> getJoinNumberList() throws SearchLibException {
		AbstractSearchRequest searchRequest = (AbstractSearchRequest) getRequest();
		if (searchRequest == null)
			return null;
		List<Integer> joinNumberList = new ArrayList<Integer>(1);
		joinNumberList.add(0);
		JoinList joinList = searchRequest.getJoinList();
		if (joinList == null)
			return joinNumberList;
		for (JoinItem joinItem : joinList)
			joinNumberList.add(joinItem.getPosition());
		return joinNumberList;
	}

	@Command
	public void onSortAdd() throws SearchLibException {
		if (selectedSort == null)
			return;
		AbstractSearchRequest request = (AbstractSearchRequest) getRequest();
		if (request == null)
			return;
		request.getSortFieldList()
				.put(new SortField(
						selectedJoinNumber,
						selectedSort,
						DIRECTIONS_ARRAY[1].equalsIgnoreCase(selectedDirection),
						NULLS_ARRAY[1].equalsIgnoreCase(selectedNull)));
		onSortCancel();
	}

	@Command
	public void onSortSave() throws SearchLibException {
		if (selectedSortField == null)
			return;
		AbstractSearchRequest request = (AbstractSearchRequest) getRequest();
		if (request == null)
			return;
		selectedSortField.setJoinNumber(selectedJoinNumber);
		selectedSortField.setName(selectedSort);
		selectedSortField.setDirection(selectedDirection);
		selectedSortField.setNullFirst(NULLS_ARRAY[1].equals(selectedNull));
		request.getSortFieldList().rebuildCacheKey();
		onSortCancel();
	}

	@Command
	public void onSortCancel() throws SearchLibException {
		reset();
		reload();
	}

	@Command
	public void onSortRemove(@BindingParam("sortfield") SortField sortField)
			throws SearchLibException {
		AbstractSearchRequest request = (AbstractSearchRequest) getRequest();
		if (request == null)
			return;
		request.getSortFieldList().remove(sortField.getName());
		onSortCancel();
	}

	@Command
	public void onSortUp(@BindingParam("sortfield") SortField sortField)
			throws SearchLibException {
		AbstractSearchRequest request = (AbstractSearchRequest) getRequest();
		if (request == null)
			return;
		request.getSortFieldList().moveUp(sortField);
		reload();
	}

	@Command
	public void onSortDown(@BindingParam("sortfield") SortField sortField)
			throws SearchLibException {
		AbstractSearchRequest request = (AbstractSearchRequest) getRequest();
		if (request == null)
			return;
		request.getSortFieldList().moveDown(sortField);
		reload();
	}

	private List<String> getFieldList(Client client) {
		if (client == null)
			return null;
		List<String> sortFieldList = new ArrayList<String>();
		for (SchemaField field : client.getSchema().getFieldList())
			if (field.checkIndexed(Indexed.YES))
				sortFieldList.add(field.getName());
		sortFieldList.add("score");
		sortFieldList.add("__distance__");
		return sortFieldList;
	}

	private JoinItem getJoinItem(int joinNumber) throws SearchLibException {
		AbstractSearchRequest searchRequest = (AbstractSearchRequest) getRequest();
		if (searchRequest == null)
			return null;
		JoinList joinList = searchRequest.getJoinList();
		if (joinList == null)
			return null;
		JoinItem[] joinItems = joinList.getArray();
		if (joinItems == null)
			return null;
		if (joinNumber >= joinItems.length)
			return null;
		return joinItems[joinNumber];
	}

	public List<String> getSortFieldList() throws SearchLibException {
		Client client = null;
		if (selectedJoinNumber == 0)
			client = getClient();
		else {
			JoinItem joinItem = getJoinItem(selectedJoinNumber - 1);
			if (joinItem == null)
				return null;
			client = ClientCatalog.getClient(joinItem.getIndexName());
		}
		return getFieldList(client);
	}

	final static public String[] DIRECTIONS_ARRAY = { "ascending", "descending" };

	public String[] getDirectionList() {
		return DIRECTIONS_ARRAY;
	}

	final static public String[] NULLS_ARRAY = { "empty last", "empty first" };

	public String[] getNullList() {
		return NULLS_ARRAY;
	}

	@Override
	@Command
	public void reload() throws SearchLibException {
		super.reload();
	}

	@Override
	@GlobalCommand
	public void eventSchemaChange(Client client) throws SearchLibException {
		reload();
	}

	private final NullFirstConverter nullFirstConverter = new NullFirstConverter();

	public NullFirstConverter getNullFirstConverter() {
		return nullFirstConverter;
	}

	private final JoinLabelConverter joinLabelConverter = new JoinLabelConverter();

	public JoinLabelConverter getJoinLabelConverter() {
		return joinLabelConverter;
	}

	public class NullFirstConverter implements
			Converter<Object, Object, Component> {

		@Override
		public Object coerceToBean(Object value, Component component,
				BindContext ctx) {
			return IGNORED_VALUE;
		}

		@Override
		public Object coerceToUi(Object value, Component component,
				BindContext ctx) {
			if (value == null)
				return IGNORED_VALUE;
			return ((Boolean) value) ? "First" : "Last";
		}
	}

	public class JoinLabelConverter implements
			Converter<Object, Object, Component> {

		@Override
		public Object coerceToBean(Object value, Component component,
				BindContext ctx) {
			return IGNORED_VALUE;
		}

		@Override
		public Object coerceToUi(Object value, Component component,
				BindContext ctx) {
			if (value == null)
				return IGNORED_VALUE;

			Integer joinNumber = (Integer) value;

			try {

				if (joinNumber == 0)
					return getIndexName();

				JoinItem joinItem = getJoinItem(joinNumber - 1);
				if (joinItem == null)
					return IGNORED_VALUE;
				return StringUtils.fastConcat(joinItem.getIndexName(), " - ",
						joinItem.getParamPosition());

			} catch (SearchLibException e) {
				return IGNORED_VALUE;
			}

		}
	}

}
