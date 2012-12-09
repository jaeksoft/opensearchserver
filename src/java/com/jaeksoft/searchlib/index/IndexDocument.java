/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.index;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang.StringEscapeUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.crawler.web.database.CredentialItem;
import com.jaeksoft.searchlib.crawler.web.spider.DownloadItem;
import com.jaeksoft.searchlib.crawler.web.spider.HttpDownloader;
import com.jaeksoft.searchlib.crawler.web.spider.ProxyHandler;
import com.jaeksoft.searchlib.logreport.ErrorParserLogger;
import com.jaeksoft.searchlib.parser.Parser;
import com.jaeksoft.searchlib.parser.ParserSelector;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.schema.FieldValueOriginEnum;
import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.XPathParser;

public class IndexDocument implements Iterable<FieldContent> {

	private Map<String, FieldContent> fields;
	private LanguageEnum lang;
	private FieldContent[] fieldContentArray;

	public IndexDocument() {
		fields = new TreeMap<String, FieldContent>();
		this.lang = null;
		fieldContentArray = null;
	}

	public IndexDocument(IndexDocument sourceDocument) {
		this(sourceDocument.lang);
		for (Map.Entry<String, FieldContent> entry : sourceDocument.fields
				.entrySet())
			add(entry.getKey(), entry.getValue());
	}

	public IndexDocument(LanguageEnum lang) {
		this();
		this.lang = lang;
	}

	public IndexDocument(Locale lang) {
		this();
		if (lang != null)
			this.lang = LanguageEnum.findByCode(lang.getLanguage());
	}

	private final List<String> getCopyFieldList(Node fieldNode)
			throws XPathExpressionException {
		List<Node> copyNodes = DomUtils.getNodes(fieldNode, "copy");
		if (copyNodes == null || copyNodes.size() == 0)
			return null;
		List<String> copyList = new ArrayList<String>();
		for (Node copyNode : copyNodes) {
			String f = XPathParser.getAttributeString(copyNode, "field");
			if (f != null)
				copyList.add(f);
		}
		return copyList;
	}

	/**
	 * Create a new instance of IndexDocument from an XML structure <br/>
	 * <field name="FIELDNAME"><br/>
	 * &nbsp;&nbsp;<value>VALUE1</value><br/>
	 * &nbsp;&nbsp;<value>VALUE2</value><br/>
	 * </field>
	 * 
	 * @param xpp
	 * @param documentNode
	 * @throws XPathExpressionException
	 * @throws SearchLibException
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws DOMException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public IndexDocument(Client client, ParserSelector parserSelector,
			Node documentNode, CredentialItem urlDefaultCredential,
			ProxyHandler proxyHandler) throws XPathExpressionException,
			SearchLibException, InstantiationException, IllegalAccessException,
			ClassNotFoundException, IOException, URISyntaxException {
		this(LanguageEnum.findByCode(XPathParser.getAttributeString(
				documentNode, "lang")));
		List<Node> fieldNodes = DomUtils.getNodes(documentNode, "field");
		for (Node fieldNode : fieldNodes) {
			List<String> copyFieldList = getCopyFieldList(fieldNode);
			String fieldName = XPathParser
					.getAttributeString(fieldNode, "name");
			List<Node> valueNodes = DomUtils.getNodes(fieldNode, "value");
			for (Node valueNode : valueNodes) {
				boolean removeTag = "yes".equalsIgnoreCase(XPathParser
						.getAttributeString(valueNode, "removeTag"));
				boolean convertHtmlEntities = "yes"
						.equalsIgnoreCase(XPathParser.getAttributeString(
								valueNode, "convertHtmlEntities"));

				String textContent = valueNode.getTextContent();
				if (convertHtmlEntities)
					textContent = StringEscapeUtils.unescapeHtml(textContent);
				if (removeTag)
					textContent = StringUtils.removeTag(textContent);
				Float boost = XPathParser.getAttributeFloat(valueNode, "boost");
				add(fieldName, textContent, boost);
				if (copyFieldList != null)
					for (String f : copyFieldList)
						add(f, textContent, boost);
			}
		}
		List<Node> binaryNodes = DomUtils.getNodes(documentNode, "binary");
		for (Node node : binaryNodes) {
			boolean bFaultTolerant = "yes".equalsIgnoreCase(XPathParser
					.getAttributeString(node, "faultTolerant"));
			String filename = XPathParser.getAttributeString(node, "fileName");
			if (filename == null || filename.length() == 0)
				filename = XPathParser.getAttributeString(node, "filename");
			String filePath = XPathParser.getAttributeString(node, "filePath");
			if (filePath == null || filePath.length() == 0)
				filePath = XPathParser.getAttributeString(node, "filepath");
			String contentType = XPathParser.getAttributeString(node,
					"contentType");
			if (contentType == null || contentType.length() == 0)
				contentType = XPathParser.getAttributeString(node,
						"contenttype");
			String content = node.getTextContent();
			String url = XPathParser.getAttributeString(node, "url");
			Parser parser = doBinary(url, content, filePath, filename, client,
					parserSelector, contentType, urlDefaultCredential,
					proxyHandler, bFaultTolerant);
			if (parser != null)
				parser.populate(this);
		}
	}

	private Parser doBinary(String url, String content, String filePath,
			String filename, Client client, ParserSelector parserSelector,
			String contentType, CredentialItem urlDefaultCredential,
			ProxyHandler proxyHandler, boolean bFaultTolerant)
			throws IOException, URISyntaxException, InstantiationException,
			IllegalAccessException, ClassNotFoundException, SearchLibException {
		try {
			Parser parser = null;
			if (url != null)
				parser = binaryFromUrl(parserSelector, url,
						urlDefaultCredential, proxyHandler);
			else if (content != null && content.length() > 0)
				parser = binaryFromBase64(parserSelector, filename,
						contentType, content);
			else if (filePath != null && filePath.length() > 0)
				parser = binaryFromFile(parserSelector, filename, contentType,
						filePath);
			return parser;
		} catch (SearchLibException e) {
			ErrorParserLogger.log(url, filename, filePath, e);
			if (!bFaultTolerant)
				throw e;
		} catch (NullPointerException e) {
			ErrorParserLogger.log(url, filename, filePath, e);
			if (!bFaultTolerant)
				throw e;
		} catch (IllegalArgumentException e) {
			ErrorParserLogger.log(url, filename, filePath, e);
			if (!bFaultTolerant)
				throw e;
		} catch (RuntimeException e) {
			ErrorParserLogger.log(url, filename, filePath, e);
			if (!bFaultTolerant)
				throw new SearchLibException(e);
		} catch (Exception e) {
			ErrorParserLogger.log(url, filename, filePath, e);
			if (!bFaultTolerant)
				throw new SearchLibException(e);
		}
		return null;
	}

	private Parser binaryFromUrl(ParserSelector parserSelector, String url,
			CredentialItem credentialItem, ProxyHandler proxyHandler)
			throws SearchLibException {
		HttpDownloader httpDownloader = new HttpDownloader(null, false,
				proxyHandler);
		try {
			DownloadItem downloadItem = httpDownloader.get(new URI(url),
					credentialItem);
			return parserSelector.parseStream(null, downloadItem.getFileName(),
					downloadItem.getContentBaseType(), url,
					downloadItem.getContentInputStream(), lang, null);
		} catch (RuntimeException e) {
			throw new SearchLibException(
					"Parser error while getting binary from URL: " + url, e);
		} catch (Exception e) {
			throw new SearchLibException(
					"Parser error while getting binary from URL: " + url, e);
		} finally {
			httpDownloader.release();
		}
	}

	private Parser binaryFromBase64(ParserSelector parserSelector,
			String filename, String contentType, String content)
			throws SearchLibException {
		try {
			return parserSelector.parseBase64(null, filename, contentType,
					null, content, lang);
		} catch (RuntimeException e) {
			throw new SearchLibException("Parser error while getting binary : "
					+ filename + " /" + contentType, e);
		} catch (Exception e) {
			throw new SearchLibException("Parser error while getting binary : "
					+ filename + " /" + contentType, e);
		}
	}

	private Parser binaryFromFile(ParserSelector parserSelector,
			String filename, String contentType, String filePath)
			throws SearchLibException {
		try {
			File f = new File(filePath);
			if (f.isDirectory())
				f = new File(f, filename);
			return parserSelector.parseFile(null, filename, contentType, null,
					f, lang);
		} catch (RuntimeException e) {
			throw new SearchLibException(
					"Parser error while getting binary from file : " + filePath
							+ " /" + filename, e);
		} catch (Exception e) {
			throw new SearchLibException(
					"Parser error while getting binary from file : " + filePath
							+ " /" + filename, e);
		}
	}

	public FieldContent getFieldContent(String field) {
		FieldContent fc = fields.get(field);
		if (fc == null) {
			fc = new FieldContent(field);
			fields.put(field, fc);
		}
		return fc;
	}

	public void add(String field, FieldValueItem fieldValueItem) {
		FieldContent fc = getFieldContent(field);
		fc.add(fieldValueItem);
		fieldContentArray = null;
	}

	public void add(String field, String value, Float boost) {
		if (value == null || value.length() == 0)
			return;
		add(field, new FieldValueItem(FieldValueOriginEnum.EXTERNAL, value,
				boost));
	}

	public void addObject(String field, Object object) {
		if (object == null)
			return;
		addString(field, object.toString());
	}

	public void addString(String field, String value) {
		if (value == null)
			return;
		add(field, new FieldValueItem(FieldValueOriginEnum.EXTERNAL, value));
	}

	public void addFieldValueArray(String field, FieldValueItem[] values) {
		if (values == null)
			return;
		for (FieldValueItem value : values)
			add(field, value);
	}

	public void addObjectList(String field, List<Object> values) {
		if (values == null)
			return;
		for (Object value : values)
			addObject(field, value.toString());
	}

	public void addStringList(String field, List<String> values) {
		if (values == null)
			return;
		for (String value : values)
			addString(field, value);
	}

	public void add(String field, FieldContent fieldContent) {
		if (fieldContent == null)
			return;
		addFieldValueArray(field, fieldContent.getValues());
	}

	private void addIfNotAlreadyHere(FieldContent fieldContent) {
		if (fieldContent == null)
			return;
		FieldContent fc = getFieldContent(fieldContent.getField());
		if (fc.checkIfAlreadyHere(fieldContent))
			return;
		fc.add(fieldContent);
	}

	public void addIfNotAlreadyHere(IndexDocument source) {
		for (FieldContent fc : source.fields.values())
			addIfNotAlreadyHere(fc);
	}

	public void add(Map<String, FieldValueItem> fieldMap) {
		for (Map.Entry<String, FieldValueItem> entry : fieldMap.entrySet())
			add(entry.getKey(), entry.getValue());
	}

	public void add(IndexDocument source) {
		for (FieldContent fc : source.fields.values())
			add(fc.getField(), fc);
	}

	public void setString(String field, String value) {
		FieldContent fc = fields.get(field);
		if (fc != null)
			fc.clear();
		add(field, value, null);
	}

	public void setStringList(String field, List<String> values) {
		FieldContent fc = fields.get(field);
		if (fc != null)
			fc.clear();
		addStringList(field, values);
	}

	public void setFieldValueItems(String field, FieldValueItem[] values) {
		FieldContent fc = fields.get(field);
		if (fc != null)
			fc.clear();
		addFieldValueArray(field, values);
	}

	public void setObjectList(String field, List<Object> values) {
		FieldContent fc = fields.get(field);
		if (fc != null)
			fc.clear();
		addObjectList(field, values);
	}

	public void setObject(String field, Object value) {
		setString(field, value.toString());
	}

	public LanguageEnum getLang() {
		return lang;
	}

	public void setLang(LanguageEnum lang) {
		this.lang = lang;
	}

	public FieldContent getField(String fieldName) {
		return fields.get(fieldName);
	}

	public FieldValueItem getFieldValue(String fieldName, int pos) {
		if (fields == null)
			return null;
		FieldContent fc = fields.get(fieldName);
		if (fc == null)
			return null;
		return fc.getValue(pos);
	}

	public FieldContent[] getFieldContentArray() {
		if (fieldContentArray != null)
			return fieldContentArray;
		fieldContentArray = new FieldContent[fields.size()];
		fields.values().toArray(fieldContentArray);
		return fieldContentArray;
	}

	@Override
	public Iterator<FieldContent> iterator() {
		return fields.values().iterator();
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		if (fields != null) {
			for (String key : fields.keySet()) {
				FieldContent value = (FieldContent) fields.get(key);
				result.append(value.toString()).append("\n");
			}
		}
		return result.toString();
	}

}
