/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/
package com.jaeksoft.searchlib.web.controller.crawler;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.web.controller.CommonController;

public class CrawlerController extends CommonController {

	public CrawlerController() throws SearchLibException {
		super();
	}

	private static final long serialVersionUID = 2308774605231174474L;

	public boolean isWebCrawlerRights() throws SearchLibException {
		if (!isLogged() || !isInstanceValid())
			return false;
		if (isNoUserList())
			return true;
		return getLoggedUser().hasAnyRole(getIndexName(),
				Role.GROUP_WEB_CRAWLER);
	}

	public boolean isWebCrawlerParametersRights() throws SearchLibException {
		if (!isLogged() || !isInstanceValid())
			return false;
		if (isNoUserList())
			return true;
		return getLoggedUser().hasAnyRole(getIndexName(),
				Role.WEB_CRAWLER_EDIT_PARAMETERS);
	}

	public boolean isWebCrawlerEditPatternsRights() throws SearchLibException {
		if (!isLogged() || !isInstanceValid())
			return false;
		if (isNoUserList())
			return true;
		return getLoggedUser().hasAnyRole(getIndexName(),
				Role.WEB_CRAWLER_EDIT_PATTERN_LIST);
	}

	public boolean isWebCrawlerStartStopRights() throws SearchLibException {
		if (!isLogged() || !isInstanceValid())
			return false;
		if (isNoUserList())
			return true;
		return getLoggedUser().hasAnyRole(getIndexName(),
				Role.WEB_CRAWLER_START_STOP);
	}

	public boolean isFileCrawlerRights() throws SearchLibException {
		if (!isLogged() || !isInstanceValid())
			return false;
		if (isNoUserList())
			return true;
		return getLoggedUser().hasAnyRole(getIndexName(),
				Role.GROUP_FILE_CRAWLER);
	}

	public boolean isFileCrawlerParametersRights() throws SearchLibException {
		if (!isLogged() || !isInstanceValid())
			return false;
		if (isNoUserList())
			return true;
		return getLoggedUser().hasAnyRole(getIndexName(),
				Role.FILE_CRAWLER_EDIT_PARAMETERS);
	}

	public boolean isFileCrawlerEditPatternsRights() throws SearchLibException {
		if (!isLogged() || !isInstanceValid())
			return false;
		if (isNoUserList())
			return true;
		return getLoggedUser().hasAnyRole(getIndexName(),
				Role.FILE_CRAWLER_EDIT_PATTERN_LIST);
	}

	public boolean isFileCrawlerStartStopRights() throws SearchLibException {
		if (!isLogged() || !isInstanceValid())
			return false;
		if (isNoUserList())
			return true;
		return getLoggedUser().hasAnyRole(getIndexName(),
				Role.FILE_CRAWLER_START_STOP);
	}

	@Override
	public void reset() {

	}

}
