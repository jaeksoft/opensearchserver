/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.web.controller.update;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.transform.stream.StreamSource;

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.web.controller.AlertController;
import com.jaeksoft.searchlib.web.controller.ScopeAttribute;

public class UploadTxtController extends AbstractUploadController {

	public class UpdateTxtThread extends AbstractUpdateThread {

		private final String capturePattern;

		private final List<String> fields;

		private final int langPosition;

		private final int bufferSize;

		private UpdateTxtThread(Client client, StreamSource streamSource,
				String capturePattern, List<String> fields, int langPosition,
				int bufferSize, String mediaName) {
			super(client, streamSource, mediaName);
			this.fields = new ArrayList<String>(fields);
			this.langPosition = langPosition;
			this.capturePattern = capturePattern;
			this.bufferSize = bufferSize;
		}

		@Override
		public int doUpdate() throws SearchLibException, IOException {
			try {
				return client.updateTextDocuments(streamSource,
						this.bufferSize, this.capturePattern,
						this.langPosition, this.fields, this);
			} catch (NoSuchAlgorithmException e) {
				throw new SearchLibException(e);
			} catch (URISyntaxException e) {
				throw new SearchLibException(e);
			} catch (InstantiationException e) {
				throw new SearchLibException(e);
			} catch (IllegalAccessException e) {
				throw new SearchLibException(e);
			} catch (ClassNotFoundException e) {
				throw new SearchLibException(e);
			}
		}
	}

	private String pattern;

	private int langPosition;

	private final List<String> fieldList = new ArrayList<String>(0);

	private String field;

	private int bufferSize;

	public UploadTxtController() throws SearchLibException {
		super(ScopeAttribute.UPDATE_TXT_MAP);
	}

	@Override
	protected void reset() {
		pattern = null;
		if (fieldList != null)
			fieldList.clear();
		langPosition = 0;
		field = null;
		bufferSize = 100;
	}

	@Override
	protected AbstractUpdateThread newUpdateThread(Client client,
			StreamSource streamSource, String mediaName) {
		return new UpdateTxtThread(client, streamSource, pattern, fieldList,
				langPosition, bufferSize, mediaName);
	}

	/**
	 * @return the pattern
	 */
	public String getPattern() {
		return pattern;
	}

	/**
	 * @param pattern
	 *            the pattern to set
	 */
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	@Command
	@NotifyChange("*")
	public void onCheckPattern() throws InterruptedException {
		Pattern.compile(pattern);
		new AlertController("Regular expression rocks!");
	}

	/**
	 * @return the langPosition
	 */
	public int getLangPosition() {
		return langPosition;
	}

	/**
	 * @param langPosition
	 *            the langPosition to set
	 */
	public void setLangPosition(int langPosition) {
		this.langPosition = langPosition;
	}

	public List<String> getFieldList() {
		return fieldList;
	}

	@Command
	@NotifyChange("*")
	public void onAddField() {
		if (field == null)
			return;
		fieldList.add(field);
	}

	@Command
	@NotifyChange("*")
	public void onRemoveField(@BindingParam("field") String field) {
		fieldList.remove(field);
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	/**
	 * @return the bufferSize
	 */
	public int getBufferSize() {
		return bufferSize;
	}

	/**
	 * @param bufferSize
	 *            the bufferSize to set
	 */
	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}
}
