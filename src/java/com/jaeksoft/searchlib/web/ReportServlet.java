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

package com.jaeksoft.searchlib.web;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.mail.HtmlEmail;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.statistics.Aggregate;
import com.jaeksoft.searchlib.statistics.StatisticPeriodEnum;
import com.jaeksoft.searchlib.statistics.StatisticTypeEnum;
import com.jaeksoft.searchlib.statistics.StatisticsAbstract;

public class ReportServlet extends AbstractServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7243867246220284137L;

	/**
	 * 
	 */

	private void doStatistics(Client client, String stat, String period,
			PrintWriter pw) throws SearchLibException {
		if (stat == null || period == null)
			return;
		StatisticTypeEnum statType = StatisticTypeEnum.valueOf(stat
				.toUpperCase());
		StatisticPeriodEnum statPeriod = StatisticPeriodEnum.valueOf(period
				.toUpperCase());
		StatisticsAbstract statistics = client.getStatisticsList().getStat(
				statType, statPeriod);
		if (statistics == null)
			return;
		pw.println("<html>");
		pw.println("<p>" + statType + " - " + statPeriod.getName() + "</p>");
		pw.println("<table cellpadding=\"1\" cellspacing=\"0\" border=\"1\">");
		pw
				.println("<tr><th>Period start time</th><th>Count</th><th>Average</th><th>Min</th><th>Max</th><th>Error</th></tr>");
		for (Aggregate aggr : statistics.getArray()) {
			pw.println("<tr>");
			pw.println("<td>" + aggr.getStartTime() + "</td>");
			pw.println("<td>" + aggr.getCount() + "</td>");
			pw.println("<td>" + aggr.getAverage() + "</td>");
			pw.println("<td>" + aggr.getMin() + "</td>");
			pw.println("<td>" + aggr.getMax() + "</td>");
			pw.println("<td>" + aggr.getError() + "</td>");
			pw.println("</tr>");
		}
		pw.println("</table>");
		pw.println("</html>");
	}

	@Override
	protected void doRequest(ServletTransaction transaction)
			throws ServletException {
		try {
			HttpServletRequest request = transaction.getServletRequest();
			Client client = ClientCatalog
					.getClient(request.getParameter("use"));
			String report = request.getParameter("report");
			String emails = request.getParameter("emails");
			if (emails == null)
				return;
			HtmlEmail htmlEmail = client.getMailer().getHtmlEmail(emails, null);
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			if ("statistics".equals(report)) {
				htmlEmail.setSubject("OpenSearchServer statistics report");
				doStatistics(client, request.getParameter("stat"), request
						.getParameter("period"), pw);
			}
			pw.close();
			sw.close();
			String html = sw.toString();
			if (html == null || html.length() == 0) {
				transaction.addXmlResponse("Result", "Nothing to send");
			} else {
				htmlEmail.setHtmlMsg(sw.toString());
				htmlEmail
						.setTextMsg("Your email client does not support HTML messages");
				htmlEmail.send();
			}
			transaction.addXmlResponse("MailSent", emails);
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

}
