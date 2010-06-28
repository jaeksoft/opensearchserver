/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.web.controller.schema;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.Image;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.FieldMap;
import com.jaeksoft.searchlib.parser.ParserFactory;
import com.jaeksoft.searchlib.parser.ParserFieldEnum;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.util.map.GenericLink;
import com.jaeksoft.searchlib.util.map.Target;
import com.jaeksoft.searchlib.web.controller.CommonController;

public class ParserListController extends CommonController implements
		ListitemRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5149273791608440407L;

	private ParserFactory selectedParser;

	private SchemaField selectedIndexField;

	private ParserFieldEnum selectedParserField;

	public ParserListController() throws SearchLibException {
		super();
	}

	@Override
	public void reset() {
		selectedParser = null;
		selectedIndexField = null;
		selectedParserField = null;
	}

	public Set<ParserFactory> getParserSet() {
		try {
			Client client = getClient();
			if (client == null)
				return null;
			return client.getParserSelector().getParserFactorySet();
		} catch (SearchLibException e) {
			throw new RuntimeException(e);
		}
	}

	public void setSelectedParser(ParserFactory parser) {
		selectedParser = parser;
		reloadPage();
	}

	public ParserFactory getSelectedParser() {
		if (selectedParser == null) {
			Set<ParserFactory> parserSet = getParserSet();
			if (parserSet != null)
				if (parserSet.size() > 0)
					selectedParser = parserSet.iterator().next();
		}
		return selectedParser;
	}

	public boolean isParserSelected() {
		return selectedParser != null;
	}

	public boolean isParserExtension() {
		if (selectedParser == null)
			return false;
		Set<String> extensionSet = selectedParser.getExtensionSet();
		if (extensionSet == null)
			return false;
		return extensionSet.size() > 0;
	}

	public boolean isParserMimeType() {
		if (selectedParser == null)
			return false;
		Set<String> mimeTypeSet = selectedParser.getMimeTypeSet();
		if (mimeTypeSet == null)
			return false;
		return mimeTypeSet.size() > 0;
	}

	public List<SchemaField> getIndexFieldList() throws SearchLibException {
		synchronized (this) {
			Client client = getClient();
			if (client == null)
				return null;
			List<SchemaField> list = client.getSchema().getFieldList()
					.getList();
			if (list.size() > 0 && selectedIndexField == null)
				selectedIndexField = list.get(0);
			return list;
		}
	}

	public ParserFieldEnum[] getParserFieldList()
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException, SearchLibException {
		synchronized (this) {
			if (selectedParser == null)
				return null;
			ParserFieldEnum[] parserFieldList = selectedParser.getNewParser()
					.getParserFieldList();
			if (parserFieldList == null)
				return null;
			if (selectedParserField == null && parserFieldList.length > 0)
				selectedParserField = parserFieldList[0];
			return parserFieldList;
		}
	}

	public void setSelectedParserField(ParserFieldEnum parserField) {
		synchronized (this) {
			selectedParserField = parserField;
		}
	}

	public ParserFieldEnum getSelectedParserField() {
		synchronized (this) {
			return selectedParserField;
		}
	}

	public void setSelectedIndexField(SchemaField field) {
		synchronized (this) {
			selectedIndexField = field;
		}
	}

	public SchemaField getSelectedIndexField() {
		synchronized (this) {
			return selectedIndexField;
		}
	}

	public FieldMap getFieldMap() {
		if (selectedParser == null)
			return null;
		return selectedParser.getFieldMap();
	}

	public void onAdd() throws SearchLibException,
			TransformerConfigurationException, SAXException, IOException,
			XPathExpressionException, ParserConfigurationException {
		if (selectedParserField == null || selectedIndexField == null)
			return;
		FieldMap fieldMap = getFieldMap();
		fieldMap.add(selectedParserField.name(), new Target(selectedIndexField
				.getName()));
		getClient().saveParsers();
		reloadPage();
	}

	@SuppressWarnings("unchecked")
	public void onLinkRemove(Event event) throws SearchLibException,
			TransformerConfigurationException, SAXException, IOException,
			XPathExpressionException, ParserConfigurationException {
		GenericLink<String, Target> link = (GenericLink<String, Target>) event
				.getData();
		FieldMap fieldMap = getFieldMap();
		fieldMap.remove(link);
		getClient().saveParsers();
		reloadPage();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void render(Listitem item, Object data) throws Exception {
		GenericLink<String, Target> link = (GenericLink<String, Target>) data;
		new Listcell(link.getSource()).setParent(item);
		new Listcell(link.getTarget().getName()).setParent(item);
		Listcell listcell = new Listcell();
		Image image = new Image("/images/action_delete.png");
		image.addForward(null, this, "onLinkRemove", data);
		image.setParent(listcell);
		listcell.setParent(item);
	}

}
