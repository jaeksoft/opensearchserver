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

package com.jaeksoft.searchlib.web.controller.schema;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.Image;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Messagebox;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.parser.ParserFactory;
import com.jaeksoft.searchlib.parser.ParserFieldEnum;
import com.jaeksoft.searchlib.parser.ParserFieldMap;
import com.jaeksoft.searchlib.parser.ParserFieldTarget;
import com.jaeksoft.searchlib.parser.ParserSelector;
import com.jaeksoft.searchlib.parser.ParserType;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.util.map.GenericLink;
import com.jaeksoft.searchlib.util.map.SourceField;
import com.jaeksoft.searchlib.web.controller.AlertController;
import com.jaeksoft.searchlib.web.controller.CommonController;

public class ParserController extends CommonController implements
		ListitemRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5149273791608440407L;

	private transient ParserFactory selectedParser;

	private transient ParserFactory currentParser;

	private transient SchemaField selectedIndexField;

	private transient ParserFieldEnum selectedParserField;

	private transient ParserType parserType;

	private transient String currentExtension;

	private transient String currentMimeType;

	private transient String currentUrlPattern;

	private transient String captureRegexp;

	private transient boolean removeTag;

	private class DeleteAlert extends AlertController {

		private ParserFactory deleteParser;

		protected DeleteAlert(ParserFactory deleteParser)
				throws InterruptedException {
			super("Please, confirm that you want to delete the parser: "
					+ deleteParser.getParserName(), Messagebox.YES
					| Messagebox.NO, Messagebox.QUESTION);
			this.deleteParser = deleteParser;
		}

		@Override
		protected void onYes() throws SearchLibException {
			Client client = getClient();
			client.getParserSelector().replaceParserFactory(deleteParser, null);
			client.saveParsers();
			onCancel();
		}
	}

	public ParserController() throws SearchLibException {
		super();
	}

	@Override
	protected void reset() {
		currentParser = null;
		selectedParser = null;
		selectedIndexField = null;
		selectedParserField = null;
		captureRegexp = null;
	}

	public ParserFactory[] getParsers() {
		try {
			Client client = getClient();
			if (client == null)
				return null;
			return client.getParserSelector().getParserFactoryArray();
		} catch (SearchLibException e) {
			throw new RuntimeException(e);
		}
	}

	private Set<String> getParserSet(ParserFactory omit)
			throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		ParserSelector parserSelector = client.getParserSelector();
		if (parserSelector == null)
			return null;
		ParserFactory[] parserFactoryArray = parserSelector
				.getParserFactoryArray();
		if (parserFactoryArray == null)
			return null;
		Set<String> list = new TreeSet<String>();
		for (ParserFactory parserFactory : parserFactoryArray) {
			if (omit != null && omit.equals(parserFactory))
				continue;
			list.add(parserFactory.getParserName());
		}
		return list;

	}

	public Set<String> getParserSet() throws SearchLibException {
		return getParserSet(null);
	}

	public Set<String> getFailOverList() throws SearchLibException {
		return getParserSet(selectedParser);
	}

	public ParserFactory getCurrentParser() {
		return currentParser;
	}

	public ParserType getSelectedParserType() throws SearchLibException {
		if (parserType == null)
			parserType = getParserTypeList().get(0);
		return parserType;
	}

	public void setSelectedParserType(ParserType parserType) {
		this.parserType = parserType;
	}

	public List<ParserType> getParserTypeList() throws SearchLibException {
		return getClient().getParserSelector().getParserTypeEnum().getList();
	}

	public void setSelectedParser(ParserFactory parser)
			throws SearchLibException {
		selectedParser = parser;
	}

	public ParserFactory getSelectedParser() throws SearchLibException {
		return selectedParser;
	}

	public boolean isEditing() {
		return currentParser != null;
	}

	public boolean isNotEditing() {
		return !isEditing();
	}

	public boolean isParserSelected() {
		return selectedParser != null;
	}

	public boolean isNoParserSelected() {
		return !isParserSelected();
	}

	public boolean isParserExtension() {
		if (currentParser == null)
			return false;
		Set<String> extensionSet = currentParser.getExtensionSet();
		if (extensionSet == null)
			return false;
		return extensionSet.size() > 0;
	}

	public boolean isParserMimeType() {
		if (currentParser == null)
			return false;
		Set<String> mimeTypeSet = currentParser.getMimeTypeSet();
		if (mimeTypeSet == null)
			return false;
		return mimeTypeSet.size() > 0;
	}

	public boolean isParserUrlPattern() {
		if (currentParser == null)
			return false;
		Set<String> urlPatternSet = currentParser.getUrlPatternSet();
		if (urlPatternSet == null)
			return false;
		return urlPatternSet.size() > 0;
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
			if (currentParser == null)
				return null;
			ParserFieldEnum[] parserFieldList = currentParser.getFieldList();
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

	public ParserFieldMap getFieldMap() {
		if (currentParser == null)
			return null;
		return currentParser.getFieldMap();
	}

	private ParserSelector getParserSelector() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		return client.getParserSelector();
	}

	private ParserFactory getParser(Component comp) {
		return (ParserFactory) getRecursiveComponentAttribute(comp,
				"parserItem");
	}

	public void doEdit(Component comp) throws SearchLibException {
		ParserFactory parser = getParser(comp);
		if (parser == null)
			return;
		selectedParser = parser;
		currentParser = ParserFactory.create(parser);
		reloadPage();
	}

	public void doDelete(Component comp) throws InterruptedException {
		ParserFactory parser = getParser(comp);
		if (parser == null)
			return;
		new DeleteAlert(parser);
	}

	public void onNew() throws SearchLibException {
		selectedParser = null;
		currentParser = ParserFactory.create(getClient(), "New parser",
				parserType.getParserClass().getCanonicalName());
		reloadPage();
	}

	public void onCancel() throws SearchLibException {
		currentParser = null;
		selectedParser = null;
		reloadPage();
	}

	public void onAdd() throws SearchLibException,
			TransformerConfigurationException, SAXException, IOException,
			XPathExpressionException, ParserConfigurationException {
		if (selectedParserField == null || selectedIndexField == null)
			return;
		ParserFieldMap fieldMap = getFieldMap();
		fieldMap.add(new SourceField(selectedParserField.name()),
				new ParserFieldTarget(selectedIndexField.getName(),
						captureRegexp, removeTag));
		reloadPage();
	}

	public void onSave() throws TransformerConfigurationException, IOException,
			SAXException, SearchLibException {
		if (currentParser == null)
			return;
		Client client = getClient();
		client.getParserSelector().replaceParserFactory(selectedParser,
				currentParser);
		client.saveParsers();
		selectedParser = currentParser;
		currentParser = null;
		reloadPage();
	}

	@SuppressWarnings("unchecked")
	public void onLinkRemove(Event event) throws SearchLibException,
			TransformerConfigurationException, SAXException, IOException,
			XPathExpressionException, ParserConfigurationException {
		GenericLink<SourceField, ParserFieldTarget> link = (GenericLink<SourceField, ParserFieldTarget>) event
				.getData();
		ParserFieldMap fieldMap = getFieldMap();
		fieldMap.remove(link);
		reloadPage();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void render(Listitem item, Object data) throws Exception {
		GenericLink<SourceField, ParserFieldTarget> link = (GenericLink<SourceField, ParserFieldTarget>) data;
		ParserFieldTarget fieldTarget = link.getTarget();
		new Listcell(link.getSource().getUniqueName()).setParent(item);
		new Listcell(fieldTarget.getName()).setParent(item);
		String s = fieldTarget.getCaptureRegexp();
		if (s != null)
			new Listcell(s).setParent(item);
		else
			new Listcell().setParent(item);
		new Listcell(Boolean.toString(fieldTarget.isRemoveTag()))
				.setParent(item);
		Listcell listcell = new Listcell();
		Image image = new Image("/images/action_delete.png");
		image.addForward(null, this, "onLinkRemove", data);
		image.setParent(listcell);
		listcell.setParent(item);
	}

	/**
	 * @return the currentExtension
	 */
	public String getCurrentExtension() {
		return currentExtension;
	}

	/**
	 * @param currentExtension
	 *            the currentExtension to set
	 */
	public void setCurrentExtension(String currentExtension) {
		this.currentExtension = currentExtension;
	}

	/**
	 * @return the currentMimeType
	 */
	public String getCurrentMimeType() {
		return currentMimeType;
	}

	/**
	 * @param currentMimeType
	 *            the currentMimeType to set
	 */
	public void setCurrentMimeType(String currentMimeType) {
		this.currentMimeType = currentMimeType;
	}

	/**
	 * @return the currentUrlPattern
	 */
	public String getCurrentUrlPattern() {
		return currentUrlPattern;
	}

	/**
	 * @param currentUrlPattern
	 *            the currentUrlPattern to set
	 */
	public void setCurrentUrlPattern(String currentUrlPattern) {
		this.currentUrlPattern = currentUrlPattern;
	}

	public void onAddExtension() throws MalformedURLException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException, SearchLibException {
		if (currentExtension == null || currentExtension.trim().length() == 0)
			return;
		ParserFactory p = getClient().getParserSelector()
				.checkParserFromExtension(currentExtension);
		if (p != null && p != selectedParser)
			throw new SearchLibException("This extension is already affected");
		currentParser.addExtension(currentExtension.trim());
		reloadPage();
	}

	public void onDeleteExtension(Component comp) throws SearchLibException {
		currentParser.removeExtension((String) getRecursiveComponentAttribute(
				comp, "extensionItem"));
		reloadPage();
	}

	public void onAddMimeType() throws MalformedURLException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException, SearchLibException {
		if (currentMimeType == null || currentMimeType.trim().length() == 0)
			return;
		currentParser.addMimeType(currentMimeType.trim());
		reloadPage();
	}

	public void onDeleteMimeType(Component comp) throws SearchLibException {
		currentParser.removeMimeType((String) getRecursiveComponentAttribute(
				comp, "mimeTypeItem"));
		reloadPage();
	}

	public void onAddUrlPattern() throws MalformedURLException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException, SearchLibException {
		if (currentUrlPattern == null || currentUrlPattern.trim().length() == 0)
			return;
		currentParser.addUrlPattern(currentUrlPattern.trim());
		reloadPage();
	}

	public void onDeleteUrlPattern(Component comp) throws SearchLibException {
		currentParser.removeUrlPattern((String) getRecursiveComponentAttribute(
				comp, "urlPatternItem"));
		reloadPage();
	}

	/**
	 * @return the captureRegexp
	 */
	public String getCaptureRegexp() {
		return captureRegexp;
	}

	/**
	 * @param captureRegexp
	 *            the captureRegexp to set
	 */
	public void setCaptureRegexp(String captureRegexp) {
		this.captureRegexp = captureRegexp;
	}

	/**
	 * @return the removeTag
	 */
	public boolean isRemoveTag() {
		return removeTag;
	}

	/**
	 * @param removeTag
	 *            the removeTag to set
	 */
	public void setRemoveTag(boolean removeTag) {
		this.removeTag = removeTag;
	}

	public String getWebDefaultParser() throws SearchLibException {
		ParserSelector parserSelector = getParserSelector();
		if (parserSelector == null)
			return null;
		return parserSelector.getWebCrawlerDefaultParserName();
	}

	public void setWebDefaultParser(String value) throws SearchLibException {
		ParserSelector parserSelector = getParserSelector();
		if (parserSelector == null)
			return;
		parserSelector.setWebCrawlerDefaultParserName(value);
		getClient().saveParsers();
	}

	public String getFileDefaultParser() throws SearchLibException {
		ParserSelector parserSelector = getParserSelector();
		if (parserSelector == null)
			return null;
		return parserSelector.getFileCrawlerDefaultParserName();
	}

	public void setFileDefaultParser(String value) throws SearchLibException {
		ParserSelector parserSelector = getParserSelector();
		if (parserSelector == null)
			return;
		parserSelector.setFileCrawlerDefaultParserName(value);
		getClient().saveParsers();
	}

}
