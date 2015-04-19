/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.webservice.query.morelikethis;

import javax.jws.WebParam;
import javax.jws.WebService;

import com.jaeksoft.searchlib.webservice.CommonResult;
import com.jaeksoft.searchlib.webservice.query.QueryTemplateResultList;

@WebService(name = "MoreLikeThis")
public interface SoapMoreLikeThis {

	public QueryTemplateResultList moreLikeThisTemplateList(
			@WebParam(name = "index") String index,
			@WebParam(name = "login") String login,
			@WebParam(name = "key") String key);

	public MoreLikeThisTemplateResult moreLikeThisTemplateGet(
			@WebParam(name = "index") String index,
			@WebParam(name = "login") String login,
			@WebParam(name = "key") String key,
			@WebParam(name = "template") String template);

	public CommonResult moreLikeThisTemplateDelete(
			@WebParam(name = "index") String index,
			@WebParam(name = "login") String login,
			@WebParam(name = "key") String key,
			@WebParam(name = "template") String template);

	public MoreLikeThisResult moreLikeThisTemplate(
			@WebParam(name = "index") String index,
			@WebParam(name = "login") String login,
			@WebParam(name = "key") String key,
			@WebParam(name = "template") String template,
			@WebParam(name = "query") MoreLikeThisQuery query);

	public CommonResult moreLikeThisTemplateSet(
			@WebParam(name = "index") String index,
			@WebParam(name = "login") String login,
			@WebParam(name = "key") String key,
			@WebParam(name = "template") String template,
			@WebParam(name = "query") MoreLikeThisQuery query);

	public MoreLikeThisResult moreLikeThis(
			@WebParam(name = "index") String index,
			@WebParam(name = "login") String login,
			@WebParam(name = "key") String key,
			@WebParam(name = "query") MoreLikeThisQuery query);

}
