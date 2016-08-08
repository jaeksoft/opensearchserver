/**
 * License Agreement for OpenSearchServer
 * 
 * Copyright (C) 2010-2013 Emmanuel Keller / Jaeksoft
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

import java.io.Closeable;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.SimpleEmail;

import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.util.IOUtils;

public class Mailer implements Closeable {

	private final Email email;
	private PrintWriter textPrintWriter;
	private PrintWriter htmlPrintWriter;
	private StringWriter textStringWriter;
	private StringWriter htmlStringWriter;

	public Mailer(boolean isHtml, String toRecipients, String subject)
			throws EmailException {
		email = isHtml ? new HtmlEmail() : new SimpleEmail();
		setupTransport();
		setupContent(toRecipients, subject);
		textPrintWriter = null;
		htmlPrintWriter = null;
		textStringWriter = null;
		htmlStringWriter = null;
	}

	public void send() throws EmailException {
		if (email instanceof HtmlEmail) {
			HtmlEmail htmlEmail = (HtmlEmail) email;
			if (htmlStringWriter != null)
				htmlEmail.setHtmlMsg(htmlStringWriter.toString());
			if (textStringWriter != null)
				htmlEmail.setTextMsg(textStringWriter.toString());
			else
				htmlEmail.setTextMsg("This message contains an HTML content");
		} else if (email instanceof SimpleEmail) {
			SimpleEmail simpleEmail = (SimpleEmail) email;
			if (textStringWriter != null)
				simpleEmail.setMsg(textStringWriter.toString());
		}
		if (textStringWriter != null)
			email.send();
	}

	@Override
	public void close() {
		IOUtils.close(textPrintWriter, htmlPrintWriter, textStringWriter,
				htmlStringWriter);
		textPrintWriter = null;
		htmlPrintWriter = null;
		textStringWriter = null;
		htmlStringWriter = null;
	}

	private void setupTransport() throws EmailException {
		String hostname = ClientFactory.INSTANCE.getSmtpHostname().getValue();
		if (hostname == null || hostname.length() == 0)
			throw new EmailException("The SMTP hostname is not setup");
		email.setHostName(hostname);
		String username = ClientFactory.INSTANCE.getSmtpUsername().getValue();
		String password = ClientFactory.INSTANCE.getSmtpPassword().getValue();
		if (username != null && password != null && username.length() > 0
				&& password.length() > 0)
			email.setAuthentication(username, password);
		boolean useSSL = ClientFactory.INSTANCE.getSmtpUseSsl().getValue();
		boolean useTLS = ClientFactory.INSTANCE.getSmtpUseTls().getValue();
		int port = ClientFactory.INSTANCE.getSmtpPort().getValue();
		email.setSmtpPort(port);
		email.setSSLOnConnect(useSSL);
		email.setStartTLSEnabled(useTLS);
		if (useSSL)
			email.setSslSmtpPort(Integer.toString(port));
		String senderEmail = ClientFactory.INSTANCE.getSmtpSenderEmail()
				.getValue();
		String senderName = ClientFactory.INSTANCE.getSmtpSenderName()
				.getValue();
		if (senderEmail == null || senderEmail.length() == 0)
			throw new EmailException("The SMTP sender e-mail is not setup");
		email.setFrom(senderEmail, senderName);
	}

	private void setupContent(String dest_emails, String subject)
			throws EmailException {
		if (dest_emails != null) {
			String[] emailList = StringUtils.split(dest_emails, ',');
			for (String address : emailList)
				email.addTo(address);
		}
		setSubject(subject);
	}

	public void setSubject(String subject) {
		if (subject != null)
			email.setSubject(subject);
	}

	public PrintWriter getTextPrintWriter() {
		if (textPrintWriter != null)
			return textPrintWriter;
		textStringWriter = new StringWriter();
		textPrintWriter = new PrintWriter(textStringWriter);
		return textPrintWriter;
	}

	public PrintWriter getHtmlPrintWriter() {
		if (htmlPrintWriter != null)
			return htmlPrintWriter;
		htmlStringWriter = new StringWriter();
		htmlPrintWriter = new PrintWriter(htmlStringWriter);
		return htmlPrintWriter;
	}

	public boolean isEmpty() {
		if (textStringWriter != null)
			if (textStringWriter.getBuffer().length() > 0)
				return false;
		if (htmlStringWriter != null)
			if (htmlStringWriter.getBuffer().length() > 0)
				return false;
		return true;
	}

}