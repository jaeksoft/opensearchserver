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
import java.util.regex.Pattern;

import javax.xml.transform.stream.StreamSource;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.web.controller.ScopeAttribute;

public class UploadTxtController extends AbstractUploadController {

	public class UpdateTxtThread extends AbstractUpdateThread {

		private final Pattern pattern;

		private UpdateTxtThread(Client client, StreamSource streamSource,
				Pattern pattern, String mediaName) {
			super(client, streamSource, mediaName);
			this.pattern = pattern;
		}

		@Override
		public int doUpdate() throws SearchLibException, IOException {
			return 0;
		}
	}

	private Pattern compiledPattern;

	private String pattern;

	public UploadTxtController() throws SearchLibException {
		super(ScopeAttribute.UPDATE_TXT_MAP);
	}

	@Override
	protected void reset() {
		pattern = null;
		compiledPattern = null;
	}

	@Override
	protected AbstractUpdateThread newUpdateThread(Client client,
			StreamSource streamSource, String mediaName) {
		return new UpdateTxtThread(client, streamSource, compiledPattern,
				mediaName);
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
		compiledPattern = Pattern.compile(pattern);
	}
}
