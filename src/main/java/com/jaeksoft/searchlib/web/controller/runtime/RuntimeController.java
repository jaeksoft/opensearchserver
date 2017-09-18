/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.web.controller.runtime;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.web.controller.CommonController;

public class RuntimeController extends CommonController {

	@Wire("#tabRuntime")
	Tabbox tabboxRuntime;

	private int selectedTabIndex = 0;

	public RuntimeController() throws SearchLibException {
		super();
	}

	@Override
	protected void reset() {
	}

	@Override
	@AfterCompose
	public void afterCompose(
			@ContextParam(ContextType.COMPONENT) Component component,
			@ContextParam(ContextType.VIEW) Component view) {
		super.afterCompose(component, view);
		Selectors.wireComponents(view, this, false);
		try {
			selectedTabIndex = isInstanceValid() ? 0 : 5;
		} catch (SearchLibException e) {
			Logging.warn(e);
		}
	}

	public boolean isCommandsRights() throws SearchLibException {
		return isAdminOrNoUser() && isInstanceValid();
	}

	/**
	 * @return the selectedTabIndex
	 */
	public int getSelectedTabIndex() {
		Tab selectedTab = tabboxRuntime.getSelectedTab();
		if (selectedTab == null || !selectedTab.isVisible())
			selectedTabIndex = 5;
		return selectedTabIndex;
	}

	/**
	 * @param selectedTabIndex
	 *            the selectedTabIndex to set
	 */
	public void setSelectedTabIndex(int selectedTabIndex) {
		this.selectedTabIndex = selectedTabIndex;
	}

}
