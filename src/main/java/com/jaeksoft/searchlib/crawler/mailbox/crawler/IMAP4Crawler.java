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

import java.io.IOException;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.mailbox.MailboxFieldEnum;
import com.jaeksoft.searchlib.crawler.mailbox.MailboxProtocolEnum;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.imap.IMAPStore;

public class IMAP4Crawler extends MailboxAbstractCrawler {

	@Override
	public void read() throws MessagingException, IOException,
			SearchLibException {
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
		IMAPStore store = (IMAPStore) session.getStore(storeProtocol);
		try {
			store.connect();
			Folder defaultFolder = store.getDefaultFolder();
			if (defaultFolder == null)
				return;
			Folder[] folders = defaultFolder.list();
			if (folders == null)
				return;
			for (Folder folder : folders)
				readFolder(folder);
		} finally {
			if (store != null)
				store.close();
		}
	}

	@Override
	public void readMessage(IndexDocument document, Folder folder,
			Message message) throws MessagingException {
		super.readMessage(document, folder, message);
		if (message instanceof IMAPMessage)
			document.addString(MailboxFieldEnum.message_id.name(),
					((IMAPMessage) message).getMessageID());
	}
}
