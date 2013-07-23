/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang.StringEscapeUtils;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.crawler.common.database.CommonFieldTarget;
import com.jaeksoft.searchlib.crawler.web.database.HostUrlList.ListType;
import com.jaeksoft.searchlib.crawler.web.process.WebCrawlMaster;
import com.jaeksoft.searchlib.crawler.web.process.WebCrawlThread;
import com.jaeksoft.searchlib.crawler.web.spider.Crawl;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.parser.Parser;
import com.jaeksoft.searchlib.parser.ParserSelector;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.schema.FieldValueOriginEnum;
import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.LinkUtils;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.jaeksoft.searchlib.util.map.GenericLink;
import com.jaeksoft.searchlib.util.map.GenericMap;
import com.jaeksoft.searchlib.util.map.SourceField;
import com.jaeksoft.searchlib.util.map.TargetField;

public abstract class FieldMapGeneric<S extends SourceField, T extends TargetField>
		extends GenericMap<S, T> {

	private File mapFile;

	protected FieldMapGeneric() {
		mapFile = null;
	}

	protected FieldMapGeneric(Node parentNode) throws XPathExpressionException {
		mapFile = null;
		load(parentNode);
	}

	protected FieldMapGeneric(File mapFile, String rootXPath)
			throws ParserConfigurationException, SAXException, IOException,
			XPathExpressionException {
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
				String sourceName = DomUtils.getAttributeText(node, "source");
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

	protected abstract void writeTarget(XmlWriter xmlWriter, T target)
			throws SAXException;

	public void store(XmlWriter xmlWriter) throws SAXException {
		for (GenericLink<S, T> link : getList()) {
			xmlWriter.startElement("link", "source", link.getSource()
					.toXmlAttribute(), "target", link.getTarget()
					.toXmlAttribute(), "analyzer", link.getTarget()
					.getAnalyzer());
			writeTarget(xmlWriter, link.getTarget());
			xmlWriter.endElement();
		}
	}

	public void store() throws TransformerConfigurationException, SAXException,
			IOException {
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

	final protected void mapFieldTarget(WebCrawlMaster webCrawlMaster,
			ParserSelector parserSelector, LanguageEnum lang,
			CommonFieldTarget dfTarget, String content, IndexDocument target)
			throws SearchLibException, IOException, ParseException,
			SyntaxError, URISyntaxException, ClassNotFoundException,
			InterruptedException, InstantiationException,
			IllegalAccessException {
		if (dfTarget == null)
			return;
		if (dfTarget.isFilePath()) {
			File file = new File(dfTarget.getFilePath(content));
			if (file.exists()) {
				Parser parser = parserSelector.parseFile(null, file.getName(),
						null, null, file, lang);
				if (parser != null)
					parser.popupateResult(0, target);
			} else {
				Logging.error("File don't exist:" + file.getAbsolutePath());
			}
		}
		if (dfTarget.isCrawlUrl()) {
			WebCrawlThread crawlThread = webCrawlMaster.manualCrawl(
					LinkUtils.newEncodedURL(content), ListType.DBCRAWL);
			crawlThread.waitForStart(60);
			crawlThread.waitForEnd(60);
			Crawl crawl = crawlThread.getCurrentCrawl();
			if (crawl != null) {
				IndexDocument targetIndexDocument = crawl
						.getTargetIndexDocument(0);
				if (targetIndexDocument != null)
					target.add(targetIndexDocument);
			}
		}
		if (dfTarget.isConvertHtmlEntities())
			content = StringEscapeUtils.unescapeHtml(content);
		if (dfTarget.isRemoveTag())
			content = StringUtils.removeTag(content);
		if (dfTarget.hasRegexpPattern())
			content = dfTarget.applyRegexPattern(content);
		target.add(dfTarget.getName(), new FieldValueItem(
				FieldValueOriginEnum.EXTERNAL, content));
	}
}
