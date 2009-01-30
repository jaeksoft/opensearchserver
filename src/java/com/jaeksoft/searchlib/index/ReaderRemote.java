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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashSet;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.Term;

import com.jaeksoft.searchlib.remote.Remote;
import com.jaeksoft.searchlib.remote.UrlReadObject;
import com.jaeksoft.searchlib.request.Request;
import com.jaeksoft.searchlib.result.DocumentResult;
import com.jaeksoft.searchlib.result.Result;

public class ReaderRemote extends NameFilter implements ReaderInterface {

	private Remote remote;

	private ReaderRemote(String indexName, Remote remote) {
		super(indexName);
		this.remote = remote;
	}

	public static ReaderRemote fromConfig(IndexConfig indexConfig)
			throws MalformedURLException {
		if (indexConfig.getName() == null)
			return null;
		if (indexConfig.getRemoteUrl() == null)
			return null;
		Remote remote = new Remote(indexConfig.getName(), indexConfig
				.getRemoteUrl());
		return new ReaderRemote(indexConfig.getName(), remote);
	}

	// TODO Implementation
	public void reload() throws IOException {
		throw new RuntimeException("Not yet implemented");
	}

	// TODO Propagate version information
	public void swap(long version, boolean deleteOld) throws IOException {
		String u = remote.getUrl("stats?reload="
				+ URLEncoder.encode(remote.getName(), "UTF-8") + "&forceLocal");
		if (deleteOld)
			u += "&deleteOld";
		HttpURLConnection huc = (HttpURLConnection) new URL(u).openConnection();
		huc.connect();
		huc.getResponseCode();
		huc.disconnect();
	}

	public void xmlInfo(PrintWriter writer, HashSet<String> classDetail) {
		// TODO Auto-generated method stub

	}

	private URL getRemoteSearchUrl(Request req)
			throws UnsupportedEncodingException, MalformedURLException {
		String baseUrl = remote.getUrl("/select?search="
				+ URLEncoder.encode(remote.getName(), "UTF-8")
				+ "&render=object&forceLocal");
		baseUrl += "&" + req.getUrlQueryString();
		return new URL(baseUrl);
	}

	public Result search(Request req) throws IOException {
		UrlReadObject urlReadObject = null;
		IOException err = null;
		Result res = null;
		try {
			urlReadObject = new UrlReadObject(getRemoteSearchUrl(req));
			if (urlReadObject.getResponseCode() != 200)
				throw new IOException(urlReadObject.getResponseMessage());
			res = (Result) urlReadObject.read();
			res.setRequest(req);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			res = null;
		} catch (IOException e) {
			e.printStackTrace();
			err = e;
		} finally {
			if (urlReadObject != null)
				urlReadObject.close();
			if (err != null)
				throw err;
		}
		return res;
	}

	private URL getRemoteDocumentUrl(Request req)
			throws UnsupportedEncodingException, MalformedURLException {
		String baseUrl = remote.getUrl("/document?search="
				+ URLEncoder.encode(remote.getName(), "UTF-8")
				+ "&render=object&forceLocal");
		baseUrl += "&" + req.getUrlQueryString();
		return new URL(baseUrl);
	}

	public DocumentResult documents(Request request)
			throws CorruptIndexException, IOException {
		UrlReadObject readObject = null;
		IOException err = null;
		DocumentResult documents = null;
		try {
			readObject = new UrlReadObject(getRemoteDocumentUrl(request));
			if (readObject.getResponseCode() != 200)
				throw new IOException(readObject.getResponseMessage());
			documents = (DocumentResult) readObject.read();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			documents = null;
		} catch (IOException e) {
			e.printStackTrace();
			err = e;
		} finally {
			if (readObject != null)
				readObject.close();
			if (err != null)
				throw err;
		}
		return documents;
	}

	/*
	 * public void getHits(ResultSearch result, int rows) throws IOException {
	 * Request req = result.getRequest().clone(); req.setRows(rows);
	 * ResultSearch rs = (ResultSearch) search(req);
	 * result.setDocSetHits(rs.getDocSetHits()); }
	 */

	public boolean sameIndex(ReaderInterface reader) {
		if (reader == this)
			return true;
		return reader.sameIndex(this);
	}

	// TODO
	public IndexStatistics getStatistics() {
		return null;
	}

	public int getDocFreq(Term term) throws IOException {
		// TODO Auto-generated method stub
		throw new RuntimeException("Not yet implemented");
	}

	public long getVersion() {
		// TODO Auto-generated method stub
		throw new RuntimeException("Not yet implemented");
	}

}
