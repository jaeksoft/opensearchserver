/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.scheduler;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class TaskCronExpression {

	private String seconds;

	private String minutes;

	private String hours;

	private String dayOfMonth;

	private String month;

	private String dayOfWeek;

	private String year;

	public TaskCronExpression() {
		seconds = "*";
		minutes = "*";
		hours = "*";
		dayOfMonth = "*";
		month = "*";
		dayOfWeek = "*";
		year = "*";
	}

	public void copy(TaskCronExpression cron) {
		seconds = cron.seconds;
		minutes = cron.minutes;
		hours = cron.hours;
		dayOfMonth = cron.dayOfMonth;
		month = cron.month;
		dayOfWeek = cron.dayOfWeek;
		year = cron.year;
	}

	/**
	 * @return the seconds
	 */
	public String getSeconds() {
		return seconds;
	}

	/**
	 * @param seconds
	 *            the seconds to set
	 */
	public void setSeconds(String seconds) {
		this.seconds = seconds;
	}

	/**
	 * @return the minutes
	 */
	public String getMinutes() {
		return minutes;
	}

	/**
	 * @param minutes
	 *            the minutes to set
	 */
	public void setMinutes(String minutes) {
		this.minutes = minutes;
	}

	/**
	 * @return the hours
	 */
	public String getHours() {
		return hours;
	}

	/**
	 * @param hours
	 *            the hours to set
	 */
	public void setHours(String hours) {
		this.hours = hours;
	}

	/**
	 * @return the dayOfMonth
	 */
	public String getDayOfMonth() {
		return dayOfMonth;
	}

	/**
	 * @param dayOfMonth
	 *            the dayOfMonth to set
	 */
	public void setDayOfMonth(String dayOfMonth) {
		this.dayOfMonth = dayOfMonth;
	}

	/**
	 * @return the month
	 */
	public String getMonth() {
		return month;
	}

	/**
	 * @param month
	 *            the month to set
	 */
	public void setMonth(String month) {
		this.month = month;
	}

	/**
	 * @return the dayOfWeek
	 */
	public String getDayOfWeek() {
		return dayOfWeek;
	}

	/**
	 * @param dayOfWeek
	 *            the dayOfWeek to set
	 */
	public void setDayOfWeek(String dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

	/**
	 * @return the year
	 */
	public String getYear() {
		return year;
	}

	/**
	 * @param year
	 *            the year to set
	 */
	public void setYear(String year) {
		this.year = year;
	}

	private final static String XML_NODE_SECONDS = "seconds";
	private final static String XML_NODE_MINUTES = "minutes";
	private final static String XML_NODE_HOURS = "hours";
	private final static String XML_NODE_DAYOFMONTH = "dayOfMonth";
	private final static String XML_NODE_MONTH = "month";
	private final static String XML_NODE_DAYOFWEEK = "dayOfWeek";
	private final static String XML_NODE_YEAR = "year";

	public TaskCronExpression fromXml(Node node) {
		TaskCronExpression cron = new TaskCronExpression();
		NamedNodeMap attributes = node.getAttributes();
		if (attributes == null)
			return cron;
		cron.setSeconds(XPathParser.getAttributeString(node, XML_NODE_SECONDS));
		cron.setMinutes(XPathParser.getAttributeString(node, XML_NODE_MINUTES));
		cron.setHours(XPathParser.getAttributeString(node, XML_NODE_HOURS));
		cron.setSeconds(XPathParser.getAttributeString(node,
				XML_NODE_DAYOFMONTH));
		cron.setSeconds(XPathParser.getAttributeString(node, XML_NODE_MONTH));
		cron.setSeconds(XPathParser
				.getAttributeString(node, XML_NODE_DAYOFWEEK));
		cron.setSeconds(XPathParser.getAttributeString(node, XML_NODE_YEAR));
		return cron;
	}

	public void writeXml(XmlWriter xmlWriter) throws SAXException {
		xmlWriter.startElement("cron", XML_NODE_SECONDS, seconds,
				XML_NODE_MINUTES, minutes, XML_NODE_HOURS, hours,
				XML_NODE_DAYOFMONTH, dayOfMonth, XML_NODE_MONTH, month,
				XML_NODE_DAYOFWEEK, dayOfWeek, XML_NODE_YEAR, year);
		xmlWriter.endElement();
	}

}
