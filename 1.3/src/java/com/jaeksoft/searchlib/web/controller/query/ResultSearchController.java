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
import java.io.PrintWriter;

import org.zkoss.zul.Filedownload;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.facet.Facet;
import com.jaeksoft.searchlib.facet.FacetList;
import com.jaeksoft.searchlib.render.RenderCSV;
import com.jaeksoft.searchlib.result.AbstractResultSearch;

public class ResultSearchController extends AbstractQueryController {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3462760563129892850L;

	private transient Facet selectedFacet;

	public ResultSearchController() throws SearchLibException {
		super();
	}

	@Override
	protected void reset() throws SearchLibException {
		selectedFacet = null;
	}

	public boolean getDocumentFound() throws SearchLibException {
		synchronized (this) {
			AbstractResultSearch result = (AbstractResultSearch) getResult();
			if (result == null)
				return false;
			return result.getDocumentCount() > 0;
		}
	}

	public FacetList getFacetList() throws SearchLibException {
		synchronized (this) {
			AbstractResultSearch result = (AbstractResultSearch) getResult();
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

	public boolean isFacetValid() throws SearchLibException {
		synchronized (this) {
			return getFacetList() != null;
		}
	}

	public void exportSearchResultToCsv() throws Exception {
		Client client = getClient();
		if (client == null)
			return;
		AbstractResultSearch result = (AbstractResultSearch) getResult();
		if (result == null)
			return;

		PrintWriter pw = null;
		try {
			File tempFile = File.createTempFile("OSS_Search_Result", ".csv");
			pw = new PrintWriter(tempFile);
			new RenderCSV(result).render(pw);
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
	public void reloadPage() throws SearchLibException {
		synchronized (this) {
			selectedFacet = null;
			super.reloadPage();
		}
	}

}
