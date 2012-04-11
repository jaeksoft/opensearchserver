/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.parser;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.parser.torrent.MetaInfo;
import com.jaeksoft.searchlib.streamlimiter.StreamLimiter;

public class TorrentParser extends Parser {

	private static ParserFieldEnum[] fl = { ParserFieldEnum.name,
			ParserFieldEnum.announce, ParserFieldEnum.total_length,
			ParserFieldEnum.file_length, ParserFieldEnum.file_path,
			ParserFieldEnum.info_hash, ParserFieldEnum.info_hash_urlencoded,
			ParserFieldEnum.comment, ParserFieldEnum.creation_date };

	public TorrentParser() {
		super(fl);
	}

	@Override
	public void initProperties() throws SearchLibException {
		super.initProperties();
		addProperty(ClassPropertyEnum.SIZE_LIMIT, "0", null);
	}

	/**
	 * Naveen native utils ;)
	 * 
	 * @param in
	 * @return
	 */
	private static String geturlhash(byte in[]) {
		byte ch = 0x00;
		int i = 0;
		if (in == null || in.length <= 0)
			return null;

		String pseudo[] = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
				"A", "B", "C", "D", "E", "F" };
		StringBuffer out = new StringBuffer(in.length * 2);

		while (i < in.length) {
			// First check to see if we need ASCII or HEX
			if ((in[i] >= '0' && in[i] <= '9')
					|| (in[i] >= 'a' && in[i] <= 'z')
					|| (in[i] >= 'A' && in[i] <= 'Z') || in[i] == '$'
					|| in[i] == '-' || in[i] == '_' || in[i] == '.'
					|| in[i] == '+' || in[i] == '!') {
				out.append((char) in[i]);
				i++;
			} else {
				out.append('%');
				ch = (byte) (in[i] & 0xF0); // Strip off high nibble
				ch = (byte) (ch >>> 4); // shift the bits down
				ch = (byte) (ch & 0x0F); // must do this is high order bit is
				// on!
				out.append(pseudo[(int) ch]); // convert the nibble to a
				// String Character
				ch = (byte) (in[i] & 0x0F); // Strip off low nibble
				out.append(pseudo[(int) ch]); // convert the nibble to a
				// String Character
				i++;
			}
		}

		return new String(out);
	}

	@Override
	protected void parseContent(StreamLimiter streamLimiter, LanguageEnum lang)
			throws IOException {

		MetaInfo meta = new MetaInfo(streamLimiter.getNewInputStream());
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
			byte[] infoHash = meta.getInfoHash();
			addField(ParserFieldEnum.info_hash, Hex.encodeHexString(infoHash));
			addField(ParserFieldEnum.info_hash_urlencoded, geturlhash(infoHash));
		} catch (NoSuchAlgorithmException e) {
			throw new IOException(e);
		}

		addField(ParserFieldEnum.comment, meta.getComment());

		addField(ParserFieldEnum.creation_date,
				Long.toString(meta.getCreationDate()));

	}

}
