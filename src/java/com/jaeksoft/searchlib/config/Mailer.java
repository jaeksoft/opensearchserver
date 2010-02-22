package com.jaeksoft.searchlib.config;

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
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Jaeksoft OpenSearchServer. If not, see <http://www.gnu.org/licenses/>.
 **/

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.w3c.dom.Node;

import com.jaeksoft.searchlib.util.XPathParser;

public class Mailer {

	private String smtp_host;
	private String username;
	private String password;
	private boolean use_ssl;
	private int smtp_port;
	private String from_email;
	private String from_name;

	private Mailer(String smtp_host, String username, String password,
			boolean use_ssl, int smtp_port, String from_email, String from_name) {
		this.smtp_host = smtp_host == null ? "localhost" : smtp_host;
		this.username = username;
		this.password = password;
		this.use_ssl = use_ssl;
		this.smtp_port = smtp_port == 0 ? 25 : smtp_port;
		this.from_email = from_email;
		this.from_name = from_name;
	}

	public HtmlEmail getHtmlEmail(String dest_emails, String subject)
			throws EmailException {
		HtmlEmail htmlEmail = new HtmlEmail();
		htmlEmail.setHostName(smtp_host);
		if (username != null && password != null)
			htmlEmail.setAuthentication(username, password);
		htmlEmail.setSSL(use_ssl);
		htmlEmail.setSmtpPort(smtp_port);
		htmlEmail.setFrom(from_email, from_name);
		if (dest_emails != null) {
			String[] emailList = dest_emails.split(",");
			for (String email : emailList)
				htmlEmail.addTo(email);
		}
		if (subject != null)
			htmlEmail.setSubject(subject);
		return htmlEmail;
	}

	public static Mailer fromXmlConfig(Node node) {
		String smtp_host = XPathParser.getAttributeString(node, "smtp_host");
		String username = XPathParser.getAttributeString(node, "username");
		String password = XPathParser.getAttributeString(node, "password");
		boolean use_ssl = "yes".equalsIgnoreCase(XPathParser
				.getAttributeString(node, "use_ssl"));
		int smtp_port = XPathParser.getAttributeValue(node, "smtp_port");
		String from_email = XPathParser.getAttributeString(node, "from_email");
		String from_name = XPathParser.getAttributeString(node, "from_name");
		return new Mailer(smtp_host, username, password, use_ssl, smtp_port,
				from_email, from_name);
	}
}