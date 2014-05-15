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
import java.util.Date;

import javax.mail.Address;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.mailbox.MailboxCrawlItem;
import com.jaeksoft.searchlib.crawler.mailbox.MailboxCrawlThread;
import com.jaeksoft.searchlib.crawler.mailbox.MailboxFieldEnum;
import com.jaeksoft.searchlib.crawler.mailbox.MailboxProtocolEnum;
import com.jaeksoft.searchlib.index.IndexDocument;

public abstract class MailboxAbstractCrawler {

	protected MailboxCrawlThread thread;
	protected MailboxProtocolEnum protocol;
	protected MailboxCrawlItem item;

	public void init(MailboxCrawlThread thread, MailboxProtocolEnum protocol,
			MailboxCrawlItem item) {
		this.thread = thread;
		this.protocol = protocol;
		this.item = item;
	}

	public abstract void read() throws MessagingException, IOException,
			SearchLibException;

	private void readMessagesFolder(Folder folder) throws MessagingException,
			IOException, SearchLibException {
		folder.open(Folder.READ_ONLY);
		String folderFullName = folder.getFullName();
		try {
			System.out.println("FOLDER: " + folder.getClass().getName() + " "
					+ folderFullName);
			Message[] messages = folder.getMessages();
			for (Message message : messages) {
				IndexDocument document = new IndexDocument(item.getLang());
				document.addString(MailboxFieldEnum.folder.name(),
						folderFullName);
				try {
					readMessage(document, folder, message);
				} catch (Throwable t) {
					Logging.warn(t);
					thread.incError();
				}
				thread.addDocument(document, null);
			}
		} finally {
			folder.close(false);
		}
	}

	protected void readFolder(Folder folder) throws MessagingException,
			IOException, SearchLibException {
		if (folder == null)
			return;
		switch (folder.getType()) {
		case Folder.HOLDS_FOLDERS:
			readHoldsFolder(folder);
			break;
		case Folder.HOLDS_MESSAGES:
			readMessagesFolder(folder);
			break;
		}
	}

	private void readHoldsFolder(Folder folder) throws MessagingException,
			IOException, SearchLibException {
		Folder[] folders = folder.list();
		if (folders == null)
			return;
		for (Folder f : folders)
			readFolder(f);
	}

	private void putAddresses(IndexDocument document, Address[] addresses,
			String fieldEmail, String fieldPersonal) {
		if (addresses == null)
			return;
		for (Address address : addresses) {
			if (address == null)
				continue;
			if (!(address instanceof InternetAddress))
				continue;
			InternetAddress ia = (InternetAddress) address;
			document.addString(fieldEmail, ia.getAddress());
			document.addString(fieldPersonal, ia.getPersonal());
		}
	}

	public void readMessage(IndexDocument document, Folder folder,
			Message message) throws MessagingException {
		document.addString(MailboxFieldEnum.message_number.name(),
				Integer.toString(message.getMessageNumber()));
		if (message instanceof MimeMessage)
			document.addString(MailboxFieldEnum.content_id.name(),
					((MimeMessage) message).getContentID());
		document.addString(MailboxFieldEnum.subject.name(),
				message.getSubject());
		putAddresses(document, message.getFrom(),
				MailboxFieldEnum.from_address.name(),
				MailboxFieldEnum.from_personal.name());
		putAddresses(document, message.getReplyTo(),
				MailboxFieldEnum.reply_to_address.name(),
				MailboxFieldEnum.reply_to_personal.name());
		putAddresses(document, message.getRecipients(RecipientType.TO),
				MailboxFieldEnum.recipient_to_address.name(),
				MailboxFieldEnum.recipient_to_personal.name());
		putAddresses(document, message.getRecipients(RecipientType.CC),
				MailboxFieldEnum.recipient_cc_address.name(),
				MailboxFieldEnum.recipient_cc_personal.name());
		putAddresses(document, message.getRecipients(RecipientType.BCC),
				MailboxFieldEnum.recipient_bcc_address.name(),
				MailboxFieldEnum.recipient_bcc_personal.name());
		Date dt = message.getSentDate();
		if (dt != null)
			document.addString(MailboxFieldEnum.send_date.name(), dt.toString());
		dt = message.getReceivedDate();
		if (dt != null)
			document.addString(MailboxFieldEnum.received_date.name(),
					dt.toString());
		if (message.isSet(Flag.ANSWERED))
			document.addString(MailboxFieldEnum.flags.name(), "ANSWERED");
		if (message.isSet(Flag.DELETED))
			document.addString(MailboxFieldEnum.flags.name(), "DELETED");
		if (message.isSet(Flag.DRAFT))
			document.addString(MailboxFieldEnum.flags.name(), "DRAFT");
		if (message.isSet(Flag.FLAGGED))
			document.addString(MailboxFieldEnum.flags.name(), "FLAGGED");
		if (message.isSet(Flag.SEEN))
			document.addString(MailboxFieldEnum.flags.name(), "SEEN");
	}
}
