package net.gisiinteractive.gipublishweb.controller;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.event.ActionEvent;

import net.gisiinteractive.gipublish.controller.ArticleManager;
import net.gisiinteractive.gipublish.controller.session.filters.DocumentArticleFilter;
import net.gisiinteractive.gipublish.model.Document;
import net.gisiinteractive.gipublishweb.controller.common.CommonJsfShortcuts;

import com.jaeksoft.searchlib.SearchLibException;

public class DocumentInsert extends CommonJsfShortcuts {

	private static final int MAX_TO_INSERT_IN_A_ROW = 10000;
	public static final String DOCUMENT_INSERT = "documentInsert";

	public static String DESCRIPTION_EXAMPLE = "   Description is one of four rhetorical modes (also known as modes of discourse), along with exposition, argumentation, and narration."
			+ " Each of the rhetorical modes is present in a variety of forms and each has its own purpose and conventions."
			+ " Description is also the fiction-writing mode for transmitting a mental image of the particulars of a story.";

	public static DocumentInsert getInstance() {
		return getThroughEl(DOCUMENT_INSERT, DocumentInsert.class);
	}

	public ArticleManager getArticleManager() {
		return ArticleManager.getInstance();
	}

	private Document newDoc = new Document();
	private int numberToInsert = 500;

	public Document getNewDoc() {
		return newDoc;
	}

	public void setNewDoc(Document newDoc) {
		this.newDoc = newDoc;
	}

	public int getNumberToInsert() {
		return numberToInsert;
	}

	public void setNumberToInsert(int numberToInsert) {
		this.numberToInsert = numberToInsert;
	}

	public void insertLstr(ActionEvent actionEvent) {
		if (newDoc == null)
			return;

		if (newDoc.getId() == null || newDoc.getDescription() == null)
			return;

		try {
			getArticleManager().indexDocument(newDoc);
			getArticleManager().reloadIndex();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SearchLibException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		newDoc = new Document();
		DocumentList.getInstance().resetDocumentsLstr(actionEvent);

	}

	public void insertDefaultContentLstr(ActionEvent actionEvent) {
		if (getNumberToInsert() > 0
				&& getNumberToInsert() <= MAX_TO_INSERT_IN_A_ROW) {

			Long firstIdValue = getMaxIdFRomIndex();

			List<Document> listToInsert = new ArrayList<Document>();
			Document cur = null;
			for (int i = 0; i < getNumberToInsert(); i++) {
				firstIdValue++;

				cur = new Document();
				cur.setId(firstIdValue);
				cur.setTitle("Title : " + firstIdValue);
				cur.setDescription("Description : " + firstIdValue
						+ DESCRIPTION_EXAMPLE);
				listToInsert.add(cur);
			}

			try {
				getArticleManager().indexDocuments(listToInsert);
				getArticleManager().reloadIndex();
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SearchLibException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			newDoc = new Document();
			DocumentList.getInstance().resetDocumentsLstr(actionEvent);
		}
	}

	/**
	 * Retrieves max Id that index contains
	 * 
	 * @return
	 */
	public Long getMaxIdFRomIndex() {
		DocumentArticleFilter myFilter = new DocumentArticleFilter();
		myFilter.setRowCount(1);
		List<Document> oneList = getArticleManager().getDocuments(
				DocumentList.getInstance(), myFilter).getList();
		if (oneList != null && oneList.size() == 1) {
			Document maxDoc = oneList.iterator().next();
			return maxDoc.getId();
		}

		return new Long(0);
	}
}
