/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft SearchLib Community.
 *
 * Jaeksoft SearchLib Community is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft SearchLib Community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft SearchLib Community. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.web.controller.runtime;

import java.util.List;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.statistics.StatisticTypeEnum;
import com.jaeksoft.searchlib.statistics.StatisticsAbstract;
import com.jaeksoft.searchlib.web.controller.CommonController;

public abstract class StatisticsControllerAbstract extends CommonController {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2420054165684926096L;

	private StatisticsAbstract selectedStat;

	private StatisticTypeEnum statType;

	private List<StatisticsAbstract> statList;

	protected StatisticsControllerAbstract(StatisticTypeEnum type)
			throws SearchLibException {
		super();
		this.statType = type;
		selectedStat = null;
		statList = null;
	}

	public boolean isStatList() throws SearchLibException {
		return getList() != null;
	}

	public List<StatisticsAbstract> getList() throws SearchLibException {
		synchronized (this) {
			if (statList != null)
				return statList;
			statList = getClient().getStatisticsList().getStatList(statType);
			if (statList == null)
				return null;
			if (selectedStat == null && statList.size() > 0)
				selectedStat = statList.get(0);
			return statList;
		}
	}

	public StatisticsAbstract getSelectedStat() {
		synchronized (this) {
			return selectedStat;
		}
	}

	public void setSelectedStat(StatisticsAbstract selectedStat) {
		synchronized (this) {
			this.selectedStat = selectedStat;
			statList = null;
		}
	}

	@Override
	protected void reloadPage() {
		synchronized (this) {
			selectedStat = null;
			statList = null;
			super.reloadPage();
		}
	}
}
