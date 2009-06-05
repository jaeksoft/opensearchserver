package net.gisiinteractive.gipublishweb.controller;

import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.event.ActionEvent;

import net.gisiinteractive.gipublish.common.indexing.IndexedDocument;
import net.gisiinteractive.gipublish.common.indexing.IndexingDataModel;
import net.gisiinteractive.gipublish.controller.ArticleManager;
import net.gisiinteractive.gipublish.controller.session.filters.DocumentArticleFilter;
import net.gisiinteractive.gipublish.model.Document;
import net.gisiinteractive.gipublishweb.controller.common.CommonJsfShortcuts;

import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.result.ResultDocument;

public class DocumentList extends CommonJsfShortcuts implements
		IndexedDocument<Document> {

	public static final String DOCUMENT_LIST = "documentList";
	public static final String DEFAULT = "default";
	private int currentPage = 1;

	private DocumentArticleFilter filter = new DocumentArticleFilter();

	private IndexingDataModel<Document> currentDocuments;

	public static DocumentList getInstance() {
		return getThroughEl(DOCUMENT_LIST, DocumentList.class);
	}

	public ArticleManager getArticleManager() {
		return ArticleManager.getInstance();
	}

	public IndexingDataModel<Document> getDocuments() {
		if (currentDocuments != null)
			return currentDocuments;

		currentDocuments = getArticleManager().getDocuments(this, getFilter());

		if (currentDocuments == null)
			return null;

		return currentDocuments;
	}

	public int getDocumentCount() {
		if (currentDocuments == null)
			return 0;
		return currentDocuments.getRowCount();
	}

	public DocumentArticleFilter getFilter() {
		return this.filter;
	}

	public void setFilter(DocumentArticleFilter f) {
		this.filter = f;
	}

	@Override
	public Document createIndexedDocument(ResultDocument idxDoc) {
		Document doc = getArticleManager().createIndexedDocument(idxDoc);

		return doc;
	}

	@Override
	public IndexDocument createIndexingDocument(Document document) {
		return getArticleManager().createIndexingDocument(document);
	}

	public IndexingDataModel<Document> getCurrentDocuments() {
		return currentDocuments;
	}

	public void setCurrentDocuments(IndexingDataModel<Document> currentDocuments) {
		this.currentDocuments = currentDocuments;
	}

	public void updateFilterLstr(ActionEvent actionEvent) {
		currentDocuments = null;
		currentPage = 1;
	}

	public void clearFilterColumnsLstr(ActionEvent actionEvent) {
		int rowCountSave = filter.getRowCount();
		filter = new DocumentArticleFilter();
		filter.setRowCount(rowCountSave);

		currentDocuments = null;
		currentPage = 1;
	}

	public void resetDocumentsLstr(ActionEvent ev) {
		currentDocuments = null;
		currentPage = 1;
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

	public void deleteLstr(ActionEvent actionEvent) {
		UIComponent component = actionEvent.getComponent();
		while (!(component instanceof UIData)) {
			component = component.getParent();
		}
		UIData table = (UIData) component;
		Document current = null;

		if (table.getRowData() instanceof Document) {
			current = (Document) table.getRowData();
		}

		if (current != null) {
			ArticleManager.getInstance().deleteFromIndex(current);
			this.resetDocumentsLstr(actionEvent);
		}
	}

	public void deleteAllLstr(ActionEvent actionEvent) {
		ArticleManager.getInstance().deleteAllFromIndex();
		this.resetDocumentsLstr(actionEvent);
	}

}
