/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012-2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.replication;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public enum ReplicationType {

	BACKUP_INDEX("Backup", FinalMode.SWITCH, null,
			ReplicationItem.NOT_PUSHED_PATH),

	MAIN_INDEX("Main index", FinalMode.SWITCH, null,
			ReplicationItem.NOT_PUSHED_PATH,
			ReplicationItem.NOT_PUSHED_PATH_NODB),

	WEB_CRAWLER_URL_DATABASE("Web crawler URL database", FinalMode.SWITCH,
			null, ReplicationItem.NOT_PUSHED_PATH),

	FILE_CRAWLER_URI_DATABASE("File crawler URI database", FinalMode.SWITCH,
			null, ReplicationItem.NOT_PUSHED_PATH),

	SCHEMA_ONLY("Schema only", FinalMode.MERGE,
			ReplicationItem.NOT_PUSHED_DATA_FOLDERS,
			ReplicationItem.NOT_PUSHED_PATH, ReplicationItem.NOT_PUSHED_INDEX,
			ReplicationItem.NOT_PUSHED_DATA_PATH),

	MAIN_DATA_COPY("Main data copy", null, null);

	public enum FinalMode {
		SWITCH, MERGE;

	}

	private final String label;

	private final String[][] notPushedPaths;

	private final Set<String> notPushedFolderNames;

	private final FinalMode mode;

	private ReplicationType(final String label, final FinalMode mode,
			final String[] notPushedFolderNames,
			final String[]... notPushedPaths) {
		this.label = label;
		this.notPushedPaths = notPushedPaths;
		this.notPushedFolderNames = notPushedFolderNames != null ? new TreeSet<String>()
				: null;
		if (notPushedFolderNames != null)
			for (String folder : notPushedFolderNames)
				this.notPushedFolderNames.add(folder);
		this.mode = mode;
	}

	@Override
	final public String toString() {
		return label;
	}

	final public static ReplicationType find(String attributeString) {
		if (attributeString != null)
			for (ReplicationType type : values())
				if (type.name().equals(attributeString))
					return type;
		return MAIN_INDEX;
	}

	final public void addNotPushedPath(final File sourceDirectory,
			final List<File> filesNotPushed, final List<File> dirsNotPushed) {
		if (notPushedPaths == null)
			return;
		for (String[] notPushedPath : notPushedPaths) {
			for (String path : notPushedPath) {
				File f = new File(sourceDirectory, path);
				if (f.isFile())
					filesNotPushed.add(f);
				else if (f.isDirectory())
					dirsNotPushed.add(f);
			}
		}
	}

	final public boolean isNotPushedFolder(final File file) {
		if (notPushedFolderNames == null)
			return false;
		if (!file.isDirectory())
			return false;
		return notPushedFolderNames.contains(file.getName());
	}

	final public String getLabel() {
		return label;
	}

	final public FinalMode getFinalMode() {
		return mode;
	}
}
