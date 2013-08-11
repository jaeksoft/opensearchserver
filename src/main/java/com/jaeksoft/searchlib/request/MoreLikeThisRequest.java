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
import java.io.StringReader;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.similar.MoreLikeThis;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.Analyzer;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.analysis.filter.stop.WordArray;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.filter.FilterAbstract;
import com.jaeksoft.searchlib.filter.FilterList;
import com.jaeksoft.searchlib.filter.QueryFilter;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.IndexAbstract;
import com.jaeksoft.searchlib.index.ReaderInterface;
import com.jaeksoft.searchlib.index.ReaderLocal;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.result.AbstractResult;
import com.jaeksoft.searchlib.result.AbstractResultSearch;
import com.jaeksoft.searchlib.result.ResultMoreLikeThis;
import com.jaeksoft.searchlib.schema.SchemaFieldList;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.jaeksoft.searchlib.web.ServletTransaction;

public class MoreLikeThisRequest extends AbstractRequest implements
		RequestInterfaces.FilterListInterface,
		RequestInterfaces.ReturnedFieldInterface {

	private String docQuery;
	private String likeText;
	private LanguageEnum lang;
	private String analyzerName;
	private ReturnFieldList fieldList;
	private int minWordLen;
	private int maxWordLen;
	private int minDocFreq;
	private int minTermFreq;
	private int maxNumTokensParsed;
	private int maxQueryTerms;
	private boolean boost;
	private String stopWords;
	private ReturnFieldList returnFieldList;
	private FilterList filterList;
	private int start;
	private int rows;
	private Query mltQuery;

	public MoreLikeThisRequest() {
		super(null, RequestTypeEnum.MoreLikeThisRequest);
	}

	public MoreLikeThisRequest(Config config) {
		super(config, RequestTypeEnum.MoreLikeThisRequest);
	}

	@Override
	protected void setDefaultValues() {
		super.setDefaultValues();
		this.filterList = new FilterList(this.config);
		this.returnFieldList = new ReturnFieldList();
		this.docQuery = null;
		this.likeText = null;
		this.lang = LanguageEnum.UNDEFINED;
		this.analyzerName = null;
		this.fieldList = new ReturnFieldList();
		this.minWordLen = MoreLikeThis.DEFAULT_MIN_WORD_LENGTH;
		this.maxWordLen = MoreLikeThis.DEFAULT_MAX_WORD_LENGTH;
		this.minDocFreq = MoreLikeThis.DEFAULT_MIN_DOC_FREQ;
		this.minTermFreq = MoreLikeThis.DEFAULT_MIN_TERM_FREQ;
		this.maxNumTokensParsed = MoreLikeThis.DEFAULT_MAX_NUM_TOKENS_PARSED;
		this.maxQueryTerms = MoreLikeThis.DEFAULT_MAX_QUERY_TERMS;
		this.boost = true;
		this.stopWords = null;
		this.start = 0;
		this.rows = 10;
		this.mltQuery = null;
	}

	@Override
	public void copyFrom(AbstractRequest request) {
		super.copyFrom(request);
		MoreLikeThisRequest mltRequest = (MoreLikeThisRequest) request;
		this.analyzerName = mltRequest.analyzerName;
		this.lang = mltRequest.lang;
		this.fieldList = new ReturnFieldList(mltRequest.fieldList);
		this.minWordLen = mltRequest.minWordLen;
		this.maxWordLen = mltRequest.maxWordLen;
		this.minDocFreq = mltRequest.minDocFreq;
		this.minTermFreq = mltRequest.minTermFreq;
		this.stopWords = mltRequest.stopWords;
		this.docQuery = mltRequest.docQuery;
		this.likeText = mltRequest.likeText;
		this.boost = mltRequest.boost;
		this.maxNumTokensParsed = mltRequest.maxNumTokensParsed;
		this.maxQueryTerms = mltRequest.maxQueryTerms;
		this.filterList = new FilterList(mltRequest.filterList);
		this.returnFieldList = new ReturnFieldList(mltRequest.returnFieldList);
		this.mltQuery = mltRequest.mltQuery;
	}

	@Override
	public Query getQuery() throws SearchLibException, IOException {
		rwl.r.lock();
		try {
			if (mltQuery != null)
				return mltQuery;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			if (mltQuery != null)
				return mltQuery;
			Config config = getConfig();
			IndexAbstract index = config.getIndexAbstract();

			MoreLikeThis mlt = index.getMoreLikeThis();
			mlt.setMinWordLen(minWordLen);
			mlt.setMaxWordLen(maxWordLen);
			mlt.setMinDocFreq(minDocFreq);
			mlt.setMinTermFreq(minTermFreq);
			mlt.setMaxNumTokensParsed(maxNumTokensParsed);
			mlt.setMaxQueryTerms(maxQueryTerms);
			mlt.setFieldNames(fieldList.getArrayName());
			mlt.setBoost(boost);

			if (analyzerName != null) {
				Analyzer analyzer = config.getSchema().getAnalyzerList()
						.get(analyzerName, lang);
				if (analyzer != null)
					mlt.setAnalyzer(analyzer.getQueryAnalyzer());
			}
			if (stopWords != null && stopWords.length() > 0) {
				WordArray wordArray = getConfig().getStopWordsManager()
						.getWordArray(stopWords, false);
				if (wordArray != null) {
					Set<String> stopWords = wordArray.getWordSet();
					if (stopWords != null)
						mlt.setStopWords(stopWords);
				}
			}

			if (docQuery != null && docQuery.length() > 0) {
				AbstractSearchRequest searchRequest = new SearchPatternRequest(
						config);
				searchRequest.setRows(1);
				searchRequest.setQueryString(docQuery);
				AbstractResultSearch result = (AbstractResultSearch) index
						.request(searchRequest);
				if (result.getNumFound() == 0)
					return mlt.like(new StringReader(""));
				int docId = result.getDocs().getIds()[0];
				mltQuery = mlt.like(docId);
			} else if (likeText != null & likeText.length() > 0) {
				mltQuery = mlt.like(new StringReader(likeText));
			}
			return mltQuery;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the docQuery
	 */
	public String getDocQuery() {
		rwl.r.lock();
		try {
			return docQuery;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param docQuery
	 *            the docQuery to set
	 */
	public void setDocQuery(String docQuery) {
		rwl.w.lock();
		try {
			this.docQuery = docQuery;
			mltQuery = null;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the likeText
	 */
	public String getLikeText() {
		rwl.r.lock();
		try {
			return likeText;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param likeText
	 *            the likeText to set
	 */
	public void setLikeText(String likeText) {
		rwl.w.lock();
		try {
			this.likeText = likeText;
			mltQuery = null;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * 
	 * @return the LanguageEnum
	 */
	public LanguageEnum getLang() {
		rwl.r.lock();
		try {
			return this.lang;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * 
	 * @param lang
	 *            The language to set
	 */
	public void setLang(LanguageEnum lang) {
		rwl.w.lock();
		try {
			this.lang = lang;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * 
	 * @return the AnalyzerName
	 */
	public String getAnalyzerName() {
		rwl.r.lock();
		try {
			return this.analyzerName;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * 
	 * @param lang
	 *            The language to set
	 */
	public void setAnalyzerName(String analyzerName) {
		rwl.w.lock();
		try {
			this.analyzerName = analyzerName;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the fieldList
	 */
	public ReturnFieldList getFieldList() {
		rwl.r.lock();
		try {
			return fieldList;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @return the minWordLen
	 */
	public int getMinWordLen() {
		rwl.r.lock();
		try {
			return minWordLen;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param minWordLen
	 *            the minWordLen to set
	 */
	public void setMinWordLen(int minWordLen) {
		rwl.w.lock();
		try {
			this.minWordLen = minWordLen;
			mltQuery = null;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the maxWordLen
	 */
	public int getMaxWordLen() {
		rwl.r.lock();
		try {
			return maxWordLen;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param maxWordLen
	 *            the maxWordLen to set
	 */
	public void setMaxWordLen(int maxWordLen) {
		rwl.w.lock();
		try {
			this.maxWordLen = maxWordLen;
			mltQuery = null;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the minDocFreq
	 */
	public int getMinDocFreq() {
		rwl.r.lock();
		try {
			return minDocFreq;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param minDocFreq
	 *            the minDocFreq to set
	 */
	public void setMinDocFreq(int minDocFreq) {
		rwl.w.lock();
		try {
			this.minDocFreq = minDocFreq;
			mltQuery = null;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the minTermFreq
	 */
	public int getMinTermFreq() {
		rwl.r.lock();
		try {
			return minTermFreq;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param minTermFreq
	 *            the minTermFreq to set
	 */
	public void setMinTermFreq(int minTermFreq) {
		rwl.w.lock();
		try {
			this.minTermFreq = minTermFreq;
			mltQuery = null;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the stopWords
	 */
	public String getStopWords() {
		rwl.r.lock();
		try {
			return stopWords;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param stopWords
	 *            the stopWords to set
	 */
	public void setStopWords(String stopWords) {
		rwl.w.lock();
		try {
			this.stopWords = stopWords;
			mltQuery = null;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the boost
	 */
	public boolean getBoost() {
		rwl.r.lock();
		try {
			return boost;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param boost
	 *            the boost to set
	 */
	public void setBoost(boolean boost) {
		rwl.w.lock();
		try {
			this.boost = boost;
			mltQuery = null;
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	public FilterList getFilterList() {
		rwl.r.lock();
		try {
			return this.filterList;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public void addFilter(String req, boolean negative) throws ParseException {
		rwl.w.lock();
		try {
			this.filterList.add(new QueryFilter(req, negative,
					FilterAbstract.Source.REQUEST, null));
			mltQuery = null;
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	public ReturnFieldList getReturnFieldList() {
		rwl.r.lock();
		try {
			return this.returnFieldList;
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public void addReturnField(String fieldName) {
		rwl.w.lock();
		try {
			returnFieldList.put(new ReturnField(config.getSchema()
					.getFieldList().get(fieldName).getName()));
			mltQuery = null;
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	protected void fromXmlConfigNoLock(Config config, XPathParser xpp, Node node)
			throws XPathExpressionException, DOMException, ParseException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		super.fromXmlConfigNoLock(config, xpp, node);
		setLang(LanguageEnum.findByCode(XPathParser.getAttributeString(node,
				"lang")));
		setAnalyzerName(XPathParser.getAttributeString(node, "analyzer"));
		setMinWordLen(XPathParser.getAttributeValue(node, "minWordLen"));
		setMaxWordLen(XPathParser.getAttributeValue(node, "maxWordLen"));
		setMinTermFreq(XPathParser.getAttributeValue(node, "minTermFreq"));
		setMinDocFreq(XPathParser.getAttributeValue(node, "minDocFreq"));
		setMaxNumTokensParsed(XPathParser.getAttributeValue(node,
				"maxNumTokensParsed"));
		setMaxQueryTerms(XPathParser.getAttributeValue(node, "maxQueryTerms"));
		setBoost(Boolean.TRUE.toString().equalsIgnoreCase(
				XPathParser.getAttributeString(node, "boost")));
		setStopWords(XPathParser.getAttributeString(node, "stopWords"));
		setStart(XPathParser.getAttributeValue(node, "start"));
		setRows(XPathParser.getAttributeValue(node, "rows"));

		NodeList mltFieldsNodes = xpp.getNodeList(node, "fields/field");
		if (mltFieldsNodes != null) {
			ReturnFieldList moreLikeThisFields = getFieldList();
			for (int i = 0; i < mltFieldsNodes.getLength(); i++) {
				ReturnField field = ReturnField.fromXmlConfig(mltFieldsNodes
						.item(i));
				if (field != null)
					moreLikeThisFields.put(field);
			}
		}

		Node mltDocQueryNode = xpp.getNode(node, "docQuery");
		if (mltDocQueryNode != null)
			setDocQuery(xpp.getNodeString(mltDocQueryNode, false));

		Node mltDocLikeText = xpp.getNode(node, "likeText");
		if (mltDocLikeText != null)
			setLikeText(xpp.getNodeString(mltDocLikeText, false));

		NodeList nodes = xpp.getNodeList(node, "filters/filter");
		for (int i = 0; i < nodes.getLength(); i++) {
			Node n = nodes.item(i);
			filterList.add(new QueryFilter(xpp.getNodeString(n, false), "yes"
					.equals(XPathParser.getAttributeString(n, "negative")),
					FilterAbstract.Source.CONFIGXML, null));
		}

		SchemaFieldList fieldList = config.getSchema().getFieldList();
		returnFieldList.filterCopy(fieldList,
				xpp.getNodeString(node, "returnFields"));
		nodes = xpp.getNodeList(node, "returnFields/field");
		for (int i = 0; i < nodes.getLength(); i++) {
			ReturnField field = ReturnField.fromXmlConfig(nodes.item(i));
			if (field != null)
				returnFieldList.put(field);
		}
	}

	@Override
	public void writeXmlConfig(XmlWriter xmlWriter) throws SAXException {
		rwl.r.lock();
		try {
			xmlWriter.startElement(XML_NODE_REQUEST, XML_ATTR_NAME,
					getRequestName(), XML_ATTR_TYPE, getType().name(),
					"minWordLen", Integer.toString(minWordLen), "maxWordLen",
					Integer.toString(maxWordLen), "minDocFreq",
					Integer.toString(minDocFreq), "minTermFreq",
					Integer.toString(minTermFreq), "maxNumTokensParsed",
					Integer.toString(maxNumTokensParsed), "maxQueryTerms",
					Integer.toString(maxQueryTerms), "boost",
					Boolean.toString(boost), "stopWords", stopWords, "start",
					Integer.toString(start), "rows", Integer.toString(rows),
					"lang", lang != null ? lang.getCode() : null, "analyzer",
					analyzerName);

			if (fieldList.size() > 0) {
				xmlWriter.startElement("fields");
				fieldList.writeXmlConfig(xmlWriter);
				xmlWriter.endElement();
			}
			if (docQuery != null && docQuery.length() > 0) {
				xmlWriter.startElement("docQuery");
				xmlWriter.textNode(docQuery);
				xmlWriter.endElement();
			}
			if (likeText != null && likeText.length() > 0) {
				xmlWriter.startElement("likeText");
				xmlWriter.textNode(likeText);
				xmlWriter.endElement();
			}
			if (returnFieldList.size() > 0) {
				xmlWriter.startElement("returnFields");
				returnFieldList.writeXmlConfig(xmlWriter);
				xmlWriter.endElement();
			}
			if (filterList.size() > 0) {
				xmlWriter.startElement("filters");
				filterList.writeXmlConfig(xmlWriter);
				xmlWriter.endElement();
			}
			xmlWriter.endElement();
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	protected void setFromServletNoLock(ServletTransaction transaction) {
		String p;
		Integer i;
		Boolean b;

		if ((p = transaction.getParameterString("mlt.docquery")) != null)
			setDocQuery(p);

		if ((p = transaction.getParameterString("mlt.liketext")) != null)
			setLikeText(p);

		if ((i = transaction.getParameterInteger("mlt.minwordlen")) != null)
			setMinWordLen(i);

		if ((i = transaction.getParameterInteger("mlt.maxwordlen")) != null)
			setMaxWordLen(i);

		if ((i = transaction.getParameterInteger("mlt.mindocfreq")) != null)
			setMinDocFreq(i);

		if ((i = transaction.getParameterInteger("mlt.mintermfreq")) != null)
			setMinTermFreq(i);

		if ((p = transaction.getParameterString("mlt.stopwords")) != null)
			setStopWords(p);

		if ((p = transaction.getParameterString("lang")) != null)
			setLang(LanguageEnum.findByCode(p));
		else if ((p = transaction.getParameterString("mlt.lang")) != null)
			setLang(LanguageEnum.findByCode(p));

		if ((p = transaction.getParameterString("mlt.analyzer")) != null)
			setAnalyzerName(p);

		if ((b = transaction.getParameterBoolean("mlt.boost")) != null)
			setBoost(b);

		if ((i = transaction.getParameterInteger("start")) != null)
			setStart(i);

		if ((i = transaction.getParameterInteger("rows")) != null)
			setRows(i);
	}

	@Override
	protected void resetNoLock() {
		mltQuery = null;
	}

	@Override
	public AbstractResult<MoreLikeThisRequest> execute(ReaderInterface reader)
			throws SearchLibException {
		try {
			return new ResultMoreLikeThis((ReaderLocal) reader, this);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (ParseException e) {
			throw new SearchLibException(e);
		} catch (SyntaxError e) {
			throw new SearchLibException(e);
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		} catch (ClassNotFoundException e) {
			throw new SearchLibException(e);
		}
	}

	@Override
	public String getInfo() {
		rwl.r.lock();
		try {
			StringBuffer sb = new StringBuffer();
			if (docQuery != null) {
				sb.append(docQuery);
				sb.append(' ');
			}
			if (likeText != null) {
				sb.append(likeText);
				sb.append(' ');
			}
			if (fieldList != null)
				sb.append(fieldList.toString());
			return sb.toString();
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @return the start
	 */
	public int getStart() {
		rwl.r.lock();
		try {
			return start;
		} finally {
			rwl.r.unlock();
		}
	}

	public int getEnd() {
		rwl.r.lock();
		try {
			return start + rows;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param start
	 *            the start to set
	 */
	public void setStart(int start) {
		rwl.w.lock();
		try {
			this.start = start;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the rows
	 */
	public int getRows() {
		rwl.r.lock();
		try {
			return rows;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param rows
	 *            the rows to set
	 */
	public void setRows(int rows) {
		rwl.w.lock();
		try {
			this.rows = rows;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the maxNumTokensParsed
	 */
	public int getMaxNumTokensParsed() {
		rwl.r.lock();
		try {
			return maxNumTokensParsed;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param maxNumTokensParsed
	 *            the maxNumTokensParsed to set
	 */
	public void setMaxNumTokensParsed(int maxNumTokensParsed) {
		rwl.w.lock();
		try {
			this.maxNumTokensParsed = maxNumTokensParsed;
			mltQuery = null;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the maxQueryTerms
	 */
	public int getMaxQueryTerms() {
		rwl.r.lock();
		try {
			return maxQueryTerms;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param maxQueryTerms
	 *            the maxQueryTerms to set
	 */
	public void setMaxQueryTerms(int maxQueryTerms) {
		rwl.w.lock();
		try {
			this.maxQueryTerms = maxQueryTerms;
			mltQuery = null;
		} finally {
			rwl.w.unlock();
		}
	}

}
