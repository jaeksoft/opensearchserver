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

package com.jaeksoft.searchlib.web.controller.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.AbstractTreeModel;
import org.zkoss.zul.Html;
import org.zkoss.zul.TreeModel;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.TreeitemRenderer;
import org.zkoss.zul.Treerow;
import org.zkoss.zul.api.Window;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.RequestTypeEnum;
import com.jaeksoft.searchlib.result.AbstractResult;
import com.jaeksoft.searchlib.result.ResultDocument;
import com.jaeksoft.searchlib.result.ResultDocumentsInterface;
import com.jaeksoft.searchlib.schema.FieldValue;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.snippet.SnippetFieldValue;

public class ResultDocumentController extends AbstractQueryController implements
		TreeitemRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8531833452956918954L;

	public ResultDocumentController() throws SearchLibException {
		super(RequestTypeEnum.MoreLikeThisRequest,
				RequestTypeEnum.SearchRequest);
	}

	public class Document {
		final int pos;
		StringBuffer title;
		ResultDocument resultDocument;

		private Document(int pos) {
			this.pos = pos;
			title = null;
			resultDocument = null;
		}

		public String getTitle() throws SearchLibException {
			synchronized (this) {
				if (title != null)
					return title.toString();
				title = new StringBuffer();
				title.append('#');
				title.append(getPos());
				title.append(" - Score: ");
				title.append(getScore());
				title.append(" - Collapsed: ");
				title.append(getCollapseCount());
				title.append(" - docId: ");
				title.append(getDocId());
				return title.toString();
			}
		}

		public int getPos() {
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
			return result.getDocs().getIds()[pos];
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

		public String getReturnPercent() throws IOException, ParseException,
				SyntaxError, SearchLibException {
			if (!isReturnValid())
				return "0%";
			if (!isSnippetValid())
				return "100%";
			return "50%";
		}

		public String getSnippetPercent() throws IOException, ParseException,
				SyntaxError, SearchLibException {
			if (!isSnippetValid())
				return "0%";
			if (!isReturnValid())
				return "100%";
			return "50%";
		}

		public TreeModel getReturnTree() throws IOException, ParseException,
				SyntaxError, SearchLibException {
			ResultDocument resultDocument = getResultDocument();
			if (resultDocument == null)
				return null;
			return new FieldTreeModel<FieldValue>(
					ResultDocument.<FieldValue> toList(resultDocument
							.getReturnFields()));
		}

		public TreeModel getSnippetTree() throws IOException, ParseException,
				SyntaxError, SearchLibException {
			ResultDocument resultDocument = getResultDocument();
			if (resultDocument == null)
				return null;
			return new FieldTreeModel<SnippetFieldValue>(
					ResultDocument.<SnippetFieldValue> toList(resultDocument
							.getSnippetFields()));
		}
	}

	public class FieldTreeModel<T extends FieldValue> extends AbstractTreeModel {

		/**
		 * 
		 */
		private static final long serialVersionUID = -1438357359217617272L;

		public FieldTreeModel(List<T> list) {
			super(list);
		}

		@Override
		public Object getChild(Object parent, int index) {
			if (parent instanceof List<?>) {
				List<?> fieldList = (List<?>) parent;
				return fieldList.get(index);
			} else if (parent instanceof FieldValue) {
				FieldValue fieldValue = (FieldValue) parent;
				return fieldValue.getValueArray()[index];
			}
			return null;
		}

		@Override
		public int getChildCount(Object parent) {
			if (parent instanceof List<?>) {
				List<?> fieldList = (List<?>) parent;
				return fieldList.size();
			} else if (parent instanceof FieldValue) {
				FieldValue fieldValue = (FieldValue) parent;
				return fieldValue.getValuesCount();
			}
			return 0;
		}

		@Override
		public boolean isLeaf(Object node) {
			return node instanceof String;
		}
	}

	private transient List<Document> documents;

	@Override
	protected void reset() throws SearchLibException {
		documents = null;
	}

	@Override
	public void reloadPage() throws SearchLibException {
		synchronized (this) {
			documents = null;
			super.reloadPage();
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
			int pos = result.getRequestStart();
			documents = new ArrayList<Document>(docCount);
			int end = pos + docCount;
			while (pos < end)
				documents.add(new Document(pos++));
			return documents;
		}
	}

	public void explainScore(Component comp) throws SearchLibException,
			InterruptedException, IOException, ParseException, SyntaxError {
		Client client = getClient();
		if (client == null)
			return;
		ResultDocumentsInterface<?> result = getResultDocuments();
		if (result == null)
			return;
		Document document = (Document) comp.getAttribute("document");
		if (document == null)
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

	private void renderField(Treerow treerow, FieldValue fieldValue) {
		new Treecell(fieldValue.getLabel()).setParent(treerow);
		Treecell treecell;
		if (fieldValue.getValuesCount() > 0)
			treecell = new Treecell(fieldValue.getValueArray()[0].getValue());
		else
			treecell = new Treecell();
		treecell.setParent(treerow);
	}

	@Override
	public void render(Treeitem item, Object data) throws Exception {
		Treerow treerow = new Treerow();
		if (data instanceof String)
			renderValue(treerow, (String) data);
		else if (data instanceof FieldValue)
			renderField(treerow, (FieldValue) data);
		else if (data instanceof FieldValueItem)
			renderValue(treerow, (FieldValueItem) data);
		treerow.setParent(item);
	}

}
