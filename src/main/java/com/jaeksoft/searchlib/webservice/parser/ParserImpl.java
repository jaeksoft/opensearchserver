/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2014-2015 Emmanuel Keller / Jaeksoft
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicException;
import net.sf.jmimemagic.MagicMatch;
import net.sf.jmimemagic.MagicMatchNotFoundException;
import net.sf.jmimemagic.MagicParseException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.parser.Parser;
import com.jaeksoft.searchlib.parser.ParserFactory;
import com.jaeksoft.searchlib.parser.ParserResultItem;
import com.jaeksoft.searchlib.parser.ParserType;
import com.jaeksoft.searchlib.parser.ParserTypeEnum;
import com.jaeksoft.searchlib.streamlimiter.StreamLimiter;
import com.jaeksoft.searchlib.streamlimiter.StreamLimiterFile;
import com.jaeksoft.searchlib.streamlimiter.StreamLimiterInputStream;
import com.jaeksoft.searchlib.util.IOUtils;
import com.jaeksoft.searchlib.util.LinkUtils;
import com.jaeksoft.searchlib.webservice.CommonListResult;
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

	private ParserType checkParserType(String parserName) {
		ParserType parserType = ParserTypeEnum.INSTANCE.findByName(parserName);
		if (parserType == null)
			throw new CommonServiceException(Status.NOT_FOUND,
					"Parser not found: " + parserName);
		return parserType;
	}

	private ParserFactory checkParserFactory(ParserType parserType)
			throws ClassNotFoundException, SearchLibException {
		return ParserFactory.create(null, null, parserType.getParserClass()
				.getCanonicalName());
	}

	@Override
	public ParserItemResult get(UriInfo uriInfo, String login, String key,
			String parserName) {
		try {
			getLoggedUser(login, key);
			ClientFactory.INSTANCE.properties.checkApi();
			ParserType parserType = checkParserType(parserName);
			return new ParserItemResult(parserType,
					checkParserFactory(parserType));
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

	private StreamLimiter getStreamLimiter(String path, InputStream inputStream)
			throws IOException {
		if (StringUtils.isEmpty(path) && inputStream == null)
			throw new CommonServiceException(Status.NOT_ACCEPTABLE,
					"You should either provide a path or upload a file");
		if (StringUtils.isEmpty(path))
			return new StreamLimiterInputStream(0, inputStream, null, null);
		return new StreamLimiterFile(0, new File(path));
	}

	private void setParserParams(UriInfo uriInfo, ParserFactory parserFactory)
			throws SearchLibException {
		if (parserFactory == null)
			throw new CommonServiceException(Status.NOT_ACCEPTABLE,
					"No parser found");
		MultivaluedMap<String, String> parserParams = uriInfo
				.getQueryParameters();
		for (String propKey : parserParams.keySet()) {
			if (!propKey.startsWith("p."))
				continue;
			parserFactory.setUserProperty(propKey.substring(2),
					parserParams.getFirst(propKey));
		}
	}

	@Override
	public ParserDocumentsResult put(UriInfo uriInfo, String login, String key,
			String parserName, LanguageEnum language, String path,
			InputStream inputStream) {
		StreamLimiter streamLimiter = null;
		try {
			getLoggedUser(login, key);
			ClientFactory.INSTANCE.properties.checkApi();
			ParserType parserType = checkParserType(parserName);
			ParserFactory parserFactory = checkParserFactory(parserType);
			setParserParams(uriInfo, parserFactory);
			streamLimiter = getStreamLimiter(path, inputStream);
			Parser parser = (Parser) ParserFactory.create(parserFactory);
			parser.doParserContent(null, null, streamLimiter, language);
			List<ParserResultItem> parserResultList = parser.getParserResults();
			return new ParserDocumentsResult(null, null, parserResultList);
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		} catch (ClassNotFoundException e) {
			throw new CommonServiceException(e);
		} finally {
			IOUtils.close(streamLimiter);
		}
	}

	@Override
	public ParserDocumentsResult putMagic(UriInfo uriInfo, String login,
			String key, LanguageEnum language, String fileName,
			String mimeType, String path, InputStream inputStream) {
		StreamLimiter streamLimiter = null;
		try {
			getLoggedUser(login, key);
			ClientFactory.INSTANCE.properties.checkApi();
			streamLimiter = getStreamLimiter(path, inputStream);

			// Find parser from extension
			ParserType parserTypeFromExtension = null;
			String extension = null;
			if (!StringUtils.isEmpty(fileName))
				extension = FilenameUtils.getExtension(fileName);
			else if (!StringUtils.isEmpty(path))
				extension = FilenameUtils.getExtension(path);
			if (extension != null)
				parserTypeFromExtension = ParserTypeEnum.INSTANCE
						.findByExtensionFirst(extension);

			// Find a parser from the mime type
			ParserType parserTypeFromMime = null;
			MagicMatch match = null;
			if (mimeType == null) {
				match = Magic
						.getMagicMatch(streamLimiter.getFile(), true, true);
				if (match != null)
					mimeType = match.getMimeType();
			}
			if (mimeType != null)
				parserTypeFromMime = ParserTypeEnum.INSTANCE
						.findByMimeTypeFirst(mimeType);

			// Choose a parser
			ParserType parserType = parserTypeFromExtension;
			if (parserType == null)
				parserType = parserTypeFromMime;

			if (parserType == null)
				throw new CommonServiceException(Status.NOT_ACCEPTABLE,
						"Unable to find a parser");

			// Do the extraction
			ParserFactory parserFactory = checkParserFactory(parserType);
			setParserParams(uriInfo, parserFactory);
			Parser parser = (Parser) ParserFactory.create(parserFactory);
			parser.doParserContent(null, null, streamLimiter, language);
			List<ParserResultItem> parserResultList = parser.getParserResults();
			return new ParserDocumentsResult(mimeType, parserType.simpleName,
					parserResultList);
		} catch (MagicParseException e) {
			throw new CommonServiceException(e);
		} catch (MagicMatchNotFoundException e) {
			throw new CommonServiceException(e);
		} catch (MagicException e) {
			throw new CommonServiceException(e);
		} catch (SearchLibException e) {
			throw new CommonServiceException(e);
		} catch (InterruptedException e) {
			throw new CommonServiceException(e);
		} catch (IOException e) {
			throw new CommonServiceException(e);
		} catch (ClassNotFoundException e) {
			throw new CommonServiceException(e);
		} finally {
			IOUtils.close(streamLimiter);
		}
	}
}
