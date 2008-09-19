/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft SearchLib Community.
 *
 * Jaeksoft SearchLib Community is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft SearchLib Community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft SearchLib Community. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.index;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.LockObtainFailedException;
import org.w3c.dom.Node;

import com.jaeksoft.searchlib.remote.Remote;
import com.jaeksoft.searchlib.remote.UrlWriteObject;
import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.util.Md5Spliter;
import com.jaeksoft.searchlib.util.XPathParser;

public class WriterRemote extends WriterAbstract {

	private Remote remote;
	private Md5Spliter md5spliter = null;
	private String keyField = null;

	public WriterRemote(Remote remote, String indexName, String keyField,
			String keyMd5Pattern) {
		super(indexName);
		this.keyField = keyField;
		if (keyMd5Pattern != null)
			md5spliter = new Md5Spliter(keyMd5Pattern);
		this.remote = remote;
	}

	@Override
	protected boolean acceptDocument(IndexDocument document)
			throws NoSuchAlgorithmException {
		if (keyField == null)
			return true;
		if (md5spliter == null)
			return true;
		FieldContent fieldContent = document.getField(keyField);
		if (fieldContent == null)
			return false;
		return md5spliter.acceptAnyKey(fieldContent.getValues());
	}

	private URL getUrl() throws UnsupportedEncodingException,
			MalformedURLException {
		String url = remote.getUrl() + "index?index="
				+ URLEncoder.encode(remote.getName(), "UTF-8")
				+ "&render=object&forceLocal";
		return new URL(url);
	}

	public void xmlInfo(PrintWriter writer, HashSet<String> classDetail) {
		remote.xmlInfo(writer, classDetail);
	}

	public void optimize(String indexName, boolean forceLocal)
			throws CorruptIndexException, LockObtainFailedException,
			IOException {
		if (forceLocal)
			return;
		// TODO Auto-generated method stub
	}

	public void updateDocument(Schema schema, IndexDocument document,
			boolean forceLocal) throws NoSuchAlgorithmException, IOException {
		if (forceLocal)
			return;
		if (!acceptDocument(document))
			return;
		IOException err = null;
		UrlWriteObject writeObject = null;
		try {
			writeObject = new UrlWriteObject(getUrl(), document);
			if (writeObject.getResponseCode() != 200)
				throw new IOException(writeObject.getResponseCode() + " "
						+ writeObject.getResponseMessage() + ")");
		} catch (IOException e) {
			e.printStackTrace();
			err = e;
		} finally {
			if (writeObject != null)
				writeObject.close();
			if (err != null)
				throw err;
		}
	}

	public void deleteDocuments(Schema schema, String uniqueField,
			boolean forceLocal) throws CorruptIndexException,
			LockObtainFailedException, IOException {
		// TODO Auto-generated method stub
	}

	public static WriterRemote fromXmlConfig(String indexName, XPathParser xpp,
			Node node) {
		if (indexName == null)
			return null;
		Remote remote = Remote.fromXmlConfig(node, "remoteIndexPath");
		if (remote == null)
			return null;
		String keyField = XPathParser.getAttributeString(node, "keyField");
		String keyMd5Pattern = XPathParser.getAttributeString(node,
				"keyMd5RegExp");
		return new WriterRemote(remote, indexName, keyField, keyMd5Pattern);
	}

}
