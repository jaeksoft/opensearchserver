/**
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2008-2014 Emmanuel Keller / Jaeksoft
 * <p>
 * http://www.open-search-server.com
 * <p>
 * This file is part of OpenSearchServer.
 * <p>
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.
 * If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.template;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.util.IOUtils;
import com.jaeksoft.searchlib.util.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;

public abstract class TemplateAbstract {

	private final String rootPath;

	private final String[] resources;

	private final String publicName;

	private final String description;

	protected TemplateAbstract(String rootPath, String[] resources, String publicName, String description) {
		this.rootPath = rootPath;
		this.resources = resources;
		this.publicName = publicName;
		this.description = description;
	}

	public String getPublicName() {
		return publicName;
	}

	public String getDescription() {
		return description;
	}

	public void createIndex(File indexDir, URI remoteURI) throws SearchLibException, IOException {

		if (!indexDir.mkdir())
			throw new SearchLibException("directory creation failed (" + indexDir + ")");

		InputStream is = null;
		FileWriter target = null;
		for (String resource : resources) {

			String res = rootPath + '/' + resource;
			is = getClass().getResourceAsStream(res);
			if (is == null)
				is = getClass().getResourceAsStream("common" + '/' + resource);
			if (is == null)
				throw new SearchLibException("Unable to find resource " + res);

			if (resource.equals("config.xml")) {
				StringWriter writer = new StringWriter();
				IOUtils.copy(is, writer, "UTF-8");
				String newConfig = StringUtils.replace(writer.toString(), "{remoteURI}",
						remoteURI == null ? "" : StringEscapeUtils.escapeXml11(remoteURI.toString()));
				IOUtils.closeQuietly(is);
				is = IOUtils.toInputStream(newConfig, "UTF-8");
			}

			try {
				File f = new File(indexDir, resource);
				if (f.getParentFile() != indexDir)
					f.getParentFile().mkdirs();
				target = new FileWriter(f);
				IOUtils.copy(is, target, "UTF-8");
			} finally {
				IOUtils.close(target, is);
			}
		}
	}
}
