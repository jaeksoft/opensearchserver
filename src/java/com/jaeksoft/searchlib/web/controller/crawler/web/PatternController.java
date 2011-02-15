/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2011 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer.  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.web.controller.crawler.web;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.common.database.PropertyItem;
import com.jaeksoft.searchlib.crawler.web.database.PatternManager;

public class PatternController extends AbstractPatternController {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4379405145841183453L;

	public PatternController() throws SearchLibException {
		super();
	}

	@Override
	protected PatternManager getPatternManager() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		return client.getInclusionPatternManager();
	}

	@Override
	protected boolean isInclusion() {
		return true;
	}

	@Override
	public PropertyItem<Boolean> getEnabled() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		return client.getWebPropertyManager().getInclusionEnabled();
	}

}