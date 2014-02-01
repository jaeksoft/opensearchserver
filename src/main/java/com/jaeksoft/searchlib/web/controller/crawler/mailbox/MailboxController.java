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

package com.jaeksoft.searchlib.web.controller.crawler.mailbox;

import javax.naming.NamingException;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.mailbox.MailboxCrawlItem;
import com.jaeksoft.searchlib.crawler.mailbox.MailboxCrawlMaster;
import com.jaeksoft.searchlib.crawler.mailbox.MailboxCrawlThread;
import com.jaeksoft.searchlib.web.controller.crawler.CommonFieldTargetCrawlerController;

@AfterCompose(superclass = true)
public class MailboxController
		extends
		CommonFieldTargetCrawlerController<MailboxCrawlItem, MailboxCrawlThread, MailboxCrawlMaster> {

	public MailboxController() throws SearchLibException, NamingException {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void doDelete(MailboxCrawlItem crawlItem)
			throws SearchLibException {
		// TODO Auto-generated method stub

	}

	@Override
	protected MailboxCrawlItem newCrawlItem(MailboxCrawlItem crawl) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isCrawlerEditRights() throws SearchLibException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	@Command
	public void onSave() throws InterruptedException, SearchLibException {
		// TODO Auto-generated method stub

	}

	@Override
	@Command
	public void onNew() throws SearchLibException {
		// TODO Auto-generated method stub

	}

	@Override
	public MailboxCrawlMaster getCrawlMaster() throws SearchLibException {
		// TODO Auto-generated method stub
		return null;
	}

}
