/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2014 Emmanuel Keller / Jaeksoft
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

import java.io.IOException;

import javax.mail.MessagingException;
import javax.naming.NamingException;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.mailbox.MailboxCrawlItem;
import com.jaeksoft.searchlib.crawler.mailbox.MailboxCrawlList;
import com.jaeksoft.searchlib.crawler.mailbox.MailboxCrawlMaster;
import com.jaeksoft.searchlib.crawler.mailbox.MailboxCrawlThread;
import com.jaeksoft.searchlib.crawler.mailbox.MailboxFieldEnum;
import com.jaeksoft.searchlib.crawler.mailbox.MailboxProtocolEnum;
import com.jaeksoft.searchlib.web.controller.AlertController;
import com.jaeksoft.searchlib.web.controller.crawler.CommonFieldTargetCrawlerController;

@AfterCompose(superclass = true)
public class MailboxController
		extends
		CommonFieldTargetCrawlerController<MailboxCrawlItem, MailboxCrawlThread, MailboxCrawlMaster> {

	private transient MailboxCrawlList crawlList = null;

	public MailboxController() throws SearchLibException, NamingException {
		super();
	}

	public String[] getServerProtocols() {
		return MailboxProtocolEnum.labelArray;
	}

	public String[] getMailboxCrawlerFields() {
		return MailboxFieldEnum.labelArray;
	}

	@Override
	protected void reset() throws SearchLibException {
		super.reset();
		crawlList = null;
	}

	public MailboxCrawlList getMailboxCrawlList() throws SearchLibException {
		if (crawlList != null)
			return crawlList;
		Client client = getClient();
		if (client == null)
			return null;
		crawlList = client.getMailboxCrawlList();
		return crawlList;
	}

	@Override
	protected void doDelete(MailboxCrawlItem crawlItem)
			throws SearchLibException {
		Client client = getClient();
		client.getMailboxCrawlList().remove(crawlItem);
		client.saveMailboxCrawlList();
	}

	@Override
	protected MailboxCrawlItem newCrawlItem(MailboxCrawlItem crawl) {
		return crawl.duplicate();
	}

	@Override
	public boolean isCrawlerEditRights() throws SearchLibException {
		return super.isMailboxCrawlerEditRights();
	}

	@Override
	@Command
	public void onSave() throws InterruptedException, SearchLibException {
		getMailboxCrawlList();
		if (getSelectedCrawl() != null)
			getCurrentCrawl().copyTo(getSelectedCrawl());
		else {
			if (crawlList.get(getCurrentCrawl().getName()) != null) {
				new AlertController("The crawl name is already used");
				return;
			}
			crawlList.add(getCurrentCrawl());
		}
		getClient().saveMailboxCrawlList();
		onCancel();
	}

	@Override
	@Command
	public void onNew() throws SearchLibException {
		MailboxCrawlItem oldCurrentCrawl = getCurrentCrawl();
		setSelectedCrawl(null);
		MailboxCrawlItem newCrawl = new MailboxCrawlItem(getCrawlMaster());
		setCurrentCrawl(newCrawl);
		if (oldCurrentCrawl != null)
			oldCurrentCrawl.copyTo(newCrawl);
		newCrawl.setName(null);
		reload();
	}

	@Command
	public void onCheck() throws InterruptedException, InstantiationException,
			IllegalAccessException, MessagingException, IOException,
			SearchLibException {
		MailboxCrawlItem crawl = getCurrentCrawl();
		if (crawl == null)
			return;
		new AlertController("Test results: " + crawl.check());
	}

	@Override
	public void doClone(MailboxCrawlItem crawl) throws SearchLibException {
		setSelectedCrawl(null);
		MailboxCrawlItem newCrawl = crawl.duplicate();
		newCrawl.setName(null);
		setCurrentCrawl(newCrawl);
		reload();
	}

	@Override
	@Command
	public void reload() throws SearchLibException {
		crawlList = null;
		super.reload();
	}

	@Override
	public MailboxCrawlMaster getCrawlMaster() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		return client.getMailboxCrawlMaster();
	}

	@Override
	public boolean isRefresh() throws SearchLibException {
		MailboxCrawlMaster crawlMaster = getCrawlMaster();
		if (crawlMaster == null)
			return false;
		return crawlMaster.getThreadsCount() > 0;
	}

	@Override
	@Command
	public void onTimer() throws SearchLibException {
		reload();
	}
}
