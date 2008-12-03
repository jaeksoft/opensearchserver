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
import java.util.List;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.LockObtainFailedException;

import com.jaeksoft.searchlib.remote.Remote;
import com.jaeksoft.searchlib.remote.UrlRead;
import com.jaeksoft.searchlib.remote.UrlWriteObject;
import com.jaeksoft.searchlib.schema.Schema;

public class WriterRemote extends WriterAbstract {

	private Remote remote;

	public WriterRemote(Remote remote, String indexName, String keyField,
			String keyMd5Pattern) {
		super(indexName, keyField, keyMd5Pattern);
		this.remote = remote;
	}

	private URL getCommandUrl(String command)
			throws UnsupportedEncodingException, MalformedURLException {
		String url = remote.getUrl(command + "?index="
				+ URLEncoder.encode(remote.getName(), "UTF-8") + "&forceLocal");
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
		UrlRead urlRead = null;
		try {
			String url = getCommandUrl("optimize").toExternalForm();
			urlRead = new UrlRead(url);
			if (urlRead.getResponseCode() != 200)
				throw new IOException(url + " returns "
						+ urlRead.getResponseMessage() + "("
						+ urlRead.getResponseCode() + ")");
		} catch (IOException e) {
			throw e;
		} finally {
			if (urlRead != null)
				urlRead.close();
		}
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
			writeObject = new UrlWriteObject(getCommandUrl("index"), document);
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

	public void updateDocuments(Schema schema,
			List<? extends IndexDocument> documents, boolean forceLocal)
			throws NoSuchAlgorithmException, IOException {
		if (forceLocal)
			return;
		IOException err = null;
		UrlWriteObject writeObject = null;
		try {
			writeObject = new UrlWriteObject(getCommandUrl("index"), documents);
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
		if (forceLocal)
			return;
		UrlRead urlRead = null;
		try {
			String url = getCommandUrl("delete").toExternalForm();
			url += "&uniq=" + URLEncoder.encode(uniqueField, "UTF-8");
			urlRead = new UrlRead(url);
			if (urlRead.getResponseCode() != 200)
				throw new IOException(url + " returns "
						+ urlRead.getResponseMessage() + "("
						+ urlRead.getResponseCode() + ")");
		} catch (IOException e) {
			throw e;
		} finally {
			if (urlRead != null)
				urlRead.close();
		}
	}

	public static WriterRemote fromConfig(IndexConfig indexConfig)
			throws MalformedURLException {
		if (indexConfig.getName() == null)
			return null;
		if (indexConfig.getRemoteUrl() == null)
			return null;
		Remote remote = new Remote(indexConfig.getName(), indexConfig
				.getRemoteUrl());
		if (remote == null)
			return null;
		return new WriterRemote(remote, indexConfig.getName(), indexConfig
				.getKeyField(), indexConfig.getKeyMd5RegExp());
	}

}
