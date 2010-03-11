/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

import com.jaeksoft.searchlib.util.Timer;
import com.jaeksoft.searchlib.util.XPathParser;

public abstract class StatisticsAbstract {

	private LinkedList<Aggregate> aggregateList;

	private Aggregate currentAggregate;

	private Aggregate[] aggregateArray;

	private int maxRetention;

	private boolean writeToLog;

	private StatisticTypeEnum type;

	public StatisticsAbstract(StatisticTypeEnum type, boolean writeToLog,
			int maxRetention, File statDir) throws IOException,
			ClassNotFoundException {
		this.type = type;
		this.writeToLog = writeToLog;
		this.maxRetention = maxRetention;
		aggregateList = new LinkedList<Aggregate>();
		currentAggregate = null;
		aggregateArray = null;
		load(statDir);
	}

	public abstract Aggregate newAggregate(long startTime);

	public abstract StatisticPeriodEnum getPeriod();

	private void addAggregate(Aggregate aggregate) {
		aggregateList.addLast(aggregate);
		if (aggregateList.size() > maxRetention)
			aggregateList.removeFirst();
		aggregateArray = null;
	}

	public void add(Timer timer) {
		synchronized (aggregateList) {
			long startTime = timer.getStartTime();
			if (currentAggregate == null
					|| startTime >= currentAggregate.nextStart) {
				if (currentAggregate != null && writeToLog)
					System.out.println(type + " - " + getPeriod().getName()
							+ " - " + currentAggregate);
				currentAggregate = newAggregate(timer.getStartTime());
				addAggregate(currentAggregate);
			}
			currentAggregate.add(timer);
		}
	}

	protected StatisticTypeEnum getType() {
		return type;
	}

	public Aggregate[] getArray() {
		synchronized (aggregateList) {
			if (aggregateArray != null)
				return aggregateArray;
			aggregateArray = new Aggregate[aggregateList.size()];
			return aggregateList.toArray(aggregateArray);
		}
	}

	final protected static StatisticsAbstract fromXmlConfig(XPathParser xpp,
			Node node, File statDir) throws InstantiationException,
			IllegalAccessException, ClassNotFoundException,
			XPathExpressionException, DOMException, IOException {

		if (node == null)
			return null;

		StatisticTypeEnum type = StatisticTypeEnum.valueOf(XPathParser
				.getAttributeString(node, "type"));
		if (type == null)
			throw new XPathExpressionException(
					"Wrong type name. Must be SEARCH, UPDATE, DELETE, RELOAD or OPTIMIZE");
		StatisticPeriodEnum period = StatisticPeriodEnum.valueOf(XPathParser
				.getAttributeString(node, "period"));
		if (period == null)
			throw new XPathExpressionException(
					"Wrong periode name. Must be MONTH, DAY, HOUR or MINUTE");
		boolean writeToLog = "yes".equalsIgnoreCase(XPathParser
				.getAttributeString(node, "writeToLog"));
		int maxRetention = XPathParser.getAttributeValue(node, "maxRetention");
		if (maxRetention == 0)
			throw new XPathExpressionException(
					"maxRetention must be greater than 0.");
		if (period == StatisticPeriodEnum.MONTH)
			return new MonthStatistics(type, writeToLog, maxRetention, statDir);
		else if (period == StatisticPeriodEnum.DAY)
			return new DayStatistics(type, writeToLog, maxRetention, statDir);
		else if (period == StatisticPeriodEnum.HOUR)
			return new HourStatistics(type, writeToLog, maxRetention, statDir);
		else if (period == StatisticPeriodEnum.MINUTE)
			return new MinuteStatistics(type, writeToLog, maxRetention, statDir);
		else
			throw new XPathExpressionException(
					"Wrong periode name. Should be day, hour, or minute.");
	}

	private File getStatFile(File statDir) {
		return new File(statDir, type.name().replace(' ', '_') + "_"
				+ getPeriod().getName().replace(' ', '_'));
	}

	public void load(File statDir) throws IOException, ClassNotFoundException {
		File file = getStatFile(statDir);
		if (!file.exists())
			return;
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		try {
			fis = new FileInputStream(file);
			ois = new ObjectInputStream(fis);
			synchronized (aggregateList) {
				int size = ois.readInt();
				while (size-- > 0) {
					Aggregate aggr = new Aggregate();
					aggr.readExternal(ois);
					addAggregate(aggr);
				}
			}
		} finally {
			if (ois != null)
				ois.close();
			if (fis != null)
				fis.close();
		}
	}

	public void save(File statDir) throws IOException {
		File file = getStatFile(statDir);
		if (file.exists())
			file.delete();
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try {
			fos = new FileOutputStream(file);
			oos = new ObjectOutputStream(fos);
			synchronized (aggregateList) {
				oos.writeInt(aggregateList.size());
				for (Aggregate aggr : aggregateList)
					aggr.writeExternal(oos);
			}
		} finally {
			if (oos != null)
				oos.close();
			if (fos != null)
				fos.close();
		}
	}
}
