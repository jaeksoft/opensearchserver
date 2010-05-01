/**
 * 
 */
package net.gisiinteractive.gipublish.controller.session.filters;

import net.gisiinteractive.gipublish.model.Document;

public class DocumentArticleFilter extends Document implements Cloneable {
	private static final long serialVersionUID = 1L;
	protected String fullTextSearch;
	private int textSearchType = 0;
	private int rowCount = 10;

	public DocumentArticleFilter() {
	}

	public DocumentArticleFilter(String fullTextSearch) {
		this.fullTextSearch = fullTextSearch;
	}

	public String getFullTextSearch() {
		return fullTextSearch;
	}

	public void setFullTextSearch(String fullTextSearch) {
		this.fullTextSearch = fullTextSearch;
	}

	public Integer getTextSearchType() {
		return textSearchType;
	}

	public void setTextSearchType(Integer i) {
		textSearchType = i;
	}

	public void setRowCount(int rowCount) {
		this.rowCount = rowCount;
	}

	public int getRowCount() {
		return rowCount;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

}