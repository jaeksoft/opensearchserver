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

package com.jaeksoft.searchlib.parser;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import com.jaeksoft.searchlib.parser.torrent.MetaInfo;

public class TorrentParser extends Parser {

	private static ParserFieldEnum[] fl = { ParserFieldEnum.filename,
			ParserFieldEnum.content_type, ParserFieldEnum.name,
			ParserFieldEnum.announce, ParserFieldEnum.total_length,
			ParserFieldEnum.file_length, ParserFieldEnum.file_path,
			ParserFieldEnum.info_hash, ParserFieldEnum.comment,
			ParserFieldEnum.creation_date };

	public TorrentParser() {
		super(fl);
	}

	@Override
	public ParserFieldEnum[] getParserFieldList() {
		return fl;
	}

	@Override
	protected void parseContent(LimitInputStream inputStream)
			throws IOException {

		MetaInfo meta = new MetaInfo(inputStream);
		addField(ParserFieldEnum.name, meta.getName());
		addField(ParserFieldEnum.announce, meta.getAnnounce());
		addField(ParserFieldEnum.total_length,
				Long.toString(meta.getTotalLength()));
		int l = meta.getFilesCount();
		for (int i = 0; i < l; i++) {
			addField(ParserFieldEnum.file_length, meta.getFileLength(i));
			addField(ParserFieldEnum.file_path, meta.getFilePath(i));
		}

		try {
			addField(ParserFieldEnum.info_hash, meta.getInfoHash());
		} catch (NoSuchAlgorithmException e) {
			throw new IOException(e);
		}

		addField(ParserFieldEnum.comment, meta.getComment());

		addField(ParserFieldEnum.creation_date,
				Integer.toString(meta.getCreationDate()));

	}

	@Override
	protected void parseContent(LimitReader reader) throws IOException {
		throw new IOException("Not supported");
	}

}
