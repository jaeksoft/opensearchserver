/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.index;

import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.util.XmlWriter;

public abstract class IndexAbstract implements ReaderInterface, WriterInterface {

	protected IndexConfig indexConfig;

	protected IndexAbstract(IndexConfig indexConfig) {
		this.indexConfig = indexConfig;
	}

	public abstract IndexAbstract get(String name);

	public IndexConfig getIndexConfig() {
		return indexConfig;
	}

	public abstract boolean isOnline();

	public abstract void setOnline(boolean v);

	public abstract IndexMode getReadWriteMode();

	public abstract void setReadWriteMode(IndexMode mode)
			throws SearchLibException;

	protected abstract void writeXmlConfigIndex(XmlWriter xmlWriter)
			throws SAXException;

	public void writeXmlConfig(XmlWriter xmlWriter) throws SAXException {
		xmlWriter.startElement("indices");
		writeXmlConfigIndex(xmlWriter);
		xmlWriter.endElement();
	}

}
