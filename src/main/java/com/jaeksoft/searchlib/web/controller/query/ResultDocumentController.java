/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012-2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.web.controller.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Html;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.TreeitemRenderer;
import org.zkoss.zul.Treerow;
import org.zkoss.zul.Window;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.RequestTypeEnum;
import com.jaeksoft.searchlib.result.AbstractResult;
import com.jaeksoft.searchlib.result.ResultDocument;
import com.jaeksoft.searchlib.result.ResultDocumentsInterface;
import com.jaeksoft.searchlib.result.collector.DocIdInterface;
import com.jaeksoft.searchlib.schema.FieldValueItem;

@AfterCompose(superclass = true)
public class ResultDocumentController extends AbstractQueryController implements
		TreeitemRenderer<Object> {

	public ResultDocumentController() throws SearchLibException {
		super(RequestTypeEnum.MoreLikeThisRequest,
				RequestTypeEnum.SearchRequest,
				RequestTypeEnum.SearchFieldRequest,
				RequestTypeEnum.DocumentsRequest,
				RequestTypeEnum.NamedEntityExtractionRequest);
	}

	public class Document {
		final long pos;
		StringBuilder title;
		ResultDocument resultDocument;

		private Document(long pos) {
			this.pos = pos;
			title = null;
			resultDocument = null;
		}

		public String getTitle() throws SearchLibException {
			synchronized (this) {
				if (title != null)
					return title.toString();
				title = new StringBuilder();
				title.append('#');
				title.append(getPos());
				float score = getScore();
				if (score != 0) {
					title.append(" - Score: ");
					title.append(getScore());
				}
				int collapseCount = getCollapseCount();
				if (collapseCount != 0) {
					title.append(" - Collapsed: ");
					title.append(getCollapseCount());
				}
				int docId = getDocId();
				if (docId != 0) {
					title.append(" - docId: ");
					title.append(docId);
				}
				return title.toString();
			}
		}

		public long getPos() {
			return pos;
		}

		public float getScore() throws SearchLibException {
			ResultDocumentsInterface<?> result = getResultDocuments();
			if (result == null)
				return 0;
			return result.getScore(pos);
		}

		public int getCollapseCount() throws SearchLibException {
			ResultDocumentsInterface<?> result = getResultDocuments();
			if (result == null)
				return 0;
			return result.getCollapseCount(pos);
		}

		public int getDocId() throws SearchLibException {
			ResultDocumentsInterface<?> result = getResultDocuments();
			if (result == null)
				return 0;
			DocIdInterface docIdInterface = result.getDocs();
			if (docIdInterface == null)
				return 0;
			return docIdInterface.getIds()[(int) pos];
		}

		public ResultDocument getResultDocument() throws IOException,
				ParseException, SyntaxError, SearchLibException {
			if (resultDocument != null)
				return resultDocument;
			ResultDocumentsInterface<?> result = getResultDocuments();
			if (result == null)
				return null;
			resultDocument = result.getDocument(pos, null);
			return resultDocument;
		}

		public boolean isReturnValid() throws IOException, ParseException,
				SyntaxError, SearchLibException {
			ResultDocument resultDocument = getResultDocument();
			if (resultDocument == null)
				return false;
			return resultDocument.getReturnFields().size() > 0;
		}

		public boolean isSnippetValid() throws IOException, ParseException,
				SyntaxError, SearchLibException {
			ResultDocument resultDocument = getResultDocument();
			if (resultDocument == null)
				return false;
			return resultDocument.getSnippetFields().size() > 0;
		}

		public boolean isFunctionValid() throws IOException, ParseException,
				SyntaxError, SearchLibException {
			ResultDocument resultDocument = getResultDocument();
			if (resultDocument == null)
				return false;
			if (resultDocument.getFunctionFieldValues() == null)
				return false;
			return resultDocument.getFunctionFieldValues().size() > 0;
		}

	}

	private transient List<Document> documents;

	@Override
	protected void reset() throws SearchLibException {
		documents = null;
	}

	@Override
	@Command
	public void reload() throws SearchLibException {
		synchronized (this) {
			documents = null;
			super.reload();
		}
	}

	private ResultDocumentsInterface<?> getResultDocuments()
			throws SearchLibException {
		AbstractResult<?> result = getResult();
		if (result == null)
			return null;
		if (!(result instanceof ResultDocumentsInterface))
			return null;
		return (ResultDocumentsInterface<?>) result;
	}

	public List<Document> getDocuments() throws SearchLibException {
		synchronized (this) {
			if (documents != null)
				return documents;
			ResultDocumentsInterface<?> result = getResultDocuments();
			if (result == null)
				return null;
			int docCount = result.getDocumentCount();
			if (docCount <= 0)
				return null;
			long pos = result.getRequestStart();
			documents = new ArrayList<Document>(docCount);
			long end = pos + docCount;
			while (pos < end)
				documents.add(new Document(pos++));
			return documents;
		}
	}

	@Command
	public void explainScore(@BindingParam("document") Document document)
			throws SearchLibException, InterruptedException, IOException,
			ParseException, SyntaxError {
		Client client = getClient();
		if (client == null)
			return;
		ResultDocumentsInterface<?> result = getResultDocuments();
		if (result == null)
			return;
		int docId = document.getDocId();
		String explanation = client.explain(result.getRequest(), docId, true);
		Window win = (Window) Executions.createComponents(
				"/WEB-INF/zul/query/result/explanation.zul", null, null);
		Html html = (Html) win.getFellow("htmlExplain", true);
		html.setContent(explanation);
		win.doModal();
	}

	private void renderValue(Treerow treerow, String value) {
		Treecell treecell = new Treecell(value);
		treecell.setSpan(2);
		treecell.setParent(treerow);
	}

	private void renderValue(Treerow treerow, FieldValueItem value) {
		renderValue(treerow, value.getValue());
	}

	@Override
	public void render(Treeitem item, Object data, int index) throws Exception {
		Treerow treerow = new Treerow();
		if (data instanceof String)
			renderValue(treerow, (String) data);
		else if (data instanceof FieldValueItem)
			renderValue(treerow, (FieldValueItem) data);
		treerow.setParent(item);
	}

}
