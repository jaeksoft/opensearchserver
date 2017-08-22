/**
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
 * <p>
 * http://www.open-search-server.com
 * <p>
 * This file is part of OpenSearchServer.
 * <p>
 * OpenSearchServer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.web.controller.crawler.file;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.file.database.FilePathItem;
import com.jaeksoft.searchlib.web.controller.PushEvent;
import com.jaeksoft.searchlib.web.controller.ScopeAttribute;
import com.jaeksoft.searchlib.web.controller.crawler.CrawlerController;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.GlobalCommand;

import java.util.function.Supplier;

@AfterCompose(superclass = true)
public class FileCrawlerController extends CrawlerController {

	public FileCrawlerController() throws SearchLibException {
		super();
	}

	protected FilePathItem getFilePathItemEdit(Supplier<FilePathItem> defaultValue) {
		FilePathItem filePath = (FilePathItem) getAttribute(ScopeAttribute.FILEPATHITEM_EDIT);
		return filePath != null ? filePath : defaultValue == null ? null : defaultValue.get();

	}

	protected FilePathItem getFilePathItemSelected() {
		return (FilePathItem) getAttribute(ScopeAttribute.FILEPATHITEM_SELECTED);
	}

	protected void setFilePathItemEdit(FilePathItem filePathItem) {
		setAttribute(ScopeAttribute.FILEPATHITEM_EDIT, filePathItem);
		PushEvent.eventEditFileRepository.publish(filePathItem);
	}

	protected void setFilePathItemSelected(FilePathItem filePathItem) {
		setAttribute(ScopeAttribute.FILEPATHITEM_SELECTED, filePathItem);
	}

	public boolean isFilePathEdit() {
		return getFilePathItemEdit(null) != null;
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

	@Override
	protected void reset() throws SearchLibException {
		setFilePathItemEdit(null);
		setFilePathItemSelected(null);
	}

	@GlobalCommand
	@Override
	public void eventEditFileRepository(@BindingParam("filePathItem") FilePathItem filePathItem)
			throws SearchLibException {
		reload();
	}
}
