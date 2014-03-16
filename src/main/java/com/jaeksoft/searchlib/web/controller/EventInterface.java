/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012-2014 Emmanuel Keller / Jaeksoft
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
import com.jaeksoft.searchlib.request.AbstractRequest;
import com.jaeksoft.searchlib.result.AbstractResult;
import com.jaeksoft.searchlib.scheduler.JobItem;
import com.jaeksoft.searchlib.user.User;

public interface EventInterface {

	void eventClientChange() throws SearchLibException;

	void eventClientSwitch(Client client) throws SearchLibException;

	void eventFlushPrivileges(User user) throws SearchLibException;

	void eventDocumentUpdate(Client client) throws SearchLibException;

	void eventRequestListChange(Client client) throws SearchLibException;

	void eventSchemaChange(Client client) throws SearchLibException;

	void eventEditRequest(AbstractRequest request) throws SearchLibException;

	void eventEditScheduler(JobItem jobItem) throws SearchLibException;

	void eventEditRequestResult(AbstractResult<?> result)
			throws SearchLibException;

	void eventEditFileRepository(FilePathItem filePathItem)
			throws SearchLibException;

	void eventLogout(User user) throws SearchLibException;

}
