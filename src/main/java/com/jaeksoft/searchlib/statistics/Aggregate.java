/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.statistics;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.util.Timer;

@XmlAccessorType(XmlAccessType.FIELD)
public class Aggregate {

	@JsonIgnore
	private final ReadWriteLock rwl = new ReadWriteLock();

	private Date startTime;

	private long count;

	private long max;

	private String maxInfo;

	private long min;

	private float average;

	protected long nextStart;

	private long error;

	private String lastError;

	protected Aggregate() {
	}

	protected Aggregate(long startTime, long nextStart) {
		this.startTime = new Date(startTime);
		this.nextStart = nextStart;
		count = 0;
		error = 0;
		max = 0;
		min = Long.MAX_VALUE;
		average = 0;
		maxInfo = null;
		lastError = null;
	}

	protected void add(Timer timer) {
		rwl.w.lock();
		try {
			long duration = timer.getDuration();
			if (duration > max) {
				max = duration;
				maxInfo = timer.getInfo();
			}
			if (duration < min)
				min = duration;
			if (count != 0) {
				float added = average + (float) duration / (float) count;
				float nn1 = (float) count / (float) ++count;
				average = added * nn1;
			} else {
				average = duration;
				count++;
			}
			String err = timer.getError();
			if (err != null) {
				lastError = err;
				error++;
			}
		} finally {
			rwl.w.unlock();
		}
	}

	public long getMin() {
		rwl.r.lock();
		try {
			return min;
		} finally {
			rwl.r.unlock();
		}
	}

	public long getMax() {
		rwl.r.lock();
		try {
			return max;
		} finally {
			rwl.r.unlock();
		}
	}

	public long getError() {
		rwl.r.lock();
		try {
			return error;
		} finally {
			rwl.r.unlock();
		}
	}

	public String getLastError() {
		rwl.r.lock();
		try {
			return lastError;
		} finally {
			rwl.r.unlock();
		}
	}

	public String getMaxInfo() {
		rwl.r.lock();
		try {
			return maxInfo;
		} finally {
			rwl.r.unlock();
		}
	}

	public long getCount() {
		rwl.r.lock();
		try {
			return count;
		} finally {
			rwl.r.unlock();
		}
	}

	public float getAverage() {
		rwl.r.lock();
		try {
			return average;
		} finally {
			rwl.r.unlock();
		}
	}

	public Date getStartTime() {
		rwl.r.lock();
		try {
			return startTime;
		} finally {
			rwl.r.unlock();
		}
	}

	public long getNextStart() {
		rwl.r.lock();
		try {
			return nextStart;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public String toString() {
		rwl.r.lock();
		try {
			StringBuilder sb = new StringBuilder();
			sb.append(startTime);
			sb.append(" - Count:");
			sb.append(count);
			sb.append(" -  Average:");
			sb.append(average);
			sb.append(" - Min:");
			sb.append(min);
			sb.append(" - Max:");
			sb.append(max);
			return sb.toString();
		} finally {
			rwl.r.unlock();
		}
	}

}
