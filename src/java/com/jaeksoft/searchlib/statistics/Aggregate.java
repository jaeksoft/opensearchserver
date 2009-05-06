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

import java.util.Date;

import com.jaeksoft.searchlib.util.Timer;

public class Aggregate {

	private volatile Date startTime;

	private volatile long count;

	private volatile long max;

	private volatile long min;

	private volatile float avg;

	private volatile long nextStart;

	protected Aggregate(long startTime, long nextStart) {
		this.startTime = new Date(startTime);
		this.nextStart = nextStart;
		count = 0;
		max = 0;
		min = Long.MAX_VALUE;
		avg = 0;
	}

	protected void add(Timer timer) {
		synchronized (this) {
			long duration = timer.duration();
			if (duration > max)
				max = duration;
			if (duration < min)
				min = duration;
			if (count != 0) {
				float added = avg + (float) duration / (float) count;
				float nn1 = (float) count / (float) ++count;
				avg = added * nn1;
			} else {
				avg = duration;
				count++;
			}
		}
	}

	public long getMin() {
		return min;
	}

	public long getMax() {
		return max;
	}

	public long getCount() {
		return count;
	}

	public float getAverage() {
		return avg;
	}

	public Date getStartTime() {
		return startTime;
	}

	public long getNextStart() {
		return nextStart;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(startTime);
		sb.append(" - Count:");
		sb.append(count);
		sb.append(" -  Average:");
		sb.append(avg);
		sb.append(" - Min:");
		sb.append(min);
		sb.append(" - Max:");
		sb.append(max);
		return sb.toString();
	}
}
