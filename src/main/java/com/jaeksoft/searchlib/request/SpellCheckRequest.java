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

package com.jaeksoft.searchlib.request;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.ReaderInterface;
import com.jaeksoft.searchlib.index.ReaderLocal;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.result.AbstractResult;
import com.jaeksoft.searchlib.result.ResultSpellCheck;
import com.jaeksoft.searchlib.schema.Schema;
import com.jaeksoft.searchlib.schema.SchemaFieldList;
import com.jaeksoft.searchlib.spellcheck.SpellCheckField;
import com.jaeksoft.searchlib.spellcheck.SpellCheckFieldList;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.jaeksoft.searchlib.web.ServletTransaction;

public class SpellCheckRequest extends AbstractRequest {

	private SpellCheckFieldList spellCheckFieldList;

	private String queryString;

	private LanguageEnum lang;

	public SpellCheckRequest() {
		super(null, RequestTypeEnum.SpellCheckRequest);
	}

	public SpellCheckRequest(Config config) {
		super(config, RequestTypeEnum.SpellCheckRequest);
	}

	@Override
	public void setDefaultValues() {
		super.setDefaultValues();
		this.spellCheckFieldList = new SpellCheckFieldList();
		this.queryString = null;
		this.lang = null;
	}

	@Override
	public void copyFrom(AbstractRequest request) {
		super.copyFrom(request);
		SpellCheckRequest spellCheckrequest = (SpellCheckRequest) request;
		this.spellCheckFieldList = new SpellCheckFieldList(
				spellCheckrequest.spellCheckFieldList);
		this.queryString = spellCheckrequest.queryString;
		this.lang = spellCheckrequest.lang;
	}

	public SpellCheckFieldList getSpellCheckFieldList() {
		rwl.r.lock();
		try {
			return this.spellCheckFieldList;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public void fromXmlConfigNoLock(Config config, XPathParser xpp, Node node)
			throws XPathExpressionException, DOMException, ParseException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		super.fromXmlConfigNoLock(config, xpp, node);
		SchemaFieldList schemaFieldList = config.getSchema().getFieldList();
		NodeList nodes = xpp.getNodeList(node,
				"spellCheckFields/spellCheckField");
		int l = nodes.getLength();
		for (int i = 0; i < l; i++)
			SpellCheckField.copySpellCheckFields(nodes.item(i),
					schemaFieldList, spellCheckFieldList);
		setLang(LanguageEnum.findByCode(XPathParser.getAttributeString(node,
				"lang")));
		setQueryString(xpp.getNodeString(node, "query"));
	}

	@Override
	public void writeXmlConfig(XmlWriter xmlWriter) throws SAXException {
		rwl.r.lock();
		try {
			xmlWriter.startElement(XML_NODE_REQUEST, XML_ATTR_NAME,
					getRequestName(), XML_ATTR_TYPE, getType().name(), "lang",
					lang != null ? lang.getCode() : null);
			if (spellCheckFieldList.size() > 0) {
				xmlWriter.startElement("spellCheckFields");
				spellCheckFieldList.writeXmlConfig(xmlWriter);
				xmlWriter.endElement();
			}
			if (queryString != null && queryString.trim().length() > 0) {
				xmlWriter.startElement("query");
				xmlWriter.textNode(queryString);
				xmlWriter.endElement();
			}
			xmlWriter.endElement();

		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	protected void setFromServletNoLock(ServletTransaction transaction)
			throws SyntaxError {
		String p;
		if ((p = transaction.getParameterString("query")) != null)
			setQueryString(p);
		else if ((p = transaction.getParameterString("q")) != null)
			setQueryString(p);
	}

	@Override
	protected void resetNoLock() {
	}

	@Override
	public AbstractResult<SpellCheckRequest> execute(ReaderInterface reader)
			throws SearchLibException {
		try {
			return new ResultSpellCheck((ReaderLocal) reader, this);
		} catch (ParseException e) {
			throw new SearchLibException(e);
		} catch (SyntaxError e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		}
	}

	public Set<Term> getTermSet(String fieldName) throws SearchLibException,
			ParseException {
		Set<Term> set = new LinkedHashSet<Term>();
		Schema schema = config.getSchema();
		QueryParser queryParser = new QueryParser(Version.LUCENE_36, fieldName,
				schema.getQueryPerFieldAnalyzer(lang));
		try {
			queryParser.parse(queryString).extractTerms(set);
		} catch (org.apache.lucene.queryParser.ParseException e) {
			throw new ParseException(e);
		}
		return set;
	}

	@Override
	public String getInfo() {
		rwl.r.lock();
		try {
			StringBuilder sb = new StringBuilder();
			for (SpellCheckField field : spellCheckFieldList) {
				if (sb.length() > 0)
					sb.append(", ");
				sb.append(field.getName());
			}
			return sb.toString();
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @return the queryString
	 */
	public String getQueryString() {
		rwl.r.lock();
		try {
			return queryString;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param queryString
	 *            the queryString to set
	 */
	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	public LanguageEnum getLang() {
		rwl.r.lock();
		try {
			return this.lang;
		} finally {
			rwl.r.unlock();
		}
	}

	public void setLang(LanguageEnum lang) {
		rwl.w.lock();
		try {
			if (this.lang == lang)
				return;
			this.lang = lang;
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	public Query getQuery() throws ParseException, SyntaxError,
			SearchLibException, IOException {
		return null;
	}

}
