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

	private transient JoinSelect selectedJoinSelect;

	private transient String selectedSort;

	private transient String selectedDirection;

	private transient String selectedNull;

	public class JoinSelect {

		public final JoinItem joinItem;
		public final int position;
		public final String indexName;
		public final String label;

		private JoinSelect(final JoinItem joinItem) throws SearchLibException {
			this.joinItem = joinItem;
			if (joinItem == null) {
				this.indexName = getIndexName();
				this.label = indexName;
				this.position = 0;
			} else {
				this.indexName = joinItem.getIndexName();
				this.label = StringUtils.fastConcat(indexName, " - ",
						joinItem.getParamPosition());
				this.position = joinItem.getPosition();
			}
		}

		@Override
		final public String toString() {
			return label;
		}
	}

	public SortedController() throws SearchLibException {
		super();
	}

	@Override
	protected void reset() throws SearchLibException {
		selectedSort = null;
		selectedJoinSelect = null;
		selectedDirection = null;
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
	public void setSelectedJoinSelect(JoinSelect joinSelect) {
		selectedJoinSelect = joinSelect;
		resize();
	}

	public JoinSelect getSelectedJoinSelect() {
		return selectedJoinSelect;
	}

	private List<JoinSelect> joinSelectList = null;

	public List<JoinSelect> getJoinSelectList() throws SearchLibException {
		AbstractSearchRequest searchRequest = (AbstractSearchRequest) getRequest();
		if (searchRequest == null)
			return null;
		joinSelectList = new ArrayList<JoinSelect>(1);
		joinSelectList.add(new JoinSelect(null));
		JoinList joinList = searchRequest.getJoinList();
		if (joinList == null)
			return joinSelectList;
		for (JoinItem joinItem : joinList)
			joinSelectList.add(new JoinSelect(joinItem));
		return joinSelectList;
	}

	@Command
	public void onSortAdd() throws SearchLibException {
		if (selectedSort == null || selectedJoinSelect == null)
			return;
		((AbstractSearchRequest) getRequest())
				.getSortFieldList()
				.put(new SortField(
						selectedJoinSelect.position,
						selectedSort,
						DIRECTIONS_ARRAY[1].equalsIgnoreCase(selectedDirection),
						NULLS_ARRAY[1].equalsIgnoreCase(selectedNull)));
		reload();
	}

	@Command
	public void onSortRemove(@BindingParam("sortfield") SortField sortField)
			throws SearchLibException {
		((AbstractSearchRequest) getRequest()).getSortFieldList().remove(
				sortField.getName());
		reload();
	}

	public List<String> getSortFieldList() throws SearchLibException {
		if (selectedJoinSelect == null)
			return null;
		Client client = ClientCatalog.getClient(selectedJoinSelect.indexName);
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
		selectedSort = null;
		selectedJoinSelect = null;
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
			return joinSelectList.get((Integer) value).label;
		}

	}

}
