/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2010 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.file.database;

import java.io.File;

import com.jaeksoft.searchlib.crawler.common.database.Selector;

public class FilePathItem {

	private static final String NO = "no";
	private static final String YES = "yes";

	public enum Status {
		UNDEFINED("Undefined"), INJECTED("Injected"), ALREADY(
				"Already injected"), ERROR("Unknown Error");

		private String name;

		private Status(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	private Status status;
	private File filePath;
	private Selector<FilePathItem> filePathSelector;
	private boolean withSub;

	public FilePathItem() {
		status = Status.UNDEFINED;
		filePath = null;
		filePathSelector = null;
		withSub = false;
	}

	public FilePathItem(File filePath, boolean withSub) {
		this();
		setFilePath(filePath);
		setWithSub(withSub);
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status v) {
		status = v;
	}

	public void setSelected(boolean v) {
		if (v)
			filePathSelector.addSelection(this);
		else
			filePathSelector.removeSelection(this);
	}

	public boolean isSelected() {
		return filePathSelector.isSelected(this);
	}

	public void setFilePath(File filePath) {
		this.filePath = filePath;
	}

	public File getFilePath() {
		return filePath;
	}

	public void setFilePathSelector(Selector<FilePathItem> patternSelector) {
		this.filePathSelector = patternSelector;
	}

	public boolean isWithSub() {
		return withSub;
	}

	public String getWithSubToString() {
		return (withSub ? YES : NO);
	}

	public static boolean parse(String text) {
		if (text == null)
			return false;

		if (text.equals(YES))
			return true;

		return false;
	}

	public void setWithSub(boolean withSub) {
		this.withSub = withSub;
	}

}
