/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.web.spider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.io.IOUtils;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.web.database.CredentialItem;
import com.jaeksoft.searchlib.process.ThreadAbstract;
import com.jaeksoft.searchlib.util.ReadWriteLock;

public class HttpDownloadThread extends ThreadAbstract<HttpDownloadThread> {

	private final ReadWriteLock rwl = new ReadWriteLock();

	private URI uri;
	private File destFile;
	private HttpDownloader httpDownloader;
	private CredentialItem credentialItem;
	private long contentSize;
	private long contentTransfered;
	private boolean downloadSuccess;

	public HttpDownloadThread(Config config, URI uri, File destFile)
			throws SearchLibException, MalformedURLException,
			URISyntaxException {
		super(config, null, null);
		this.uri = uri;
		contentSize = 0;
		contentTransfered = 0;
		this.destFile = destFile;
		httpDownloader = config.getWebCrawlMaster().getNewHttpDownloader();
		credentialItem = config.getWebCredentialManager().getCredential(
				uri.toString());
		downloadSuccess = false;
	}

	@Override
	public void runner() throws Exception {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(destFile);
			DownloadItem downloadItem = httpDownloader.get(uri, credentialItem);
			setContentSize(httpDownloader.getContentLength());
			InputStream is = downloadItem.getContentInputStream();
			byte buf[] = new byte[65536];
			int l;
			while ((l = is.read(buf)) != -1) {
				fos.write(buf, 0, l);
				incContentTransfered(l);
			}
			fos.flush();
			IOUtils.closeQuietly(fos);
			fos = null;
			setDownloadSuccess(true);
		} finally {
			if (fos != null)
				IOUtils.closeQuietly(fos);
		}

	}

	private void setDownloadSuccess(boolean b) {
		rwl.w.lock();
		try {
			downloadSuccess = b;
		} finally {
			rwl.w.unlock();
		}
	}

	public boolean isDownloadSuccess() {
		rwl.r.lock();
		try {
			return downloadSuccess;
		} finally {
			rwl.r.unlock();
		}
	}

	private void setContentSize(long s) {
		rwl.w.lock();
		try {
			contentSize = s;
		} finally {
			rwl.w.unlock();
		}
	}

	private void incContentTransfered(long s) {
		rwl.w.lock();
		try {
			contentTransfered += s;
		} finally {
			rwl.w.unlock();
		}
	}

	public int getPercent() {
		rwl.r.lock();
		try {
			if (contentSize == 0)
				return 0;
			float v = (((float) contentTransfered / (float) contentSize) * 100);
			return (int) v;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public void release() {
		if (httpDownloader != null)
			httpDownloader.release();
	}
}
