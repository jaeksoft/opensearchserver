/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.util.video;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.simple.JSONObject;

public class DailymotionItem {

	private final String title;
	private final String videoId;
	private final String thumbnail;

	public DailymotionItem(InputStream dailymotionResponse, String videoId,
			String thumbnail) throws JSONException, IOException {
		String jsonText = IOUtils.toString(dailymotionResponse);
		org.json.JSONObject jsonObject = new org.json.JSONObject(jsonText);
		title = jsonObject.getString("title");
		this.videoId = videoId;
		this.thumbnail = thumbnail;
	}

	final public String getTitle() {
		return title;
	}

	public String getVideoId() {
		return videoId;
	}

	public String getThumbnail() {
		return thumbnail;
	}

	@Override
	final public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Title: ");
		sb.append(title);
		sb.append(" - VideoId: ");
		sb.append(videoId);
		sb.append(" - thumbnail: ");
		sb.append(thumbnail);
		return sb.toString();
	}

	@SuppressWarnings("unchecked")
	final public String toJson(URL url) {
		JSONObject json = new JSONObject();
		json.put("url", url.toExternalForm());
		json.put("title", title);
		json.put("videoId", videoId);
		json.put("thumbnail", thumbnail);
		return json.toJSONString();
	}

}
