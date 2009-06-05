package net.gisiinteractive.gipublish.controller;

import static net.gisiinteractive.gipublish.common.indexing.IndexingFormatter.getLongValue;
import static net.gisiinteractive.gipublish.common.indexing.IndexingFormatter.getStringValue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import net.gisiinteractive.common.exceptions.BusinessException;
import net.gisiinteractive.common.exceptions.SystemException;
import net.gisiinteractive.gipublish.common.indexing.DocumentDeleteRequest;
import net.gisiinteractive.gipublish.common.indexing.DocumentSearchRequest;
import net.gisiinteractive.gipublish.common.indexing.IndexRequest;
import net.gisiinteractive.gipublish.common.indexing.IndexedDocument;
import net.gisiinteractive.gipublish.common.indexing.IndexingDataModel;
import net.gisiinteractive.gipublish.common.indexing.IndexingFormatter;
import net.gisiinteractive.gipublish.controller.session.filters.DocumentArticleFilter;
import net.gisiinteractive.gipublish.model.Document;
import net.gisiinteractive.gipublishweb.controller.common.CommonJsfShortcuts;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.result.ResultDocument;

public class ArticleManager implements IndexedDocument<Document> {
	private static final String BOSEARCH = "search";
	private static Locale LANGUAGE = Locale.FRENCH;

	private static ArticleManager instance;
	private Client documentListIndex;

	public static ArticleManager getInstance() {
		if (instance == null)
			instance = new ArticleManager();
		return instance;
	}

	private ArticleManager() {
		if (CommonJsfShortcuts.getServerRoot() != null) {
			File confFile = new File(CommonJsfShortcuts.getServerRoot(),
					"/META-INF/" + "/indexDocumentList.xml");

			try {
				documentListIndex = new Client(confFile);
			} catch (SearchLibException e) {
				System.err.println("No Index Document Found");
				throw new BusinessException(e.getMessage());
			}
		}
	}

	/**
	 * Retrieve documents
	 * 
	 * @param idxDoc
	 * @param filter
	 * @return
	 */
	public IndexingDataModel<Document> getDocuments(
			IndexedDocument<Document> idxDoc, DocumentArticleFilter filter) {

		if (documentListIndex == null)
			return null;

		IndexRequest request = null;
		String fullTextSearch = filter.getFullTextSearch();
		try {
			IndexDocument recherche = createIndexingDocument(filter);

			request = new DocumentSearchRequest(documentListIndex, recherche,
					BOSEARCH, fullTextSearch);

			return new IndexingDataModel<Document>(idxDoc, documentListIndex,
					request.getRequest(filter.getRowCount()));

		} catch (Throwable ex) {
			throw new SystemException(ex);
		}
	}

	/**
	 * Indexing Mode
	 */
	public IndexDocument createIndexingDocument(Document article) {
		IndexDocument idxDoc = new IndexDocument(LANGUAGE);

		if (article == null || idxDoc == null)
			return null;

		Long id = article.getId();
		if (id == null)
			return null;

		IndexingFormatter.add(idxDoc, "id", id);
		IndexingFormatter.add(idxDoc, "title", article.getTitle());
		IndexingFormatter.add(idxDoc, "description", article.getDescription());

		return idxDoc;
	}

	public Document createIndexedDocument(ResultDocument idxDoc) {
		Document article = new Document();

		article.setId(getLongValue(idxDoc, "id"));
		article.setTitle(getStringValue(idxDoc, "title"));
		article.setDescription(getStringValue(idxDoc, "description"));

		return article;
	}

	/**
	 * Index one document
	 * 
	 * @param article
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public void indexDocument(Document article)
			throws NoSuchAlgorithmException, IOException, URISyntaxException {

		if (documentListIndex == null)
			return;

		IndexDocument idxDoc = createIndexingDocument(article);
		documentListIndex.updateDocument(idxDoc);
	}

	/**
	 * Index a list of documents
	 * 
	 * @param lst
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public void indexDocuments(List<Document> lst)
			throws NoSuchAlgorithmException, IOException, URISyntaxException {

		if (documentListIndex == null)
			return;

		List<IndexDocument> lstIndexDocument = new ArrayList<IndexDocument>();
		Document article = null;

		for (Iterator<Document> iterator = lst.iterator(); iterator.hasNext();) {
			article = (Document) iterator.next();
			lstIndexDocument.add(createIndexingDocument(article));

		}

		documentListIndex.updateDocuments(lstIndexDocument);
	}

	/**
	 * Reload index (as a sql commit).
	 */
	public void reloadIndex() {

		if (documentListIndex == null)
			return;

		try {
			documentListIndex.reload(null);
		} catch (IOException ex) {
			throw new SystemException("Unable to reload index", ex);
		} catch (URISyntaxException e) {
			throw new SystemException("Unable to find correct Index URL", e);
		}
	}

	public void deleteFromIndex(Document doc) {
		if (documentListIndex == null)
			return;
		try {
			IndexRequest request = new DocumentDeleteRequest(documentListIndex,
					createIndexingDocument(doc), BOSEARCH);
			documentListIndex.search(request.getRequest(0));
			reloadIndex();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void deleteAllFromIndex() {
		if (documentListIndex == null)
			return;
		try {
			IndexRequest request = new DocumentDeleteRequest(documentListIndex,
					createIndexingDocument(null), BOSEARCH, true);
			documentListIndex.search(request.getRequest(0));
			reloadIndex();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
