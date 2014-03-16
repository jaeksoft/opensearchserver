/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
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

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Messagebox;

import com.jaeksoft.searchlib.SearchLibException;

public class AlertController implements EventListener<Event> {

	private final static String ALERT_TITLE = "OpenSearchServer";

	private final String redirection;

	public AlertController(String msg) throws InterruptedException {
		Messagebox.show(msg, ALERT_TITLE, Messagebox.OK,
				Messagebox.INFORMATION, this);
		redirection = null;
	}

	public AlertController(String title, String msg, String redirection)
			throws InterruptedException {
		Messagebox
				.show(msg, title, Messagebox.OK, Messagebox.INFORMATION, this);
		this.redirection = redirection;
	}

	public AlertController(String msg, int buttons, String icon)
			throws InterruptedException {
		Messagebox.show(msg, ALERT_TITLE, buttons, icon, this);
		redirection = null;
	}

	public AlertController(String msg, String icon) throws InterruptedException {
		Messagebox.show(msg, ALERT_TITLE, Messagebox.OK, icon, this);
		redirection = null;
	}

	@Override
	public void onEvent(Event event) throws Exception {
		switch (((Integer) event.getData()).intValue()) {
		case Messagebox.OK:
			onOk();
			break;
		case Messagebox.ABORT:
			onAbort();
			break;
		case Messagebox.CANCEL:
			onCancellation();
			break;
		case Messagebox.IGNORE:
			onIgnore();
			break;
		case Messagebox.YES:
			onYes();
			break;
		case Messagebox.NO:
			onNo();
			break;
		case Messagebox.RETRY:
			onRetry();
			break;
		}
		if (redirection != null)
			Executions.getCurrent().sendRedirect(redirection);
	}

	protected void onOk() throws SearchLibException {
	}

	protected void onAbort() throws SearchLibException {
	}

	protected void onCancellation() throws SearchLibException {
	}

	protected void onIgnore() throws SearchLibException {
	}

	protected void onYes() throws SearchLibException, InterruptedException,
			IOException {
	}

	protected void onNo() throws SearchLibException {
	}

	protected void onRetry() throws SearchLibException {
	}
}
