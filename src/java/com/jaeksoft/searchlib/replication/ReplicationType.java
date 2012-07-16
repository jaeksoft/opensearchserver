/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
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

public enum ReplicationType {

	BACKUP_INDEX("Backup", ReplicationItem.NOT_PUSHED_PATH),

	MAIN_INDEX("Main index", ReplicationItem.NOT_PUSHED_PATH,
			ReplicationItem.NOT_PUSHED_PATH_NODB),

	WEB_CRAWLER_URL_DATABASE("Web crawler URL database",
			ReplicationItem.NOT_PUSHED_PATH),

	FILE_CRAWLER_URI_DATABASE("File crawler URI database",
			ReplicationItem.NOT_PUSHED_PATH);

	private final String label;

	private final String[][] notPushedPaths;

	private ReplicationType(String label, String[]... notPushedPaths) {
		this.label = label;
		this.notPushedPaths = notPushedPaths;
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

	public void addNotPushedPath(File sourceDirectory,
			List<File> filesNotPushed, List<File> dirsNotPushed) {
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
}
