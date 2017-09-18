/*
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2008-2015 Emmanuel Keller / Jaeksoft
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
 */

package com.jaeksoft.searchlib.index;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.crawler.web.database.CredentialItem;
import com.jaeksoft.searchlib.crawler.web.spider.DownloadItem;
import com.jaeksoft.searchlib.crawler.web.spider.HttpDownloader;
import com.jaeksoft.searchlib.logreport.ErrorParserLogger;
import com.jaeksoft.searchlib.parser.Parser;
import com.jaeksoft.searchlib.parser.ParserSelector;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.schema.FieldValueOriginEnum;
import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.XPathParser;
import org.apache.commons.text.StringEscapeUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

import javax.xml.xpath.XPathExpressionException;
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
import java.util.function.BiConsumer;

public class IndexDocument implements Iterable<FieldContent> {

	private final Map<String, FieldContent> fields;
	private LanguageEnum lang;

	public IndexDocument() {
		fields = new TreeMap<>();
		this.lang = null;
	}

	public IndexDocument(IndexDocument sourceDocument) {
		this(sourceDocument.lang);
		for (Map.Entry<String, FieldContent> entry : sourceDocument.fields.entrySet())
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

	private final List<String> getCopyFieldList(Node fieldNode) throws XPathExpressionException {
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
	 * Create a new instance of IndexDocument from an XML structure <br>
	 * &lt;field name="FIELDNAME"&gt;<br>
	 * &nbsp;&nbsp;&lt;value&gt;VALUE1&lt;/value&gt;<br>
	 * &nbsp;&nbsp;&lt;value&gt;VALUE2&lt;/value&gt;<br>
	 * &lt;/field&gt;
	 *
	 * @param client
	 * @param parserSelector
	 * @param documentNode
	 * @param urlDefaultCredential
	 * @param httpDownloader
	 * @throws XPathExpressionException
	 * @throws SearchLibException
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws DOMException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public IndexDocument(Client client, ParserSelector parserSelector, Node documentNode,
			CredentialItem urlDefaultCredential, HttpDownloader httpDownloader)
			throws XPathExpressionException, SearchLibException, InstantiationException, IllegalAccessException,
			ClassNotFoundException, IOException, URISyntaxException {
		this(LanguageEnum.findByCode(XPathParser.getAttributeString(documentNode, "lang")));
		List<Node> fieldNodes = DomUtils.getNodes(documentNode, "field");
		for (Node fieldNode : fieldNodes) {
			List<String> copyFieldList = getCopyFieldList(fieldNode);
			String fieldName = XPathParser.getAttributeString(fieldNode, "name");
			List<Node> valueNodes = DomUtils.getNodes(fieldNode, "value");
			for (Node valueNode : valueNodes) {
				boolean removeTag = "yes".equalsIgnoreCase(XPathParser.getAttributeString(valueNode, "removeTag"));
				boolean convertHtmlEntities = "yes".equalsIgnoreCase(
						XPathParser.getAttributeString(valueNode, "convertHtmlEntities"));

				String textContent = valueNode.getTextContent();
				if (convertHtmlEntities)
					textContent = StringEscapeUtils.unescapeHtml4(textContent);
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
			boolean bFaultTolerant = "yes".equalsIgnoreCase(XPathParser.getAttributeString(node, "faultTolerant"));
			String filename = XPathParser.getAttributeString(node, "fileName");
			if (filename == null || filename.length() == 0)
				filename = XPathParser.getAttributeString(node, "filename");
			String filePath = XPathParser.getAttributeString(node, "filePath");
			if (filePath == null || filePath.length() == 0)
				filePath = XPathParser.getAttributeString(node, "filepath");
			String contentType = XPathParser.getAttributeString(node, "contentType");
			if (contentType == null || contentType.length() == 0)
				contentType = XPathParser.getAttributeString(node, "contenttype");
			String content = node.getTextContent();
			String url = XPathParser.getAttributeString(node, "url");
			Parser parser = doBinary(url, content, filePath, filename, client, parserSelector, contentType,
					urlDefaultCredential, httpDownloader, bFaultTolerant);
			if (parser != null)
				parser.popupateResult(0, this);
		}
	}

	private Parser doBinary(String url, String content, String filePath, String filename, Client client,
			ParserSelector parserSelector, String contentType, CredentialItem urlDefaultCredential,
			HttpDownloader httpDownloader, boolean bFaultTolerant)
			throws IOException, URISyntaxException, InstantiationException, IllegalAccessException,
			ClassNotFoundException, SearchLibException {
		try {
			Parser parser = null;
			if (url != null)
				parser = binaryFromUrl(parserSelector, url, urlDefaultCredential, httpDownloader);
			else if (content != null && content.length() > 0)
				parser = binaryFromBase64(parserSelector, filename, contentType, content);
			else if (filePath != null && filePath.length() > 0)
				parser = binaryFromFile(parserSelector, filename, contentType, filePath);
			return parser;
		} catch (SearchLibException | NullPointerException | IllegalArgumentException e) {
			ErrorParserLogger.log(url, filename, filePath, e);
			if (!bFaultTolerant)
				throw e;
		} catch (Exception e) {
			ErrorParserLogger.log(url, filename, filePath, e);
			if (!bFaultTolerant)
				throw new SearchLibException(e);
		}
		return null;
	}

	private Parser binaryFromUrl(ParserSelector parserSelector, String url, CredentialItem credentialItem,
			HttpDownloader httpDownloader) throws SearchLibException {
		try {
			DownloadItem downloadItem = httpDownloader.get(new URI(url), credentialItem);
			downloadItem.checkNoErrorList(200);
			return parserSelector.parseStream(null, downloadItem.getFileName(), downloadItem.getContentBaseType(), url,
					downloadItem.getContentInputStream(), lang, null, null);
		} catch (RuntimeException e) {
			throw new SearchLibException("Parser error while getting binary from URL: " + url, e);
		} catch (Exception e) {
			throw new SearchLibException("Parser error while getting binary from URL: " + url, e);
		}
	}

	private Parser binaryFromBase64(ParserSelector parserSelector, String filename, String contentType, String content)
			throws SearchLibException {
		try {
			return parserSelector.parseBase64(null, filename, contentType, null, content, lang);
		} catch (Exception e) {
			throw new SearchLibException("Parser error while getting binary : " + filename + " /" + contentType, e);
		}
	}

	private Parser binaryFromFile(ParserSelector parserSelector, String filename, String contentType, String filePath)
			throws SearchLibException {
		try {
			File f = new File(filePath);
			if (f.isDirectory())
				f = new File(f, filename);
			return parserSelector.parseFile(null, filename, contentType, null, f, lang);
		} catch (Exception e) {
			throw new SearchLibException("Parser error while getting binary from file : " + filePath + " /" + filename,
					e);
		}
	}

	public void forEachFieldValueItem(final BiConsumer<String, List<FieldValueItem>> consumer) {
		fields.forEach((fieldName, fieldContent) -> {
			if (fieldContent.hasContent())
				consumer.accept(fieldName, fieldContent.getValues());
		});
	}

	public FieldContent getFieldContent(String field) {
		if (field == null)
			return null;
		field = field.intern();
		FieldContent fc = fields.get(field);
		if (fc == null) {
			fc = new FieldContent(field);
			fields.put(field, fc);
		}
		return fc;
	}

	public void add(String field, FieldValueItem fieldValueItem) {
		if (field == null)
			return;
		FieldContent fc = getFieldContent(field);
		fc.add(fieldValueItem);
	}

	public void add(String field, String value, Float boost) {
		if (value == null || value.length() == 0)
			return;
		add(field, new FieldValueItem(FieldValueOriginEnum.EXTERNAL, value, boost));
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

	public void addFieldIndexDocument(String field, IndexDocument source) {
		if (source == null)
			return;
		for (FieldContent fieldContent : source)
			add(field, fieldContent);
	}

	public void addFieldValueList(String field, List<FieldValueItem> values) {
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
		addFieldValueList(field, fieldContent.getValues());
	}

	private void addIfNotAlreadyHere(FieldContent fieldContent) {
		if (fieldContent == null)
			return;
		FieldContent fc = getFieldContent(fieldContent.getField());
		fc.addIfNotAlreadyHere(fieldContent);
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

	public void setFieldValueItems(String field, List<FieldValueItem> values) {
		FieldContent fc = fields.get(field);
		if (fc != null)
			fc.clear();
		addFieldValueList(field, values);
	}

	public void setSameValueItems(String field, List<FieldValueItem> values) {
		if (field == null)
			return;
		FieldContent fc = getFieldContent(field);
		fc.setValueItems(values);
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

	final public FieldContent getField(final String fieldName) {
		return fields.get(fieldName);
	}

	final public boolean hasContent(final String fieldName) {
		FieldContent fc = getField(fieldName);
		if (fc == null)
			return false;
		return fc.hasContent();
	}

	public FieldValueItem getFieldValue(String fieldName, int pos) {
		if (fields == null)
			return null;
		FieldContent fc = fields.get(fieldName);
		if (fc == null)
			return null;
		return fc.getValue(pos);
	}

	public String getFieldValueString(String fieldName, int pos) {
		FieldValueItem fvi = getFieldValue(fieldName, pos);
		if (fvi == null)
			return null;
		return fvi.getValue();
	}

	@Override
	public Iterator<FieldContent> iterator() {
		return fields.values().iterator();
	}

	final public FieldContent[] getFieldContentArray() {
		return fields.values().toArray(new FieldContent[fields.size()]);
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		if (fields != null) {
			for (String key : fields.keySet()) {
				FieldContent value = fields.get(key);
				result.append(value.toString()).append("\n");
			}
		}
		return result.toString();
	}

	/**
	 * Populate the copyOf field with a reference to the fieldcontent of the
	 * source field
	 *
	 * @param schema
	 */
	public void prepareCopyOf(Schema schema) {
		for (SchemaField schemaField : schema.getFieldList()) {
			String fname = schemaField.getName();
			List<String> copyOfList = schemaField.getCopyOf();
			if (copyOfList == null)
				continue;
			for (String copyOf : copyOfList) {
				if (copyOf == null || copyOf.length() == 0)
					continue;
				FieldContent fieldContent = fields.get(copyOf);
				if (fieldContent != null)
					add(fname, fieldContent);
			}
		}

	}
}
