/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
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

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.GlobalCommand;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.file.database.FilePathItem;
import com.jaeksoft.searchlib.request.AbstractRequest;
import com.jaeksoft.searchlib.result.AbstractResult;
import com.jaeksoft.searchlib.scheduler.JobItem;
import com.jaeksoft.searchlib.user.User;

public interface EventInterface {

	@GlobalCommand
	void eventClientChange() throws SearchLibException;

	@GlobalCommand
	void eventClientSwitch(@BindingParam("client") Client client)
			throws SearchLibException;

	@GlobalCommand
	void eventFlushPrivileges(@BindingParam("user") User user)
			throws SearchLibException;

	@GlobalCommand
	void eventDocumentUpdate(@BindingParam("client") Client client)
			throws SearchLibException;

	@GlobalCommand
	void eventRequestListChange(@BindingParam("client") Client client)
			throws SearchLibException;

	@GlobalCommand
	void eventSchemaChange(@BindingParam("client") Client client)
			throws SearchLibException;

	@GlobalCommand
	void eventEditRequest(@BindingParam("request") AbstractRequest request)
			throws SearchLibException;

	@GlobalCommand
	void eventEditScheduler(@BindingParam("jobItem") JobItem jobItem)
			throws SearchLibException;

	@GlobalCommand
	void eventEditRequestResult(@BindingParam("result") AbstractResult<?> result)
			throws SearchLibException;

	@GlobalCommand
	void eventEditFileRepository(
			@BindingParam("filePathItem") FilePathItem filePathItem)
			throws SearchLibException;

	@GlobalCommand
	void eventLogout(@BindingParam("user") User user) throws SearchLibException;

}
