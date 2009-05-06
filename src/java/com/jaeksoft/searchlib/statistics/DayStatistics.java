/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.statistics;

import java.util.Calendar;

public class DayStatistics extends StatisticsAbstract {

	public DayStatistics(StatisticTypeEnum type, boolean writeToLog,
			int maxRetention) {
		super(type, writeToLog, maxRetention);
	}

	@Override
	public Aggregate newAggregate(long startTime) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.roll(Calendar.DAY_OF_MONTH, true);
		long nextStart = cal.getTimeInMillis();
		return new Aggregate(startTime, nextStart);
	}

	@Override
	public StatisticPeriodEnum getPeriod() {
		return StatisticPeriodEnum.DAY;
	}
}
