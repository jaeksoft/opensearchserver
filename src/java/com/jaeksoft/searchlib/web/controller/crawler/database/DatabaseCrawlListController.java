/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2010-2011 Emmanuel Keller / Jaeksoft
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

import java.io.IOException;
import java.util.List;

import javax.naming.NamingException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Messagebox;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.database.DatabaseCrawl;
import com.jaeksoft.searchlib.crawler.database.DatabaseCrawlList;
import com.jaeksoft.searchlib.crawler.database.DatabaseCrawlMaster;
import com.jaeksoft.searchlib.crawler.database.DatabaseDriverNames;
import com.jaeksoft.searchlib.crawler.database.DatabaseFieldTarget;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.util.map.GenericLink;
import com.jaeksoft.searchlib.web.controller.AlertController;
import com.jaeksoft.searchlib.web.controller.crawler.CrawlerController;

public class DatabaseCrawlListController extends CrawlerController {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2176688319013998120L;

	private class DeleteAlert extends AlertController {

		private DatabaseCrawl dbcrawl;

		protected DeleteAlert(DatabaseCrawl dbcrawl)
				throws InterruptedException {
			super("Please, confirm that you want to delete the crawl process: "
					+ dbcrawl.getName(), Messagebox.YES | Messagebox.NO,
					Messagebox.QUESTION);
			this.dbcrawl = dbcrawl;
		}

		@Override
		protected void onYes() throws SearchLibException {
			getDatabaseCrawlList().remove(dbcrawl);
			getClient().saveDatabaseCrawlList();
			onCancel();
		}
	}

	private transient DatabaseCrawl currentCrawl;

	private transient DatabaseCrawl selectedCrawl;

	private transient DatabaseCrawlList dbCrawlList;

	private transient String sqlColumn;

	private transient SchemaField selectedIndexField;

	private transient DatabaseFieldTarget dbFieldTarget;

	public DatabaseCrawlListController() throws SearchLibException,
			NamingException {
		super();
	}

	@Override
	protected void reset() throws SearchLibException {
		currentCrawl = null;
		DatabaseCrawlMaster cm = getCrawlMaster();
		if (cm != null)
			new DatabaseCrawl(cm);
		selectedCrawl = null;
		dbCrawlList = null;
		sqlColumn = null;
		dbFieldTarget = new DatabaseFieldTarget(null, false, false, false,
				null, false);
		selectedIndexField = null;
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

	public boolean isEditing() {
		return currentCrawl != null;
	}

	public boolean isNotEditing() {
		return !isEditing();
	}

	public DatabaseCrawl getSelectedCrawl() {
		return selectedCrawl;
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

	public void setSelectedCrawl(DatabaseCrawl crawl) throws SearchLibException {
		selectedCrawl = crawl;
		currentCrawl = new DatabaseCrawl(getCrawlMaster(), selectedCrawl);
		reloadPage();
	}

	public void onAddField() throws SearchLibException,
			TransformerConfigurationException, SAXException, IOException,
			XPathExpressionException, ParserConfigurationException {
		if (!isFileCrawlerParametersRights())
			throw new SearchLibException("Not allowed");
		if (dbFieldTarget == null || dbFieldTarget.getName() == null
				|| dbFieldTarget.getName().length() == 0)
			return;
		currentCrawl.getFieldMap().add(sqlColumn, dbFieldTarget);
		sqlColumn = null;
		selectedIndexField = null;
		dbFieldTarget = new DatabaseFieldTarget(null, false, false, false,
				null, false);
		reloadPage();
	}

	@SuppressWarnings("unchecked")
	public void removeLink(Component comp) throws SearchLibException,
			InterruptedException {
		if (comp == null)
			return;
		GenericLink<String, DatabaseFieldTarget> fieldLink = (GenericLink<String, DatabaseFieldTarget>) comp
				.getAttribute("fieldlink");
		if (fieldLink == null)
			return;
		currentCrawl.getFieldMap().remove(fieldLink);
		reloadPage();
	}

	public void onSave() throws InterruptedException, SearchLibException {
		getDatabaseCrawlList();
		if (selectedCrawl != null)
			currentCrawl.copyTo(selectedCrawl);
		else {
			if (dbCrawlList.get(currentCrawl.getName()) != null) {
				new AlertController("The crawl name is already used");
				return;
			}
			dbCrawlList.add(currentCrawl);
		}
		getClient().saveDatabaseCrawlList();
		onCancel();
	}

	public void onNew() throws SearchLibException {
		DatabaseCrawl oldCurrentCrawl = currentCrawl;
		selectedCrawl = null;
		currentCrawl = new DatabaseCrawl(getCrawlMaster());
		if (oldCurrentCrawl != null)
			oldCurrentCrawl.copyTo(currentCrawl);
		currentCrawl.setName(null);
		reloadPage();
	}

	public void onCancel() throws SearchLibException {
		currentCrawl = null;
		selectedCrawl = null;
		reloadPage();
	}

	public void onTimer() {
		super.reloadPage();
	}

	private DatabaseCrawl getDatabaseCrawlItem(Component comp) {
		if (comp == null)
			return null;
		return (DatabaseCrawl) comp.getAttribute("dbcrawlitem");
	}

	public void delete(Component comp) throws SearchLibException,
			InterruptedException {
		DatabaseCrawl item = getDatabaseCrawlItem(comp);
		if (item == null)
			return;
		new DeleteAlert(item);
	}

	public void edit(Component comp) throws SearchLibException,
			InterruptedException {
		DatabaseCrawl item = getDatabaseCrawlItem(comp);
		if (item == null)
			return;
		setSelectedCrawl(item);
	}

	public void execute(Component comp) throws SearchLibException,
			InterruptedException {
		DatabaseCrawl item = getDatabaseCrawlItem(comp);
		if (item == null)
			return;
		Client client = getClient();
		if (client == null)
			return;
		getCrawlMaster().execute(client, item, false);
		reloadPage();
	}

	private DatabaseCrawlMaster getCrawlMaster() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		return client.getDatabaseCrawlMaster();
	}

	public String getCurrentEditMode() throws SearchLibException {
		return selectedCrawl == null ? "Create a new database crawl process"
				: "Edit the database crawl process : "
						+ selectedCrawl.getName();
	}

	public List<SchemaField> getIndexFieldList() throws SearchLibException {
		synchronized (this) {
			Client client = getClient();
			if (client == null)
				return null;
			List<SchemaField> list = client.getSchema().getFieldList()
					.getList();
			if (list.size() > 0 && selectedIndexField == null)
				setSelectedIndexField(list.get(0));
			return list;
		}
	}

	public void setSelectedIndexField(SchemaField field) {
		synchronized (this) {
			selectedIndexField = field;
			dbFieldTarget.setName(field.getName());
		}
	}

	public SchemaField getSelectedIndexField() {
		synchronized (this) {
			return selectedIndexField;
		}
	}

	public boolean isRefresh() throws SearchLibException {
		DatabaseCrawlMaster crawlMaster = getCrawlMaster();
		if (crawlMaster == null)
			return false;
		return crawlMaster.getThreadsCount() > 0;
	}

	@Override
	public void reloadPage() {
		dbCrawlList = null;
		super.reloadPage();
	}

	/**
	 * @return the dbFieldTarget
	 */
	public DatabaseFieldTarget getDbFieldTarget() {
		return dbFieldTarget;
	}

	/**
	 * @return the sqlColumn
	 */
	public String getSqlColumn() {
		return sqlColumn;
	}

	/**
	 * @param sqlColumn
	 *            the sqlColumn to set
	 */
	public void setSqlColumn(String sqlColumn) {
		this.sqlColumn = sqlColumn;
	}

}
