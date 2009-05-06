/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
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

package com.jaeksoft.searchlib.web.controller.basket;

import java.util.Map;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Panelchildren;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.basket.BasketDocument;
import com.jaeksoft.searchlib.basket.BasketKey;
import com.jaeksoft.searchlib.web.controller.CommonController;
import com.jaeksoft.searchlib.web.model.FieldContentModel;

public class ListController extends CommonController {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4651293740989742211L;

	private BasketDocument basketDocument;

	private Panelchildren basketComponent;

	public ListController() throws SearchLibException {
		super();
		basketDocument = null;
		basketComponent = null;
	}

	public BasketDocument getCurrentBasketDocument() {
		synchronized (this) {
			return basketDocument;
		}
	}

	public boolean isCurrentBasketDocumentValid() {
		synchronized (this) {
			return basketDocument != null;
		}
	}

	private void removeBasketComponent() {
		synchronized (this) {
			if (basketComponent == null)
				return;
			getFellow("basketDocumentPanel").removeChild(basketComponent);
			basketComponent = null;
		}
	}

	private void setBasketComponent() {
		synchronized (this) {
			removeBasketComponent();
			if (basketDocument == null)
				return;
			basketComponent = FieldContentModel
					.createIndexDocumentComponent(basketDocument
							.getFieldContentArray());
			Component parent = getFellow("basketDocumentPanel");
			basketComponent.setParent(parent);
		}
	}

	public void setCurrentBasketDocument(
			Map.Entry<BasketDocument, BasketDocument> entry) {
		synchronized (this) {
			basketDocument = entry == null ? null : entry.getValue();
			setBasketComponent();
			reloadPage();
		}
	}

	public void onRemoveDocument() throws SearchLibException {
		synchronized (this) {
			if (basketDocument == null)
				return;
			getClient().getBasketCache().remove(new BasketKey(basketDocument));
			setCurrentBasketDocument(null);
			reloadPage();
		}
	}
}
