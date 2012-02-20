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

package com.jaeksoft.searchlib.result;

import javax.servlet.http.HttpServletRequest;

import com.jaeksoft.searchlib.index.ReaderLocal;
import com.jaeksoft.searchlib.render.Render;
import com.jaeksoft.searchlib.request.SpellCheckRequest;

public class ResultMoreLikeThis extends AbstractResult<SpellCheckRequest> {

	protected ResultMoreLikeThis(ReaderLocal reader, SpellCheckRequest request) {
		super(request);
	}

	@Override
	public Render getRender(HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

}
