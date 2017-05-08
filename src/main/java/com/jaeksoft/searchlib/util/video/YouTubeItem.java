/**
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
 * <p>
 * http://www.open-search-server.com
 * <p>
 * This file is part of OpenSearchServer.
 * <p>
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.
 * If not, see <http://www.gnu.org/licenses/>.
 **/
package com.jaeksoft.searchlib.util.video;

import com.jaeksoft.searchlib.util.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class YouTubeItem {

	private final String title;
	private final String description;
	private final String videoId;
	private final String thumbnail;

	public YouTubeItem(InputStream inputStream, String videoId, String thumbnail) throws IOException, JSONException {
		String jsonText = IOUtils.toString(inputStream, "UTF-8");
		JSONObject jsonRoot = new JSONObject(jsonText);
		JSONObject jsonEntry = jsonRoot.getJSONObject("entry");
		if (jsonEntry == null)
			throw new JSONException("No entry item");
		this.title = getText(jsonEntry, "title");
		this.description = getText(jsonEntry, "content");
		this.videoId = videoId;
		this.thumbnail = thumbnail;
	}

	private String getText(JSONObject json, String key) throws JSONException {
		json = json.getJSONObject(key);
		if (json == null)
			return null;
		return json.getString("$t");
	}

	final public String getTitle() {
		return title;
	}

	final public String getDescription() {
		return description;
	}

	public String getVideoId() {
		return videoId;
	}

	public String getThumbnail() {
		return thumbnail;
	}

	@Override
	final public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Title: ");
		sb.append(title);
		sb.append(" - Description: ");
		sb.append(description);
		sb.append(" - VideoId: ");
		sb.append(videoId);
		sb.append(" - thumbnail: ");
		sb.append(thumbnail);
		return sb.toString();
	}

	final public String toJson(URL url) throws JSONException {
		JSONObject json = new JSONObject();
		json.put("url", url.toExternalForm());
		json.put("title", title);
		json.put("description", description);
		json.put("videoId", videoId);
		json.put("thumbnail", thumbnail);
		return json.toString(0);
	}
}
