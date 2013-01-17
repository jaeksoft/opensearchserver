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

package com.jaeksoft.searchlib.renderer;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.ocr.HocrPdf;
import com.jaeksoft.searchlib.result.ResultDocument;

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

	private Client client;
	private StringBuffer sbUrl;
	private String keywords;
	private List<Item> items;
	private String contentTypeField;
	private String filenameField;
	private String hocrField;

	public RendererResult(Client client, String serverBaseUrl,
			Renderer renderer, String keywords) {
		this.client = client;
		this.keywords = keywords;
		this.contentTypeField = renderer.getContentTypeField();
		this.filenameField = renderer.getFilenameField();
		this.hocrField = renderer.getHocrField();
		sbUrl = new StringBuffer(serverBaseUrl);
		sbUrl.append("/viewer.zul?h=");
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
		HocrPdf hocrPdf = hocrField == null ? null : new HocrPdf(
				resultDocument.getValueArray(hocrField));

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
}
