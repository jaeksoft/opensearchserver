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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.FetchProfile;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.common.process.CrawlStatus;
import com.jaeksoft.searchlib.crawler.mailbox.MailboxCrawlItem;
import com.jaeksoft.searchlib.crawler.mailbox.MailboxCrawlThread;
import com.jaeksoft.searchlib.crawler.mailbox.MailboxFieldEnum;
import com.jaeksoft.searchlib.crawler.mailbox.MailboxProtocolEnum;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.util.IOUtils;
import com.jaeksoft.searchlib.util.StringUtils;

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

	protected abstract Store getStore() throws MessagingException;

	protected abstract void connect(Store store) throws MessagingException;

	public void read() throws MessagingException, IOException,
			SearchLibException {
		Store store = null;
		try {
			store = getStore();
			connect(store);
			readFolder(store.getDefaultFolder());
		} finally {
			if (store != null)
				store.close();
		}
	}

	public String check() throws MessagingException, IOException,
			SearchLibException {
		Store store = null;
		StringWriter sw = null;
		PrintWriter pw = null;
		try {
			sw = new StringWriter();
			pw = new PrintWriter(sw);
			pw.println();
			store = getStore();
			connect(store);
			checkFolder(store.getDefaultFolder(), pw);
			pw.println("OK");
			return sw.toString();
		} finally {
			if (store != null)
				store.close();
			IOUtils.close(pw, sw);
		}
	}

	private void readMessagesFolder(Folder folder) throws MessagingException,
			IOException, SearchLibException {
		folder.open(Folder.READ_ONLY);
		String folderFullName = folder.getFullName();
		try {
			int max = folder.getMessageCount();
			int i = 0;
			final int buffer = item.getBufferSize();
			while (i < max && !thread.isAborted()) {
				thread.setStatusInfo(CrawlStatus.CRAWL);
				int end = i + buffer;
				if (end > max)
					end = max;
				Message[] messages = folder.getMessages(i + 1, end);
				FetchProfile fp = new FetchProfile();
				fp.add(FetchProfile.Item.ENVELOPE);
				folder.fetch(messages, fp);
				for (Message message : messages) {
					if (thread.isAborted())
						break;
					i++;
					String messageId = getMessageId(folder, message);
					if (StringUtils.isEmpty(messageId))
						continue;
					if (thread.isAlreadyIndexed(messageId)) {
						thread.incIgnored();
						continue;
					}
					IndexDocument crawlIndexDocument = new IndexDocument(
							item.getLang());
					IndexDocument parserIndexDocument = new IndexDocument(
							item.getLang());
					crawlIndexDocument.addString(
							MailboxFieldEnum.folder.name(), folderFullName);
					try {
						readMessage(crawlIndexDocument, parserIndexDocument,
								folder, message, messageId);
						thread.addDocument(crawlIndexDocument,
								parserIndexDocument);
					} catch (Exception e) {
						Logging.warn(e);
						thread.incError();
					}
				}
			}
		} finally {
			folder.close(false);
		}
	}

	protected abstract String getMessageId(Folder folder, Message message)
			throws MessagingException;

	protected void readFolder(Folder folder) throws MessagingException,
			IOException, SearchLibException {
		if (folder == null)
			return;
		if ((folder.getType() & Folder.HOLDS_MESSAGES) != 0)
			readMessagesFolder(folder);
		if ((folder.getType() & Folder.HOLDS_FOLDERS) != 0)
			readHoldsFolder(folder);
	}

	protected void checkFolder(Folder folder, PrintWriter pw)
			throws MessagingException, IOException, SearchLibException {
		if (folder == null)
			return;
		if ((folder.getType() & Folder.HOLDS_MESSAGES) != 0) {
			folder.open(Folder.READ_ONLY);
			try {
				pw.print("Folder ");
				pw.print(folder.getName());
				pw.print(": ");
				pw.print(folder.getMessageCount());
				pw.println(" msgs(s).");
			} finally {
				folder.close(false);
			}
		}
		if ((folder.getType() & Folder.HOLDS_FOLDERS) != 0) {
			Folder[] folders = folder.list();
			if (folders != null)
				for (Folder f : folders)
					checkFolder(f, pw);
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

	private void multipartIterator(Multipart multipart, IndexDocument document)
			throws MessagingException, IOException, SearchLibException,
			ClassNotFoundException {
		for (int j = 0; j < multipart.getCount(); j++) {
			BodyPart bodyPart = multipart.getBodyPart(j);
			String disposition = bodyPart.getDisposition();
			if (disposition != null
					&& (disposition.equalsIgnoreCase("ATTACHMENT"))) {
				doBodyPart(bodyPart.getContent(), bodyPart.getContentType(),
						document);
			} else
				mimeDispatch(bodyPart.getContent(), bodyPart.getContentType(),
						document);
		}
	}

	private void doBodyPart(Object content, String contentType,
			IndexDocument document) throws SearchLibException, IOException,
			ClassNotFoundException {
		thread.indexContent(content, contentType, document);
	}

	private void mimeDispatch(Object content, String contentType,
			IndexDocument document) throws MessagingException, IOException,
			SearchLibException, ClassNotFoundException {
		String lowerContentType = contentType.toLowerCase();
		if (lowerContentType.startsWith("multipart/alternative"))
			multipartIterator((Multipart) content, document);
		else if (lowerContentType.startsWith("multipart/related"))
			multipartIterator((Multipart) content, document);
		else if (lowerContentType.startsWith("multipart/mixed"))
			multipartIterator((Multipart) content, document);
		else
			doBodyPart(content, contentType, document);
	}

	final public void readMessage(IndexDocument crawlIndexDocument,
			IndexDocument parserIndexDocument, Folder folder, Message message,
			String id) throws MessagingException, IOException,
			SearchLibException, ClassNotFoundException {
		crawlIndexDocument.addString(MailboxFieldEnum.message_id.name(), id);
		crawlIndexDocument.addString(MailboxFieldEnum.message_number.name(),
				Integer.toString(message.getMessageNumber()));
		if (message instanceof MimeMessage)
			crawlIndexDocument.addString(MailboxFieldEnum.content_id.name(),
					((MimeMessage) message).getContentID());
		crawlIndexDocument.addString(MailboxFieldEnum.subject.name(),
				message.getSubject());
		putAddresses(crawlIndexDocument, message.getFrom(),
				MailboxFieldEnum.from_address.name(),
				MailboxFieldEnum.from_personal.name());
		putAddresses(crawlIndexDocument, message.getReplyTo(),
				MailboxFieldEnum.reply_to_address.name(),
				MailboxFieldEnum.reply_to_personal.name());
		putAddresses(crawlIndexDocument,
				message.getRecipients(RecipientType.TO),
				MailboxFieldEnum.recipient_to_address.name(),
				MailboxFieldEnum.recipient_to_personal.name());
		putAddresses(crawlIndexDocument,
				message.getRecipients(RecipientType.CC),
				MailboxFieldEnum.recipient_cc_address.name(),
				MailboxFieldEnum.recipient_cc_personal.name());
		putAddresses(crawlIndexDocument,
				message.getRecipients(RecipientType.BCC),
				MailboxFieldEnum.recipient_bcc_address.name(),
				MailboxFieldEnum.recipient_bcc_personal.name());
		Date dt = message.getSentDate();
		if (dt != null)
			crawlIndexDocument.addString(MailboxFieldEnum.send_date.name(),
					dt.toString());
		dt = message.getReceivedDate();
		if (dt != null)
			crawlIndexDocument.addString(MailboxFieldEnum.received_date.name(),
					dt.toString());
		if (message.isSet(Flag.ANSWERED))
			crawlIndexDocument.addString(MailboxFieldEnum.flags.name(),
					"ANSWERED");
		if (message.isSet(Flag.DELETED))
			crawlIndexDocument.addString(MailboxFieldEnum.flags.name(),
					"DELETED");
		if (message.isSet(Flag.DRAFT))
			crawlIndexDocument
					.addString(MailboxFieldEnum.flags.name(), "DRAFT");
		if (message.isSet(Flag.FLAGGED))
			crawlIndexDocument.addString(MailboxFieldEnum.flags.name(),
					"FLAGGED");
		if (message.isSet(Flag.SEEN))
			crawlIndexDocument.addString(MailboxFieldEnum.flags.name(), "SEEN");

		try {
			mimeDispatch(message.getContent(), message.getContentType(),
					parserIndexDocument);
		} catch (UnsupportedEncodingException e) {
			Logging.warn("Message " + id, e);
		}
	}
}
