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

package com.jaeksoft.searchlib.crawler.mailbox;

import com.jaeksoft.searchlib.crawler.mailbox.crawler.IMAP4Crawler;
import com.jaeksoft.searchlib.crawler.mailbox.crawler.MailboxAbstractCrawler;
import com.jaeksoft.searchlib.crawler.mailbox.crawler.POP3Crawler;

public enum MailboxProtocolEnum {

	POP3(POP3Crawler.class),

	POP3S(POP3Crawler.class),

	IMAP4(IMAP4Crawler.class),

	IMAP4S(IMAP4Crawler.class);

	private final Class<? extends MailboxAbstractCrawler> crawlerClass;

	private MailboxProtocolEnum(
			Class<? extends MailboxAbstractCrawler> crawlerClass) {
		this.crawlerClass = crawlerClass;
	}

	public static MailboxAbstractCrawler getNewCrawler(
			MailboxCrawlThread thread, MailboxCrawlItem item)
			throws InstantiationException, IllegalAccessException {
		try {
			MailboxProtocolEnum protocol = valueOf(item.getServerProtocol());
			MailboxAbstractCrawler crawler = protocol.crawlerClass
					.newInstance();
			crawler.init(thread, protocol, item);
			return crawler;
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	public String getLabel() {
		return name();
	}

	public static final String[] labelArray;

	static {
		labelArray = new String[values().length];
		int i = 0;
		for (MailboxProtocolEnum e : values())
			labelArray[i++] = e.name();
	}

}
