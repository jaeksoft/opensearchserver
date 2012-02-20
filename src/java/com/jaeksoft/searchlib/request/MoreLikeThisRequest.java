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

import javax.xml.xpath.XPathExpressionException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.similar.MoreLikeThis;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.index.IndexAbstract;
import com.jaeksoft.searchlib.index.ReaderInterface;
import com.jaeksoft.searchlib.result.AbstractResult;
import com.jaeksoft.searchlib.result.AbstractResultSearch;
import com.jaeksoft.searchlib.schema.Field;
import com.jaeksoft.searchlib.schema.FieldList;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.jaeksoft.searchlib.web.ServletTransaction;

public class MoreLikeThisRequest extends AbstractRequest {

	private LanguageEnum lang;
	private Analyzer analyzer;
	private String moreLikeThisDocQuery;
	private FieldList<Field> moreLikeThisFieldList;
	private int moreLikeThisMinWordLen;
	private int moreLikeThisMaxWordLen;
	private int moreLikeThisMinDocFreq;
	private int moreLikeThisMinTermFreq;
	private String moreLikeThisStopWords;

	public MoreLikeThisRequest() {
	}

	public MoreLikeThisRequest(Config config) {
		super(config);
	}

	@Override
	protected void setDefaultValues() {
		super.setDefaultValues();
		this.lang = null;
		this.moreLikeThisFieldList = new FieldList<Field>();
		this.moreLikeThisMinWordLen = 0;
		this.moreLikeThisMaxWordLen = 0;
		this.moreLikeThisMinDocFreq = 0;
		this.moreLikeThisMinTermFreq = 0;
		this.moreLikeThisStopWords = null;
	}

	@Override
	public void copyFrom(AbstractRequest request) {
		super.copyFrom(request);
		MoreLikeThisRequest mltRequest = (MoreLikeThisRequest) request;
		this.lang = mltRequest.lang;
		this.moreLikeThisFieldList = new FieldList<Field>(
				mltRequest.moreLikeThisFieldList);
		this.moreLikeThisMinWordLen = mltRequest.moreLikeThisMinWordLen;
		this.moreLikeThisMaxWordLen = mltRequest.moreLikeThisMaxWordLen;
		this.moreLikeThisMinDocFreq = mltRequest.moreLikeThisMinDocFreq;
		this.moreLikeThisMinTermFreq = mltRequest.moreLikeThisMinTermFreq;
		this.moreLikeThisStopWords = mltRequest.moreLikeThisStopWords;
		this.moreLikeThisDocQuery = mltRequest.moreLikeThisDocQuery;
	}

	private Analyzer checkAnalyzer() throws SearchLibException {
		if (analyzer == null)
			analyzer = config.getSchema().getQueryPerFieldAnalyzer(lang);
		return analyzer;
	}

	private Query getMoreLikeThisQuery() throws SearchLibException, IOException {
		Config config = getConfig();
		IndexAbstract index = config.getIndex();
		MoreLikeThis mlt = index.getMoreLikeThis();
		SearchRequest searchRequest = new SearchRequest(config);
		searchRequest.setRows(1);
		searchRequest.setQueryString(moreLikeThisDocQuery);
		AbstractResultSearch result = (AbstractResultSearch) index
				.request(searchRequest);
		if (result.getNumFound() == 0)
			return mlt.like(new StringReader(""));
		int docId = result.getDocs()[0].doc;
		mlt.setMinWordLen(moreLikeThisMinWordLen);
		mlt.setMaxWordLen(moreLikeThisMaxWordLen);
		mlt.setMinDocFreq(moreLikeThisMinDocFreq);
		mlt.setMinTermFreq(moreLikeThisMinTermFreq);
		mlt.setFieldNames(moreLikeThisFieldList.toArrayName());
		mlt.setAnalyzer(checkAnalyzer());
		if (moreLikeThisStopWords != null)
			mlt.setStopWords(getConfig().getStopWordsManager()
					.getWordArray(moreLikeThisStopWords, false).getWordSet());
		return mlt.like(docId);
	}

	/**
	 * @return the moreLikeThisDocQuery
	 */
	public String getMoreLikeThisDocQuery() {
		rwl.r.lock();
		try {
			return moreLikeThisDocQuery;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param moreLikeThisDocQuery
	 *            the moreLikeThisDocQuery to set
	 */
	public void setMoreLikeThisDocQuery(String moreLikeThisDocQuery) {
		rwl.w.lock();
		try {
			this.moreLikeThisDocQuery = moreLikeThisDocQuery;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the moreLikeThisFieldList
	 */
	public FieldList<Field> getMoreLikeThisFieldList() {
		rwl.r.lock();
		try {
			return moreLikeThisFieldList;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @return the moreLikeThisMinWordLen
	 */
	public int getMoreLikeThisMinWordLen() {
		rwl.r.lock();
		try {
			return moreLikeThisMinWordLen;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param moreLikeThisMinWordLen
	 *            the moreLikeThisMinWordLen to set
	 */
	public void setMoreLikeThisMinWordLen(int moreLikeThisMinWordLen) {
		rwl.w.lock();
		try {
			this.moreLikeThisMinWordLen = moreLikeThisMinWordLen;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the moreLikeThisMaxWordLen
	 */
	public int getMoreLikeThisMaxWordLen() {
		rwl.r.lock();
		try {
			return moreLikeThisMaxWordLen;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param moreLikeThisMaxWordLen
	 *            the moreLikeThisMaxWordLen to set
	 */
	public void setMoreLikeThisMaxWordLen(int moreLikeThisMaxWordLen) {
		rwl.w.lock();
		try {
			this.moreLikeThisMaxWordLen = moreLikeThisMaxWordLen;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the moreLikeThisMinDocFreq
	 */
	public int getMoreLikeThisMinDocFreq() {
		rwl.r.lock();
		try {
			return moreLikeThisMinDocFreq;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param moreLikeThisMinDocFreq
	 *            the moreLikeThisMinDocFreq to set
	 */
	public void setMoreLikeThisMinDocFreq(int moreLikeThisMinDocFreq) {
		rwl.w.lock();
		try {
			this.moreLikeThisMinDocFreq = moreLikeThisMinDocFreq;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the moreLikeThisMinTermFreq
	 */
	public int getMoreLikeThisMinTermFreq() {
		rwl.r.lock();
		try {
			return moreLikeThisMinTermFreq;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param moreLikeThisMinTermFreq
	 *            the moreLikeThisMinTermFreq to set
	 */
	public void setMoreLikeThisMinTermFreq(int moreLikeThisMinTermFreq) {
		rwl.w.lock();
		try {
			this.moreLikeThisMinTermFreq = moreLikeThisMinTermFreq;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the moreLikeThisStopWords
	 */
	public String getMoreLikeThisStopWords() {
		rwl.r.lock();
		try {
			return moreLikeThisStopWords;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param moreLikeThisStopWords
	 *            the moreLikeThisStopWords to set
	 */
	public void setMoreLikeThisStopWords(String moreLikeThisStopWords) {
		rwl.w.lock();
		try {
			this.moreLikeThisStopWords = moreLikeThisStopWords;
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	public void fromXmlConfig(Config config, XPathParser xpp, Node node)
			throws XPathExpressionException, DOMException, ParseException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		rwl.w.lock();
		try {
			super.fromXmlConfig(config, xpp, node);
			setMoreLikeThisMinWordLen(XPathParser.getAttributeValue(node,
					"minWordLen"));
			setMoreLikeThisMaxWordLen(XPathParser.getAttributeValue(node,
					"maxWordLen"));
			setMoreLikeThisMinTermFreq(XPathParser.getAttributeValue(node,
					"minTermFreq"));
			setMoreLikeThisMinDocFreq(XPathParser.getAttributeValue(node,
					"minDocFreq"));
			setMoreLikeThisStopWords(XPathParser.getAttributeString(node,
					"stopWords"));

			NodeList mltFieldsNodes = xpp.getNodeList(node, "fields/field");
			if (mltFieldsNodes != null) {
				FieldList<Field> moreLikeThisFields = getMoreLikeThisFieldList();
				for (int i = 0; i < mltFieldsNodes.getLength(); i++) {
					Field field = Field.fromXmlConfig(mltFieldsNodes.item(i));
					if (field != null)
						moreLikeThisFields.add(field);
				}
			}
			Node mltDocQueryNode = xpp.getNode(node, "docQuery");
			if (mltDocQueryNode != null)
				setMoreLikeThisDocQuery(xpp.getNodeString(mltDocQueryNode));
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	public void writeXmlConfig(XmlWriter xmlWriter) throws SAXException {
		rwl.r.lock();
		try {
			xmlWriter.startElement("moreLikeThis", "minWordLen",
					Integer.toString(moreLikeThisMinWordLen), "maxWordLen",
					Integer.toString(moreLikeThisMaxWordLen), "minDocFreq",
					Integer.toString(moreLikeThisMinDocFreq), "minTermFreq",
					Integer.toString(moreLikeThisMinTermFreq), "stopWords");

			if (moreLikeThisFieldList.size() > 0) {
				xmlWriter.startElement("fields");
				moreLikeThisFieldList.writeXmlConfig(xmlWriter);
				xmlWriter.endElement();
			}
			if (moreLikeThisDocQuery != null
					&& moreLikeThisDocQuery.length() > 0) {
				xmlWriter.startElement("docQuery");
				xmlWriter.textNode(moreLikeThisDocQuery);
				xmlWriter.endElement();
			}
			xmlWriter.endElement();
		} finally {
			rwl.r.unlock();
		}
	}

	@Override
	public RequestTypeEnum getType() {
		return RequestTypeEnum.MoreLikeThisRequest;
	}

	@Override
	public void setFromServlet(ServletTransaction transaction) {
		rwl.w.lock();
		try {
			String p;
			Integer i;

			if ((p = transaction.getParameterString("mlt.docquery")) != null)
				setMoreLikeThisDocQuery(p);

			if ((i = transaction.getParameterInteger("mlt.minwordlen")) != null)
				setMoreLikeThisMinWordLen(i);

			if ((i = transaction.getParameterInteger("mlt.maxwordlen")) != null)
				setMoreLikeThisMaxWordLen(i);

			if ((i = transaction.getParameterInteger("mlt.mindocfreq")) != null)
				setMoreLikeThisMinDocFreq(i);

			if ((i = transaction.getParameterInteger("mlt.mintermfreq")) != null)
				setMoreLikeThisMinTermFreq(i);

			if ((p = transaction.getParameterString("mlt.stopwords")) != null)
				setMoreLikeThisStopWords(p);
		} finally {
			rwl.w.unlock();
		}

	}

	@Override
	public void reset() {
	}

	@Override
	public AbstractResult<MoreLikeThisRequest> execute(ReaderInterface reader)
			throws SearchLibException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getInfo() {
		// TODO Auto-generated method stub
		return null;
	}
}
