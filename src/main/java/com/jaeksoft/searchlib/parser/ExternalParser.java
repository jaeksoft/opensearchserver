/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013-2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.parser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.transform.TransformerConfigurationException;

import org.apache.commons.exec.ExecuteException;
import org.apache.commons.lang.SystemUtils;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.streamlimiter.StreamLimiter;
import com.jaeksoft.searchlib.streamlimiter.StreamLimiterFile;
import com.jaeksoft.searchlib.util.ExecuteUtils;
import com.jaeksoft.searchlib.util.IOUtils;
import com.jaeksoft.searchlib.util.JsonUtils;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.jaeksoft.searchlib.web.StartStopListener;
import com.jaeksoft.searchlib.webservice.document.DocumentUpdate;

public class ExternalParser {

	private final static String FILE_PARSER_COMMAND = "parserCommand.json";
	private final static String FILE_PARSER_CONFIG = "parserConfig.xml";
	private final static String FILE_PARSER_RESULTS = "parserResults.xml";
	private final static String FILE_PARSER_ENCODING = "UTF-8";

	private final static String ADDITIONAL_CLASSPATH;

	static {
		String s = System.getProperty("oss.externalparser.classpath");
		if (!StringUtils.isEmpty(s)) {
			boolean bEndsWithStar = s.endsWith("*");
			File f = new File(bEndsWithStar ? s.substring(0, s.length() - 2)
					: s);
			ADDITIONAL_CLASSPATH = f.getAbsolutePath()
					+ (bEndsWithStar ? "/*" : "");
			if (!f.exists()) {
				System.err
						.println("WARNING: oss.externalparser.classpath does not exist: "
								+ ADDITIONAL_CLASSPATH);
			}
			System.out.println("oss.externalparser.classpath set to:"
					+ ADDITIONAL_CLASSPATH);
		} else
			ADDITIONAL_CLASSPATH = null;
	}

	@JsonInclude(Include.NON_EMPTY)
	final public static class Command {

		public final String opensearchserver_data_path;
		public final DocumentUpdate sourceDocument;
		public final String filePath;
		public final LanguageEnum lang;
		public final String originalURL;

		public Command() {
			opensearchserver_data_path = null;
			sourceDocument = null;
			filePath = null;
			lang = null;
			originalURL = null;
		}

		public Command(final IndexDocument sourceDocument,
				final StreamLimiter streamLimiter, final LanguageEnum lang)
				throws IOException, SearchLibException {
			this.opensearchserver_data_path = StartStopListener.OPENSEARCHSERVER_DATA_FILE
					.getAbsolutePath();
			this.sourceDocument = sourceDocument == null ? null
					: new DocumentUpdate(sourceDocument);
			File f = streamLimiter.getFile();
			this.filePath = f == null ? null : f.getAbsolutePath();
			this.lang = lang;
			this.originalURL = streamLimiter.getOriginURL();
		}
	}

	@JsonInclude(Include.NON_EMPTY)
	final public static class Result {

		public final DocumentUpdate parserDocument;
		public final DocumentUpdate directDocument;

		public Result() {
			parserDocument = null;
			directDocument = null;
		}

		public Result(DocumentUpdate parserDocument,
				DocumentUpdate directDocument) {
			this.parserDocument = parserDocument;
			this.directDocument = directDocument;
		}
	}

	@JsonInclude(Include.NON_EMPTY)
	final public static class Results {

		public final List<Result> results;
		public final List<String> links;
		public final String error;

		public Results() {
			results = null;
			links = null;
			error = null;
		}

		public Results(Throwable t) {
			this.error = t.getMessage();
			results = null;
			links = null;
		}

		public Results(List<ParserResultItem> resultItems,
				Set<String> detectedLinks, Throwable error) {
			this.results = resultItems != null ? new ArrayList<ExternalParser.Result>(
					resultItems.size()) : null;
			if (resultItems != null)
				for (ParserResultItem resultItem : resultItems)
					results.add(resultItem.getNewExternalResult());
			this.links = detectedLinks != null ? new ArrayList<String>(
					detectedLinks.size()) : null;
			if (detectedLinks != null)
				this.links.addAll(detectedLinks);
			this.error = error == null ? null : error.getMessage();
		}
	}

	public final static void doParserContent(Parser parser, File tempDir,
			final IndexDocument sourceDocument,
			final StreamLimiter streamLimiter, final LanguageEnum lang)
			throws IOException, SearchLibException,
			TransformerConfigurationException, SAXException {
		PrintWriter configWriter = null;
		ByteArrayOutputStream err = null;
		ByteArrayOutputStream out = null;
		try {
			// Prepare the files JSON and XML
			Command command = new Command(sourceDocument, streamLimiter, lang);
			File commandFile = new File(tempDir, FILE_PARSER_COMMAND);
			JsonUtils.jsonToFile(command, commandFile);
			File configFile = new File(tempDir, FILE_PARSER_CONFIG);
			configWriter = new PrintWriter(configFile, FILE_PARSER_ENCODING);
			XmlWriter xmlWriter = new XmlWriter(configWriter,
					FILE_PARSER_ENCODING);
			parser.writeXmlConfig(xmlWriter);
			xmlWriter.endDocument();
			configWriter.close();

			// Execute
			out = new ByteArrayOutputStream();
			err = new ByteArrayOutputStream();
			String classPath = ExecuteUtils.getClassPath();
			if (!StringUtils.isEmpty(ADDITIONAL_CLASSPATH))
				classPath = StringUtils.fastConcat(classPath,
						SystemUtils.PATH_SEPARATOR, ADDITIONAL_CLASSPATH);
			int statusCode = ExecuteUtils.command(tempDir, "java", classPath,
					true, out, err, 3600000L, ExternalParser.class.getName());
			if (statusCode != 0)
				throw new SearchLibException(err.toString("UTF-8"));
			File fileParserResults = new File(tempDir, FILE_PARSER_RESULTS);
			if (!fileParserResults.exists())
				return;
			Results results = JsonUtils.getObject(fileParserResults,
					Results.class);
			if (!StringUtils.isEmpty(results.error)) {
				Logging.warn("External parser error: " + err.toString());
				throw new SearchLibException.ExternalParserException(
						results.error);
			}
			parser.setExternalResults(results);
		} catch (ExecuteException e) {
			if (err != null)
				Logging.error(err.toString("UTF-8"));
			throw e;
		} finally {
			IOUtils.close(configWriter, err, out);
		}
	}

	public final static void main(String[] args) {
		Results results = null;
		try {
			File fileParserConfig = new File(FILE_PARSER_CONFIG);
			if (!fileParserConfig.exists())
				throw new FileNotFoundException(
						fileParserConfig.getAbsolutePath());
			XPathParser xpp = new XPathParser(fileParserConfig);
			Parser parser = (Parser) ParserFactory.create(null, xpp,
					xpp.getNode("/parser"));
			if (parser == null)
				throw new SearchLibException("Parser not found");
			File fileParserCommand = new File(FILE_PARSER_COMMAND);
			if (!fileParserCommand.exists())
				throw new FileNotFoundException(
						fileParserCommand.getAbsolutePath());
			Command command = JsonUtils.getObject(fileParserCommand,
					Command.class);
			if (command == null)
				throw new SearchLibException("Not parsing command found");
			StartStopListener.OPENSEARCHSERVER_DATA_FILE = new File(
					command.opensearchserver_data_path);
			IndexDocument indexSourceDocument = command.sourceDocument == null ? null
					: DocumentUpdate.getIndexDocument(command.sourceDocument);
			StreamLimiterFile streamLimiterFile = new StreamLimiterFile(
					parser.getSizeLimit(), new File(command.filePath),
					command.originalURL);
			parser.doParserContent(indexSourceDocument, streamLimiterFile,
					command.lang);
			results = parser.getExternalResults();
		} catch (Exception e) {
			results = new Results(e);
		} finally {
			File fileParserResults = new File(FILE_PARSER_RESULTS);
			if (fileParserResults.exists())
				fileParserResults.delete();
			try {
				JsonUtils.jsonToFile(results, fileParserResults);
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		System.exit(0);
	}
}
