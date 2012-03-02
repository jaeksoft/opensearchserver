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

package com.jaeksoft.searchlib.web.controller.query;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.AbstractTreeModel;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Html;
import org.zkoss.zul.TreeModel;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.TreeitemRenderer;
import org.zkoss.zul.Treerow;
import org.zkoss.zul.api.Window;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.facet.Facet;
import com.jaeksoft.searchlib.facet.FacetList;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.render.RenderCSV;
import com.jaeksoft.searchlib.request.RequestTypeEnum;
import com.jaeksoft.searchlib.result.AbstractResultSearch;
import com.jaeksoft.searchlib.result.ResultDocument;
import com.jaeksoft.searchlib.schema.FieldList;
import com.jaeksoft.searchlib.schema.FieldValue;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.snippet.SnippetFieldValue;

public class ResultSearchController extends AbstractQueryController implements
		TreeitemRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3462760563129892850L;

	private transient List<Document> documents;

	private transient Facet selectedFacet;

	public ResultSearchController() throws SearchLibException {
		super();
	}

	@Override
	protected void reset() throws SearchLibException {
		selectedFacet = null;
		documents = null;
	}

	public class Document {
		int pos;
		StringBuffer title;

		private Document(int pos) {
			this.pos = pos;
			title = null;
		}

		public String getTitle() {
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

		public float getScore() {
			AbstractResultSearch result = getResult();
			if (result == null)
				return 0;
			return result.getScore(pos);
		}

		public int getCollapseCount() {
			AbstractResultSearch result = getResult();
			if (result == null)
				return 0;
			return result.getCollapseCount(pos);
		}

		public int getDocId() {
			AbstractResultSearch result = getResult();
			if (result == null)
				return 0;
			return result.getDocs()[pos].doc;
		}

		public ResultDocument getResultDocument() throws IOException,
				ParseException, SyntaxError {
			AbstractResultSearch result = getResult();
			if (result == null)
				return null;
			return result.getDocument(pos);
		}

		public boolean isReturnValid() throws IOException, ParseException,
				SyntaxError {
			ResultDocument resultDocument = getResultDocument();
			if (resultDocument == null)
				return false;
			return resultDocument.getReturnFields().size() > 0;
		}

		public boolean isSnippetValid() throws IOException, ParseException,
				SyntaxError {
			ResultDocument resultDocument = getResultDocument();
			if (resultDocument == null)
				return false;
			return resultDocument.getSnippetFields().size() > 0;
		}

		public String getReturnPercent() throws IOException, ParseException,
				SyntaxError {
			if (!isReturnValid())
				return "0%";
			if (!isSnippetValid())
				return "100%";
			return "50%";
		}

		public String getSnippetPercent() throws IOException, ParseException,
				SyntaxError {
			if (!isSnippetValid())
				return "0%";
			if (!isReturnValid())
				return "100%";
			return "50%";
		}

		public TreeModel getReturnTree() throws IOException, ParseException,
				SyntaxError {
			ResultDocument resultDocument = getResultDocument();
			if (resultDocument == null)
				return null;
			return new FieldTreeModel<FieldValue>(
					resultDocument.getReturnFields());
		}

		public TreeModel getSnippetTree() throws IOException, ParseException,
				SyntaxError {
			ResultDocument resultDocument = getResultDocument();
			if (resultDocument == null)
				return null;
			return new FieldTreeModel<SnippetFieldValue>(
					resultDocument.getSnippetFields());
		}
	}

	public class FieldTreeModel<T extends FieldValue> extends AbstractTreeModel {

		/**
		 * 
		 */
		private static final long serialVersionUID = -1438357359217617272L;

		public FieldTreeModel(FieldList<T> fieldList) {
			super(fieldList);
		}

		@Override
		public Object getChild(Object parent, int index) {
			if (parent instanceof FieldList<?>) {
				FieldList<?> fieldList = (FieldList<?>) parent;
				return fieldList.get(index);
			} else if (parent instanceof FieldValue) {
				FieldValue fieldValue = (FieldValue) parent;
				return fieldValue.getValueArray()[index];
			}
			return null;
		}

		@Override
		public int getChildCount(Object parent) {
			if (parent instanceof FieldList<?>) {
				FieldList<?> fieldList = (FieldList<?>) parent;
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

	public AbstractResultSearch getResult() {
		return (AbstractResultSearch) getResult(RequestTypeEnum.SearchRequest);
	}

	public List<Document> getDocuments() {
		synchronized (this) {
			if (documents != null)
				return documents;
			AbstractResultSearch result = getResult();
			if (result == null)
				return null;
			int i = result.getDocumentCount();
			if (i <= 0)
				return null;
			documents = new ArrayList<Document>(i);
			int pos = result.getRequest().getStart();
			int end = pos + result.getDocumentCount();
			while (pos < end)
				documents.add(new Document(pos++));
			return documents;
		}
	}

	public boolean getDocumentFound() {
		synchronized (this) {
			AbstractResultSearch result = getResult();
			if (result == null)
				return false;
			return result.getDocumentCount() > 0;
		}
	}

	public FacetList getFacetList() {
		synchronized (this) {
			AbstractResultSearch result = getResult();
			if (result == null)
				return null;
			FacetList facetList = result.getFacetList();
			if (facetList == null)
				return null;
			if (facetList.getList().size() > 0)
				if (selectedFacet == null)
					selectedFacet = facetList.getList().get(0);
			return result.getFacetList();
		}
	}

	public boolean isFacetValid() {
		synchronized (this) {
			return getFacetList() != null;
		}
	}

	public void explainScore(Component comp) throws SearchLibException,
			InterruptedException, IOException, ParseException, SyntaxError {
		Client client = getClient();
		if (client == null)
			return;
		AbstractResultSearch result = getResult();
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

	public void exportSearchResultToCsv() throws Exception {
		Client client = getClient();
		if (client == null)
			return;
		AbstractResultSearch result = getResult();
		if (result == null)
			return;

		PrintWriter pw = null;
		try {
			File tempFile = File.createTempFile("OSS_Search_Result", ".csv");
			pw = new PrintWriter(tempFile);
			new RenderCSV(result).renderCSV(pw);
			Filedownload.save(new FileInputStream(tempFile),
					"text/csv; charset-UTF-8", "OSS_Search_Result.csv");
		} finally {
			if (pw != null)
				pw.close();
		}
	}

	public void setSelectedFacet(Facet facet) {
		synchronized (this) {
			selectedFacet = facet;
		}
	}

	public Facet getSelectedFacet() {
		synchronized (this) {
			return selectedFacet;
		}
	}

	@Override
	public void reloadPage() {
		synchronized (this) {
			documents = null;
			selectedFacet = null;
			super.reloadPage();
		}
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
		new Treecell(fieldValue.toString()).setParent(treerow);
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
