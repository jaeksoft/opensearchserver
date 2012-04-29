/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2010 Emmanuel Keller / Jaeksoft
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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Date;

import com.jaeksoft.searchlib.util.External;
import com.jaeksoft.searchlib.util.Timer;

public class Aggregate implements Externalizable {

	private volatile Date startTime;

	private volatile long count;

	private volatile long max;

	private volatile String maxInfo;

	private volatile long min;

	private volatile float avg;

	protected volatile long nextStart;

	private volatile long error;

	private volatile String lastError;

	protected Aggregate() {

	}

	protected Aggregate(long startTime, long nextStart) {
		this.startTime = new Date(startTime);
		this.nextStart = nextStart;
		count = 0;
		error = 0;
		max = 0;
		min = Long.MAX_VALUE;
		avg = 0;
		maxInfo = null;
		lastError = null;
	}

	protected void add(Timer timer) {
		synchronized (this) {
			long duration = timer.duration();
			if (duration > max) {
				max = duration;
				maxInfo = timer.getInfo();
			}
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
			String err = timer.getError();
			if (err != null) {
				lastError = err;
				error++;
			}
		}
	}

	public long getMin() {
		return min;
	}

	public long getMax() {
		return max;
	}

	public long getError() {
		return error;
	}

	public String getLastError() {
		return lastError;
	}

	public String getMaxInfo() {
		return maxInfo;
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

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		startTime = new Date(in.readLong());
		count = in.readLong();
		max = in.readLong();
		maxInfo = External.readUTF(in);
		min = in.readLong();
		avg = in.readFloat();
		nextStart = in.readLong();
		error = in.readLong();
		lastError = External.readUTF(in);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeLong(startTime.getTime());
		out.writeLong(count);
		out.writeLong(max);
		External.writeUTF(maxInfo, out);
		out.writeLong(min);
		out.writeFloat(avg);
		out.writeLong(nextStart);
		out.writeLong(error);
		External.writeUTF(lastError, out);
	}
}
