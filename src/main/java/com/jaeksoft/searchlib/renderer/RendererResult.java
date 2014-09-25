/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013-2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.renderer;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.ocr.HocrPdf;
import com.jaeksoft.searchlib.renderer.plugin.AuthPluginInterface;
import com.jaeksoft.searchlib.result.ResultDocument;
import com.jaeksoft.searchlib.schema.FieldValueItem;

public class RendererResult {

	public class Item {

		final private String url;

		final private HocrPdf hocrPdf;

		private Item(String url, HocrPdf hocrPdf) {
			this.url = url;
			this.hocrPdf = hocrPdf;
		}

		public String getUrl() {
			return url;
		}

		public HocrPdf getHocrPdf() {
			return hocrPdf;
		}

	}

	private final long creationTime;

	final private Client client;
	final private StringBuilder sbUrl;
	final private String keywords;
	private List<Item> items;
	final private String contentTypeField;
	private String filenameField;
	private String hocrField;
	final private AuthPluginInterface.User loggedUser;
	final private String authDomain;
	final private String authUsername;
	final private String authPassword;

	public RendererResult(Client client, Renderer renderer, String keywords,
			AuthPluginInterface.User loggedUser) {
		this.client = client;
		this.keywords = keywords;
		this.contentTypeField = renderer.getContentTypeField();
		this.filenameField = renderer.getFilenameField();
		this.hocrField = renderer.getHocrField();
		this.loggedUser = loggedUser;
		this.authDomain = renderer.getAuthDomain();
		this.authUsername = renderer.getAuthUsername();
		this.authPassword = renderer.getAuthPassword();
		sbUrl = new StringBuilder("viewer.zul?h=");
		sbUrl.append(hashCode());
		sbUrl.append("&p=");
		items = new ArrayList<Item>(0);
		creationTime = System.currentTimeMillis();
	}

	final private String addItemGetUrl(String url, HocrPdf hocrPdf) {
		int pos = items.size();
		items.add(new Item(url, hocrPdf));
		return sbUrl.toString() + pos;
	}

	final public String getViewerUrl(ResultDocument resultDocument, String url)
			throws SearchLibException {

		if (url == null)
			return null;
		HocrPdf hocrPdf = null;
		if (hocrField != null) {
			List<FieldValueItem> fieldValueItem = resultDocument
					.getValues(hocrField);
			if (fieldValueItem != null)
				hocrPdf = new HocrPdf(fieldValueItem);
		}

		if (contentTypeField != null) {
			String ct = resultDocument.getValueContent(contentTypeField, 0);
			if ("application/pdf".equalsIgnoreCase(ct))
				return addItemGetUrl(url, hocrPdf);
		} else if (filenameField != null) {
			String fn = resultDocument.getValueContent(filenameField, 0);
			if ("pdf".equalsIgnoreCase(FilenameUtils.getExtension(fn)))
				return addItemGetUrl(url, hocrPdf);
		}
		return null;
	}

	final public String getOpenFolderUrl(ResultDocument resultDocument,
			String url) {
		if (filenameField == null || url == null)
			return null;
		String fn = resultDocument.getValueContent(filenameField, 0);
		if (fn == null)
			return null;
		if (!url.startsWith(("file:/")))
			return null;
		if (url.endsWith("/"))
			return null;
		int i = url.lastIndexOf('/');
		if (i == -1)
			return null;
		return url.substring(0, i + 1);
	}

	final public Client getClient() {
		return client;
	}

	final public String getKeywords() {
		return keywords;
	}

	final public Item getItem(int pos) {
		return items.get(pos);
	}

	final public long getCreationTime() {
		return creationTime;
	}

	/**
	 * @return the loggedUser
	 */
	public AuthPluginInterface.User getLoggedUser() {
		return loggedUser;
	}

	/**
	 * @return the authDomain
	 */
	public String getAuthDomain() {
		return authDomain;
	}

	/**
	 * @return the authUsername
	 */
	public String getAuthUsername() {
		return authUsername;
	}

	/**
	 * @return the authPassword
	 */
	public String getAuthPassword() {
		return authPassword;
	}

	public boolean isAuthCredential() {
		return !StringUtils.isEmpty(authDomain)
				&& !StringUtils.isEmpty(authUsername);
	}

}
