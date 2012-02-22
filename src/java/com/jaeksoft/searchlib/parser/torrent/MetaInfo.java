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

package com.jaeksoft.searchlib.parser.torrent;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MetaInfo {

	public BDictionary metaDictionary;
	public BDictionary infoDictionary;
	public BList filesList;

	public MetaInfo(InputStream inputStream) throws IOException {
		metaDictionary = null;
		infoDictionary = null;
		BValue v = BValue.next(inputStream);
		if (v == null || !(v instanceof BDictionary))
			throw new BException();
		metaDictionary = (BDictionary) v;
		v = metaDictionary.get("info");
		if (v == null || !(v instanceof BDictionary))
			throw new BException();
		infoDictionary = (BDictionary) v;
		v = infoDictionary.get("files");
		if (v != null & !(v instanceof BList))
			throw new BException();
		filesList = (BList) v;
	}

	private long getInteger(BValue v) throws BException {
		if (v == null || !(v instanceof BInteger))
			throw new BException();
		return ((BInteger) v).getInteger();
	}

	private String getString(BValue v) throws BException {
		if (v == null || !(v instanceof BString))
			throw new BException();
		return ((BString) v).getString();
	}

	private long getOptionalInteger(BValue v) throws BException {
		if (v == null || !(v instanceof BInteger))
			return 0;
		return ((BInteger) v).getInteger();
	}

	private String getOptionalString(BValue v) throws BException {
		if (v == null || !(v instanceof BString))
			return null;
		return ((BString) v).getString();
	}

	public String getName() throws BException {
		return getString(infoDictionary.get("name"));
	}

	public String getAnnounce() throws BException {
		return getString(metaDictionary.get("announce"));
	}

	public long getTotalLength() throws BException {
		long l = getLength();
		int s = getFilesCount();
		while (s != 0)
			l += getFileLength(--s);
		return l;
	}

	private BDictionary getFile(int index) throws BException {
		if (filesList == null)
			return null;
		BValue v = filesList.get(index);
		if (!(v instanceof BDictionary))
			throw new BException();
		return (BDictionary) v;
	}

	public int getFilesCount() {
		if (filesList == null)
			return 0;
		return filesList.size();
	}

	public long getFileLength(int index) throws BException {
		BDictionary fileDict = getFile(index);
		if (fileDict == null)
			throw new BException();
		return getInteger(fileDict.get("length"));
	}

	public String getFilePath(int index) throws BException {
		BDictionary fileDict = getFile(index);
		if (fileDict == null)
			throw new BException();
		BValue v = fileDict.get("path");
		if (v == null)
			throw new BException();
		if (!(v instanceof BList))
			throw new BException();
		return ((BList) v).getFilePath();
	}

	public byte[] getInfoHash() throws NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance("SHA");
		return digest.digest(infoDictionary.byteArray.toByteArray());
	}

	public String getPieces() throws BException {
		return getString(infoDictionary.get("pieces"));
	}

	public String getPiece(int index) throws BException {
		String pieces = getPieces();
		if (pieces == null)
			return null;
		return pieces.substring(index * 20, (index + 1) * 20);
	}

	public long getLength() throws BException {
		return getOptionalInteger(infoDictionary.get("length"));
	}

	public long getPieceLength() throws BException {
		return getInteger(infoDictionary.get("piece length"));
	}

	public String getComment() throws BException {
		return getOptionalString(metaDictionary.get("comment"));
	}

	public long getCreationDate() throws BException {
		return getOptionalInteger(metaDictionary.get("creation date"));
	}
}
