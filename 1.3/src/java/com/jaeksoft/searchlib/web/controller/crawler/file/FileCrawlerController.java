/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of OpenSearchServer.
 *
 * OpenSearchServer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OpenSearchServer.  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.web.controller.crawler.file;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.file.database.FilePathItem;
import com.jaeksoft.searchlib.web.controller.ScopeAttribute;
import com.jaeksoft.searchlib.web.controller.crawler.CrawlerController;

public class FileCrawlerController extends CrawlerController {

	/**
	 * 
	 */
	private static final long serialVersionUID = -38518959461973218L;

	public FileCrawlerController() throws SearchLibException {
		super();
	}

	protected FilePathItem getFilePathItemEdit() {
		return (FilePathItem) getAttribute(ScopeAttribute.FILEPATHITEM_EDIT);
	}

	protected FilePathItem getFilePathItemSelected() {
		return (FilePathItem) getAttribute(ScopeAttribute.FILEPATHITEM_SELECTED);
	}

	protected void setFilePathItemEdit(FilePathItem filePathItem) {
		setAttribute(ScopeAttribute.FILEPATHITEM_EDIT, filePathItem);
	}

	protected void setFilePathItemSelected(FilePathItem filePathItem) {
		setAttribute(ScopeAttribute.FILEPATHITEM_SELECTED, filePathItem);
	}

	public boolean isFilePathEdit() {
		return getFilePathItemEdit() != null;
	}

	public boolean isNoFilePathEdit() {
		return !isFilePathEdit();
	}

	public boolean isFilePathSelected() {
		return getFilePathItemSelected() != null;
	}

	public boolean isNoFilePathSelected() {
		return !isFilePathSelected();
	}

	protected void reloadFileCrawlerPages() throws SearchLibException {
		FileCrawlerController ctrl = (FileCrawlerController) getParent()
				.getFellowIfAny("filecrawler", true);
		sendReload(ctrl);
	}

}
