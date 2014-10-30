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

package com.jaeksoft.searchlib.webservice.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.parser.ParserFactory;
import com.jaeksoft.searchlib.parser.ParserType;
import com.jaeksoft.searchlib.parser.ParserTypeEnum;
import com.jaeksoft.searchlib.util.LinkUtils;
import com.jaeksoft.searchlib.webservice.CommonListResult;
import com.jaeksoft.searchlib.webservice.CommonResult;
import com.jaeksoft.searchlib.webservice.CommonServices;
import com.jaeksoft.searchlib.webservice.NameLinkItem;

public class ParserImpl extends CommonServices implements RestParser {

	@Override
	public CommonListResult<NameLinkItem> list(UriInfo uriInfo, String login,
			String key) {
		try {
			getLoggedUser(login, key);
			ClientFactory.INSTANCE.properties.checkApi();
			List<ParserType> parserTypeList = ParserTypeEnum.INSTANCE.getList();
			ArrayList<NameLinkItem> items = new ArrayList<NameLinkItem>(
					parserTypeList.size());
			for (ParserType parserType : parserTypeList) {
				String name = parserType.getName();
				String link = LinkUtils.concatPath(uriInfo.getRequestUri()
						.getPath(), parserType.simpleName);
				items.add(new NameLinkItem(name, link));
			}
			return new CommonListResult<NameLinkItem>(items);
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	public ParserItemResult get(UriInfo uriInfo, String login, String key,
			String parser_name) {
		try {
			getLoggedUser(login, key);
			ClientFactory.INSTANCE.properties.checkApi();
			ParserType parserType = ParserTypeEnum.INSTANCE
					.findByName(parser_name);
			if (parserType == null)
				throw new CommonServiceException(Status.NOT_FOUND,
						"Parser not found: " + parser_name);
			ParserFactory parserFactory = ParserFactory.create(null, null,
					parserType.getParserClass().getCanonicalName());
			return new ParserItemResult(parserType, parserFactory);
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		} catch (ClassNotFoundException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	public CommonResult put(UriInfo uriInfo, String login, String key,
			String parser_name, LanguageEnum language, String params,
			InputStream inputStream) {
		// TODO Auto-generated method stub
		return null;
	}

}
