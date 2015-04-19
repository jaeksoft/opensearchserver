/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.mailbox.crawler;

import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;

import com.jaeksoft.searchlib.crawler.mailbox.MailboxProtocolEnum;
import com.sun.mail.imap.IMAPMessage;

public class IMAP4Crawler extends MailboxAbstractCrawler {

	@Override
	protected Store getStore() throws MessagingException {
		Properties properties = new Properties();
		properties.setProperty("mail.host", item.getServerName());
		properties.setProperty("mail.port",
				Integer.toString(item.getServerPort()));
		String storeProtocol = protocol == MailboxProtocolEnum.IMAP4 ? "imap"
				: "imaps";
		properties.setProperty("mail.transport.protocol", storeProtocol);
		Session session = Session.getInstance(properties,
				new javax.mail.Authenticator() {
					@Override
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(item.getUser(), item
								.getPassword());
					}
				});
		return session.getStore(storeProtocol);
	}

	@Override
	protected void connect(Store store) throws MessagingException {
		store.connect();
	}

	@Override
	protected String getMessageId(Folder folder, Message message)
			throws MessagingException {
		if (!(message instanceof IMAPMessage))
			return null;
		return ((IMAPMessage) message).getMessageID();
	}
}
