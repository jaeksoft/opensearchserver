/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft SearchLib Community.
 *
 * Jaeksoft SearchLib Community is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft SearchLib Community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft SearchLib Community. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.web.controller.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import org.zkoss.zul.AbstractTreeModel;
import org.zkoss.zul.TreeModel;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.TreeitemRenderer;
import org.zkoss.zul.Treerow;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.highlight.HighlightFieldValue;
import com.jaeksoft.searchlib.result.Result;
import com.jaeksoft.searchlib.result.ResultDocument;
import com.jaeksoft.searchlib.schema.FieldList;
import com.jaeksoft.searchlib.schema.FieldValue;

public class ResultController extends QueryController implements
		TreeitemRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3462760563129892850L;

	private List<Document> documents;

	private Result result;

	public ResultController() throws SearchLibException {
		super();
		documents = null;
		result = null;
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
				title.append("Position: ");
				title.append(getPos());
				title.append(" - ");
				title.append("Score: ");
				title.append(getScore());
				title.append(" - ");
				title.append("Collapsed: ");
				title.append(getCollapseCount());
				return title.toString();
			}
		}

		public int getPos() {
			return pos;
		}

		public float getScore() {
			return result.getScore(pos);
		}

		public int getCollapseCount() {
			return result.getCollapseCount(pos);
		}

		public ResultDocument getResultDocument() throws CorruptIndexException,
				IOException, ParseException, SyntaxError {
			return result.getDocument(pos);
		}

		public TreeModel getReturnTree() throws CorruptIndexException,
				IOException, ParseException, SyntaxError {
			return new FieldTreeModel<FieldValue>(getResultDocument()
					.getReturnFields());
		}

		public TreeModel getHighlightTree() throws CorruptIndexException,
				IOException, ParseException, SyntaxError {
			return new FieldTreeModel<HighlightFieldValue>(getResultDocument()
					.getHighlightFields());
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
			if (parent instanceof FieldList) {
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
			if (parent instanceof FieldList) {
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
			result = getResult();
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

	@Override
	protected void reloadPage() {
		synchronized (this) {
			documents = null;
			result = null;
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
