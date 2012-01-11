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

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.file.database.FilePathItem;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.Result;
import com.jaeksoft.searchlib.scheduler.JobItem;
import com.jaeksoft.searchlib.user.User;

public interface EventInterface {

	public User getLoggedUser();

	public Client getClient() throws SearchLibException;

	void eventClientChange() throws SearchLibException;

	void eventClientSwitch(Client client) throws SearchLibException;

	void eventFlushPrivileges() throws SearchLibException;

	void eventDocumentUpdate() throws SearchLibException;

	void eventRequestListChange() throws SearchLibException;

	void eventSchemaChange() throws SearchLibException;

	void eventJobEdit(JobItem jobItem) throws SearchLibException;

	void eventFilePathEdit(FilePathItem filePathItem) throws SearchLibException;

	void eventLogout() throws SearchLibException;

	void eventQueryEditResult(Result data);

	void eventQueryEditRequest(SearchRequest data);

}
