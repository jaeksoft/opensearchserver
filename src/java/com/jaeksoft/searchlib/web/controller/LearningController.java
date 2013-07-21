/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.web.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.learning.Learner;
import com.jaeksoft.searchlib.learning.LearnerManager;

public class LearningController extends CommonController {

	private Learner selectedLearner;
	private Learner currentLearner;

	private transient int totalSize;
	private transient int activePage;

	public LearningController() throws SearchLibException {
		super();
		reset();
	}

	@Override
	protected void reset() throws SearchLibException {
		currentLearner = null;
		selectedLearner = null;
		totalSize = 0;
		activePage = 0;
	}

	public Learner[] getLearners() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		return client.getLearnerManager().getArray();
	}

	public String getCurrentEditMode() throws SearchLibException {
		return selectedLearner == null ? "Create a new learner"
				: "Edit the selected learner";
	}

	public boolean isEditing() {
		return currentLearner != null;
	}

	public boolean isNotEditing() {
		return !isEditing();
	}

	public boolean isSelected() {
		return selectedLearner != null;
	}

	public boolean isNotSelected() {
		return !isSelected();
	}

	@Command
	@NotifyChange("*")
	public void onNewLearner() throws SearchLibException {
		currentLearner = new Learner();
	}

	@Command
	@NotifyChange("*")
	public void onCancel() throws SearchLibException {
		currentLearner = null;
		selectedLearner = null;
	}

	@Command
	@NotifyChange("*")
	public void onSave() throws SearchLibException,
			UnsupportedEncodingException {
		Client client = getClient();
		if (client == null)
			return;
		LearnerManager lm = client.getLearnerManager();
		if (selectedLearner != null) {
			lm.replace(selectedLearner, currentLearner);
		} else
			lm.add(currentLearner);
		client.saveLearner(currentLearner);
		onCancel();
	}

	@Command
	@NotifyChange("*")
	public void onDelete() throws SearchLibException, IOException {
		Client client = getClient();
		if (client == null)
			return;
		client.getLearnerManager().remove(selectedLearner);
		client.deleteLearner(selectedLearner);
		onCancel();
	}

	@Command
	@NotifyChange("*")
	public void onCheck() throws SearchLibException, InterruptedException {
		Client client = getClient();
		if (client == null)
			return;
		if (currentLearner == null)
			return;
		currentLearner.checkInstance(client);
		new AlertController("Learner successfully checked");
	}

	public Learner getCurrentLearner() {
		return currentLearner;
	}

	public Learner getSelectedLearner() {
		return selectedLearner;
	}

	public void setSelectedLearner(Learner learner) throws SearchLibException {
		if (learner == null)
			return;
		selectedLearner = learner;
		currentLearner = new Learner(learner);
		reload();
	}

	public int getPageSize() {
		return 20;
	}

	public int getActivePage() {
		return activePage;
	}

	public void setActivePage(int page) throws SearchLibException {
		synchronized (this) {
			activePage = page;
			reload();
		}
	}

	public int getTotalSize() {
		return totalSize;
	}

}
