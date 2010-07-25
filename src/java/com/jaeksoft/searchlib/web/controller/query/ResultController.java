/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2010 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.web.controller.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.AbstractTreeModel;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.TreeModel;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.TreeitemRenderer;
import org.zkoss.zul.Treerow;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.facet.Facet;
import com.jaeksoft.searchlib.facet.FacetList;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.result.Result;
import com.jaeksoft.searchlib.result.ResultDocument;
import com.jaeksoft.searchlib.schema.FieldList;
import com.jaeksoft.searchlib.schema.FieldValue;
import com.jaeksoft.searchlib.snippet.SnippetFieldValue;
import com.jaeksoft.searchlib.spellcheck.SpellCheck;
import com.jaeksoft.searchlib.spellcheck.SpellCheckList;

public class ResultController extends QueryController implements
		TreeitemRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3462760563129892850L;

	private List<Document> documents;

	private Facet selectedFacet;

	public ResultController() throws SearchLibException {
		super();
	}

	@Override
	protected void reset() throws SearchLibException {
		super.reset();
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
			return getResult().getScore(pos);
		}

		public int getCollapseCount() {
			return getResult().getCollapseCount(pos);
		}

		public int getDocId() {
			return getResult().getDocs()[pos].doc;
		}

		public ResultDocument getResultDocument() throws CorruptIndexException,
				IOException, ParseException, SyntaxError {
			return getResult().getDocument(pos);
		}

		public boolean isReturnValid() throws CorruptIndexException,
				IOException, ParseException, SyntaxError {
			return getResultDocument().getReturnFields().size() > 0;
		}

		public boolean isSnippetValid() throws CorruptIndexException,
				IOException, ParseException, SyntaxError {
			return getResultDocument().getSnippetFields().size() > 0;
		}

		public String getReturnPercent() throws CorruptIndexException,
				IOException, ParseException, SyntaxError {
			if (!isReturnValid())
				return "0%";
			if (!isSnippetValid())
				return "100%";
			return "50%";
		}

		public String getSnippetPercent() throws CorruptIndexException,
				IOException, ParseException, SyntaxError {
			if (!isSnippetValid())
				return "0%";
			if (!isReturnValid())
				return "100%";
			return "50%";
		}

		public TreeModel getReturnTree() throws CorruptIndexException,
				IOException, ParseException, SyntaxError {
			return new FieldTreeModel<FieldValue>(getResultDocument()
					.getReturnFields());
		}

		public TreeModel getSnippetTree() throws CorruptIndexException,
				IOException, ParseException, SyntaxError {
			return new FieldTreeModel<SnippetFieldValue>(getResultDocument()
					.getSnippetFields());
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

	public List<Document> getDocuments() {
		synchronized (this) {
			if (documents != null)
				return documents;
			Result result = getResult();
			if (result == null)
				return null;
			int i = result.getDocumentCount();
			if (i <= 0)
				return null;
			documents = new ArrayList<Document>(i);
			int pos = result.getSearchRequest().getStart();
			int end = pos + result.getDocumentCount();
			while (pos < end)
				documents.add(new Document(pos++));
			return documents;
		}
	}

	public boolean getDocumentFound() {
		synchronized (this) {
			Result result = getResult();
			if (result == null)
				return false;
			return result.getDocumentCount() > 0;
		}
	}

	public FacetList getFacetList() {
		synchronized (this) {
			Result result = getResult();
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

	public List<SpellCheck> getSpellCheckList() {
		synchronized (this) {
			Result result = getResult();
			if (result == null)
				return null;
			SpellCheckList spellChecklist = result.getSpellCheckList();
			if (spellChecklist == null)
				return null;
			return spellChecklist.getList();
		}
	}

	public void explainScore(Component comp) throws SearchLibException,
			InterruptedException, IOException, ParseException, SyntaxError {
		Client client = getClient();
		if (client == null)
			return;
		Result result = getResult();
		if (result == null)
			return;
		Document document = (Document) comp.getAttribute("document");
		if (document == null)
			return;
		int docId = document.getDocId();
		String explanation = client.explain(result.getSearchRequest(), docId);
		Filedownload.save(explanation, "text/plain; charset-UTF-8",
				"Score explanation " + docId + ".txt");
	}

	public boolean isSpellCheckValid() {
		synchronized (this) {
			return getSpellCheckList() != null;
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

	private void renderField(Treerow treerow, FieldValue fieldValue) {
		new Treecell(fieldValue.toString()).setParent(treerow);
		Treecell treecell;
		if (fieldValue.getValuesCount() > 0)
			treecell = new Treecell(fieldValue.getValueArray()[0]);
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
		treerow.setParent(item);
	}

}
