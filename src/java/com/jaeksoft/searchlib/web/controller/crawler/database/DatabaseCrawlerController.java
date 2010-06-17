/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.web.controller.crawler.database;

import java.util.Set;

import javax.naming.NamingException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.database.DatabaseCrawl;
import com.jaeksoft.searchlib.web.controller.crawler.CrawlerController;

public class DatabaseCrawlerController extends CrawlerController {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8688620789870696565L;

	private DatabaseCrawl currentCrawl;

	private DatabaseCrawl selectedCrawl;

	private Set<DatabaseCrawl> dbCrawlList;

	public DatabaseCrawlerController() throws SearchLibException,
			NamingException {
		super();
		currentCrawl = new DatabaseCrawl();
		selectedCrawl = null;
		dbCrawlList = null;
	}

	public DatabaseCrawl getCurrentCrawl() {
		return currentCrawl;
	}

	public boolean selected() {
		return selectedCrawl != null;
	}

	public boolean notSelected() {
		return !selected();
	}

	public DatabaseCrawl getSelectedCrawl() {
		return selectedCrawl;
	}

	public void setSelectedCrawl(DatabaseCrawl crawl) throws SearchLibException {
		selectedCrawl = crawl;
		currentCrawl = new DatabaseCrawl(selectedCrawl);
	}

	public void onSave() throws InterruptedException, SearchLibException {
		if (selectedCrawl != null)
			currentCrawl.copyTo(selectedCrawl);
		else
			;
		onCancel();
	}

	public void onCancel() {
		currentCrawl = new DatabaseCrawl();
		selectedCrawl = null;
		reloadPage();
	}

	public void onDelete() throws SearchLibException {
		onCancel();
	}

	public String getCurrentEditMode() throws SearchLibException {
		return selectedCrawl == null ? "Create a new database crawl"
				: "Edit the database crawl " + selectedCrawl.getName();
	}

	@Override
	public void reloadPage() {
		dbCrawlList = null;
		super.reloadPage();
	}

	@Override
	public void reset() {
	}

}
