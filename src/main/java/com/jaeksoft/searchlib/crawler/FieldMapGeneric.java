/**
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2010-2014 Emmanuel Keller / Jaeksoft
 * <p>
 * http://www.open-search-server.com
 * <p>
 * This file is part of OpenSearchServer.
 * <p>
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.
 * If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.crawler;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.common.database.CommonFieldTarget;
import com.jaeksoft.searchlib.crawler.file.database.FilePathItem;
import com.jaeksoft.searchlib.crawler.file.database.FileTypeEnum;
import com.jaeksoft.searchlib.crawler.file.process.FileInstanceAbstract;
import com.jaeksoft.searchlib.crawler.web.database.HostUrlList.ListType;
import com.jaeksoft.searchlib.crawler.web.process.WebCrawlThread;
import com.jaeksoft.searchlib.crawler.web.spider.Crawl;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.FieldContent;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.parser.Parser;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.schema.FieldValueOriginEnum;
import com.jaeksoft.searchlib.util.*;
import com.jaeksoft.searchlib.util.map.GenericLink;
import com.jaeksoft.searchlib.util.map.GenericMap;
import com.jaeksoft.searchlib.util.map.SourceField;
import com.jaeksoft.searchlib.util.map.TargetField;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import net.minidev.json.JSONArray;
import org.apache.commons.lang3.StringEscapeUtils;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

public abstract class FieldMapGeneric<S extends SourceField, T extends TargetField> extends GenericMap<S, T> {

	private File mapFile;

	protected FieldMapGeneric() {
		mapFile = null;
	}

	protected FieldMapGeneric(Node parentNode) throws XPathExpressionException {
		mapFile = null;
		load(parentNode);
	}

	protected FieldMapGeneric(File mapFile, String rootXPath)
			throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
		this.mapFile = mapFile;
		if (!mapFile.exists())
			return;
		XPathParser xpp = new XPathParser(mapFile);
		load(xpp.getNode(rootXPath));
	}

	protected abstract T loadTarget(String targetName, Node node);

	protected abstract S loadSource(String source);

	public void load(Node parentNode) throws XPathExpressionException {
		synchronized (this) {
			if (parentNode == null)
				return;
			List<Node> nodeList = DomUtils.getNodes(parentNode, "link");
			for (Node node : nodeList) {
				String sourceName = StringEscapeUtils.unescapeXml(DomUtils.getAttributeText(node, "source"));
				S source = loadSource(sourceName);
				if (source == null)
					continue;
				String targetName = DomUtils.getAttributeText(node, "target");
				T target = loadTarget(targetName, node);
				if (target == null)
					continue;
				add(source, target);
			}
		}
	}

	protected abstract void writeTarget(XmlWriter xmlWriter, T target) throws SAXException;

	public void store(XmlWriter xmlWriter) throws SAXException {
		for (GenericLink<S, T> link : getList()) {
			T target = link.getTarget();
			xmlWriter.startElement("link", "source", link.getSource().toXmlAttribute(), "target",
					target.toXmlAttribute(), "analyzer", target.getAnalyzer(), "boost",
					target.getBoost() == null ? null : Float.toString(target.getBoost()));
			writeTarget(xmlWriter, link.getTarget());
			xmlWriter.endElement();
		}
	}

	public void store() throws TransformerConfigurationException, SAXException, IOException {
		synchronized (this) {
			if (!mapFile.exists())
				mapFile.createNewFile();
			PrintWriter pw = new PrintWriter(mapFile);
			try {
				XmlWriter xmlWriter = new XmlWriter(pw, "UTF-8");
				xmlWriter.startElement("map");
				store(xmlWriter);
				xmlWriter.endElement();
				xmlWriter.endDocument();
			} finally {
				pw.close();
			}
		}
	}

	final protected void mapFieldTarget(FieldMapContext context, FieldContent fc, CommonFieldTarget targetField,
			IndexDocument target, Set<String> filePathSet)
			throws IOException, SearchLibException, ParseException, SyntaxError, URISyntaxException,
			ClassNotFoundException, InterruptedException, InstantiationException, IllegalAccessException {
		if (fc == null)
			return;
		for (FieldValueItem fvi : fc.getValues())
			mapFieldTarget(context, targetField, fvi.value, target, filePathSet);
	}

	final public String mapFieldTarget(CommonFieldTarget dfTarget, String content) {
		if (StringUtils.isEmpty(content))
			return null;
		if (dfTarget.isConvertHtmlEntities())
			content = StringEscapeUtils.unescapeHtml4(content);
		if (dfTarget.isRemoveTag())
			content = StringUtils.removeTag(content);
		if (dfTarget.hasRegexpPattern())
			content = dfTarget.applyRegexPattern(content);
		return content;
	}

	final protected void mapFieldTarget(FieldMapContext context, CommonFieldTarget dfTarget, String content,
			IndexDocument target, Set<String> filePathSet)
			throws SearchLibException, IOException, ParseException, SyntaxError, URISyntaxException,
			ClassNotFoundException, InterruptedException, InstantiationException, IllegalAccessException {
		if (dfTarget == null)
			return;
		if (StringUtils.isEmpty(content))
			return;
		if (dfTarget.isFilePath()) {
			String filePath = dfTarget.getFilePath(content);
			if (filePathSet == null || !filePathSet.contains(filePath)) {
				if (filePathSet != null)
					filePathSet.add(filePath);
				File file = new File(filePath);
				if (file.exists()) {
					Parser parser =
							context.parserSelector.parseFile(null, file.getName(), null, null, file, context.lang);
					if (parser != null)
						parser.popupateResult(0, target);
				} else {
					Logging.error("File don't exist:" + file.getAbsolutePath());
				}
			}
		}
		if (dfTarget.isCrawlFile()) {
			String filePathName = dfTarget.getFilePathPrefix();
			if (filePathSet == null || !filePathSet.contains(content)) {
				if (filePathSet != null)
					filePathSet.add(content);
				URI filePathURI = new URI(filePathName);
				FilePathItem filePathItem =
						context.filePathManager.findFirst(filePathURI.getScheme(), filePathURI.getHost());
				if (filePathItem == null)
					throw new SearchLibException("FilePathItem not found: " + filePathName);
				FileInstanceAbstract fileInstance =
						FileInstanceAbstract.create(filePathItem, null, filePathItem.getPath() + content);
				FileTypeEnum type = fileInstance.getFileType();
				if (type != null && type == FileTypeEnum.file) {
					Parser parser = context.parserSelector.parseStream(null, fileInstance.getFileName(), null, null,
							fileInstance.getInputStream(), context.lang, null, null);
					if (parser != null)
						parser.popupateResult(0, target);
				}
			}
		}
		if (dfTarget.isCrawlUrl()) {
			WebCrawlThread crawlThread =
					context.webCrawlMaster.manualCrawl(LinkUtils.newEncodedURL(content), ListType.DBCRAWL);
			crawlThread.waitForStart(60);
			crawlThread.waitForEnd(60);
			Crawl crawl = crawlThread.getCurrentCrawl();
			if (crawl != null) {
				IndexDocument targetIndexDocument = crawl.getTargetIndexDocument(0);
				if (targetIndexDocument != null)
					target.add(targetIndexDocument);
			}
		}
		content = mapFieldTarget(dfTarget, content);
		target.add(dfTarget.getName(), new FieldValueItem(FieldValueOriginEnum.EXTERNAL, content));
	}

	public void mapJson(FieldMapContext context, Object jsonObject, IndexDocument target)
			throws SearchLibException, IOException, ParseException, SyntaxError, URISyntaxException,
			ClassNotFoundException, InterruptedException, InstantiationException, IllegalAccessException {
		for (GenericLink<S, T> link : getList()) {
			String jsonPath = link.getSource().getUniqueName();
			try {
				Object jsonContent = JsonPath.read(jsonObject, jsonPath);
				if (jsonContent == null)
					continue;
				if (jsonContent instanceof JSONArray) {
					JSONArray jsonArray = (JSONArray) jsonContent;
					for (Object content : jsonArray) {
						if (content != null)
							mapFieldTarget(context, (CommonFieldTarget) link.getTarget(), content.toString(), target,
									null);
					}
				} else
					mapFieldTarget(context, (CommonFieldTarget) link.getTarget(), jsonContent.toString(), target, null);
			} catch (PathNotFoundException e) {
				continue;
			} catch (IllegalArgumentException e) {
				Logging.warn(e);
				continue;
			}
		}
	}
}
