/**
 * License Agreement for OpenSearchServer
 * 
 * Copyright (C) 2010-2012 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of OpenSearchServer.
 * 
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * OpenSearchServer. If not, see <http://www.gnu.org/licenses/>.
 **/
package com.jaeksoft.searchlib.config;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;

public class Mailer {

	private final String smtp_host;
	private final String username;
	private final String password;
	private final boolean use_ssl;
	private final int smtp_port;
	private final String from_email;
	private final String from_name;

	public Mailer(String url, String from_email, String from_name)
			throws URISyntaxException {
		URI uri = null;
		if (url != null && url.length() > 0)
			uri = new URI(url);
		this.smtp_host = uri == null ? "localhost" : uri.getHost();
		this.username = uri == null ? null : uri.getUserInfo();
		this.password = uri == null ? null : uri.getAuthority();
		this.smtp_port = uri == null ? 25 : uri.getPort();
		this.use_ssl = smtp_port != 25;
		this.from_email = from_email;
		this.from_name = from_name;
	}

	public HtmlEmail getHtmlEmail(String dest_emails, String subject)
			throws EmailException {
		HtmlEmail htmlEmail = new HtmlEmail();
		htmlEmail.setHostName(smtp_host);
		if (username != null && password != null && username.length() > 0
				&& password.length() > 0)
			htmlEmail.setAuthentication(username, password);
		htmlEmail.setSSL(use_ssl);
		htmlEmail.setSmtpPort(smtp_port);
		htmlEmail.setFrom(from_email, from_name);
		if (dest_emails != null) {
			String[] emailList = StringUtils.split(dest_emails, ',');
			for (String email : emailList)
				htmlEmail.addTo(email);
		}
		if (subject != null)
			htmlEmail.setSubject(subject);
		return htmlEmail;
	}

}