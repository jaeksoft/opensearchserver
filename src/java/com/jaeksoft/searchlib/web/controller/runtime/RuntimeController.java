/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2010-2011 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.web.controller.runtime;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.web.controller.CommonController;

public class RuntimeController extends CommonController {

	private static final long serialVersionUID = -2867769891161290723L;

	public RuntimeController() throws SearchLibException {
		super();
	}

	@Override
	protected void reset() {
	}

	public boolean isCommandsRights() throws SearchLibException {
		return isAdminOrNoUser() && isInstanceValid();
	}

}
