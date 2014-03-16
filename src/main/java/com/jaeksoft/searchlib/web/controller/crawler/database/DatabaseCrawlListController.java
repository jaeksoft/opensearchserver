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

package com.jaeksoft.searchlib.web.controller.crawler.database;

import java.io.IOException;
import java.sql.SQLException;

import javax.naming.NamingException;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.database.DatabaseCrawlAbstract;
import com.jaeksoft.searchlib.crawler.database.DatabaseCrawlEnum;
import com.jaeksoft.searchlib.crawler.database.DatabaseCrawlList;
import com.jaeksoft.searchlib.crawler.database.DatabaseCrawlMaster;
import com.jaeksoft.searchlib.crawler.database.DatabaseCrawlSql;
import com.jaeksoft.searchlib.crawler.database.DatabaseCrawlSql.SqlUpdateMode;
import com.jaeksoft.searchlib.crawler.database.DatabaseCrawlThread;
import com.jaeksoft.searchlib.crawler.database.DatabaseDriverNames;
import com.jaeksoft.searchlib.crawler.database.IsolationLevelEnum;
import com.jaeksoft.searchlib.web.controller.AlertController;
import com.jaeksoft.searchlib.web.controller.crawler.CommonFieldTargetCrawlerController;

@AfterCompose(superclass = true)
public class DatabaseCrawlListController
		extends
		CommonFieldTargetCrawlerController<DatabaseCrawlAbstract, DatabaseCrawlThread, DatabaseCrawlMaster> {

	private transient DatabaseCrawlList dbCrawlList;

	private DatabaseCrawlEnum dbCrawlType;

	public DatabaseCrawlListController() throws SearchLibException,
			NamingException {
		super();
	}

	@Override
	protected void reset() throws SearchLibException {
		super.reset();
		dbCrawlList = null;
		setDbCrawlType(DatabaseCrawlEnum.DB_SQL);
	}

	public DatabaseCrawlList getDatabaseCrawlList() throws SearchLibException {
		if (dbCrawlList != null)
			return dbCrawlList;
		Client client = getClient();
		if (client == null)
			return null;
		dbCrawlList = client.getDatabaseCrawlList();
		return dbCrawlList;
	}

	public String[] getDriverClassList() {
		return DatabaseDriverNames.getAvailableList(getDesktop().getWebApp()
				.getClass().getClassLoader());
	}

	@Override
	@Command
	public void onSave() throws InterruptedException, SearchLibException,
			IOException {
		getDatabaseCrawlList();
		if (getSelectedCrawl() != null)
			getCurrentCrawl().copyTo(getSelectedCrawl());
		else {
			if (dbCrawlList.get(getCurrentCrawl().getName()) != null) {
				new AlertController("The crawl name is already used");
				return;
			}
			dbCrawlList.add(getCurrentCrawl());
		}
		getClient().saveDatabaseCrawlList();
		onCancel();
	}

	@Override
	@Command
	public void onNew() throws SearchLibException {
		DatabaseCrawlAbstract oldCurrentCrawl = getCurrentCrawl();
		setSelectedCrawl(null);
		DatabaseCrawlAbstract newCrawl = null;
		switch (dbCrawlType) {
		case DB_SQL:
			newCrawl = new DatabaseCrawlSql(getCrawlMaster());
			break;
		case DB_NO_SQL:
			newCrawl = null;
			break;
		}
		setCurrentCrawl(newCrawl);
		if (oldCurrentCrawl != null)
			oldCurrentCrawl.copyTo(newCrawl);
		newCrawl.setName(null);
		reload();
	}

	@Override
	@Command
	public void reload() throws SearchLibException {
		dbCrawlList = null;
		super.reload();
	}

	@Command
	public void onCheckSqlSelect() throws SearchLibException,
			InterruptedException, InstantiationException,
			IllegalAccessException, ClassNotFoundException, SQLException {
		DatabaseCrawlAbstract crawl = getCurrentCrawl();
		if (crawl == null)
			throw new SearchLibException("No crawl selected");
		if (!(crawl instanceof DatabaseCrawlSql))
			throw new SearchLibException("Wrong crawl type (expecting DB SQL)");
		DatabaseCrawlSql crawlSql = (DatabaseCrawlSql) crawl;
		new AlertController(crawlSql.checkSqlSelect());
	}

	public DatabaseCrawlEnum[] getDatabaseCrawlTypes() {
		return DatabaseCrawlEnum.values();
	}

	public IsolationLevelEnum[] getIsolationLevels() {
		return IsolationLevelEnum.values();
	}

	public SqlUpdateMode[] getSqlUpdateModes() {
		return DatabaseCrawlSql.SqlUpdateMode.values();
	}

	@Override
	protected void doDelete(DatabaseCrawlAbstract crawlItem)
			throws SearchLibException {
		getClient().getDatabaseCrawlList().remove(crawlItem);
	}

	@Override
	protected DatabaseCrawlAbstract newCrawlItem(DatabaseCrawlAbstract crawl) {
		return crawl.duplicate();
	}

	@Override
	public boolean isCrawlerEditRights() throws SearchLibException {
		return isDatabaseCrawlerEditPatternsRights();
	}

	@Override
	public DatabaseCrawlMaster getCrawlMaster() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		return client.getDatabaseCrawlMaster();
	}

	/**
	 * @return the dbCrawlType
	 */
	public DatabaseCrawlEnum getDbCrawlType() {
		return dbCrawlType;
	}

	/**
	 * @param dbCrawlType
	 *            the dbCrawlType to set
	 */
	public void setDbCrawlType(DatabaseCrawlEnum dbCrawlType) {
		this.dbCrawlType = dbCrawlType;
	}

}
