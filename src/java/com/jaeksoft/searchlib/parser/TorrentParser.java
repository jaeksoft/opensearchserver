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
import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.klomp.snark.MetaInfo;

public class TorrentParser extends Parser {

	private static ParserFieldEnum[] fl = { ParserFieldEnum.filename,
			ParserFieldEnum.content_type, ParserFieldEnum.name,
			ParserFieldEnum.announce, ParserFieldEnum.totalLength,
			ParserFieldEnum.files, ParserFieldEnum.lengths,
			ParserFieldEnum.infoHash };

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
		addField(ParserFieldEnum.totalLength,
				Long.toString(meta.getTotalLength()));
		List<?> lengths = meta.getLengths();
		if (lengths != null)
			for (Object o : lengths)
				addField(ParserFieldEnum.lengths, o.toString());
		List<?> files = meta.getFiles();
		if (files != null)
			for (Object o : files)
				addField(ParserFieldEnum.files, o.toString());

		addField(ParserFieldEnum.infoHash,
				new String(Hex.encodeHex(meta.getInfoHash())));
	}

	@Override
	protected void parseContent(LimitReader reader) throws IOException {
		throw new IOException("Not supported");
	}

}
