/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013-2015 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.script.commands;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.script.CommandAbstract;
import com.jaeksoft.searchlib.script.CommandEnum;
import com.jaeksoft.searchlib.script.ScriptCommandContext;
import com.jaeksoft.searchlib.script.ScriptException;

public class IndexDocumentCommands {

	public static class New extends CommandAbstract {

		public New() {
			super(CommandEnum.INDEX_DOCUMENT_NEW);
		}

		@Override
		public void run(ScriptCommandContext context, String id,
				String... parameters) throws ScriptException {
			IndexDocument indexDocument = new IndexDocument(
					LanguageEnum.findByNameOrCode(getParameterString(1)));
			context.addIndexDocument(indexDocument);
		}
	}

	public static class AddValue extends CommandAbstract {

		public AddValue() {
			super(CommandEnum.INDEX_DOCUMENT_ADD_VALUE);
		}

		@Override
		public void run(ScriptCommandContext context, String id,
				String... parameters) throws ScriptException {
			checkParameters(2, parameters);
			IndexDocument indexDocument = context.getIndexDocument();
			if (indexDocument == null)
				throwError("No index document has been created. Call INDEX_DOCUMENT_NEW.");
			String field = getParameterString(0);
			String value = getParameterString(1);
			value = context.replaceVariables(value);
			Float boost = getParameterFloat(2);
			indexDocument.add(field, value, boost == null ? 1.0F : boost);
		}
	}

	public static class AddNow extends CommandAbstract {

		public AddNow() {
			super(CommandEnum.INDEX_DOCUMENT_ADD_NOW);
		}

		@Override
		public void run(ScriptCommandContext context, String id,
				String... parameters) throws ScriptException {
			checkParameters(2, parameters);
			IndexDocument indexDocument = context.getIndexDocument();
			if (indexDocument == null)
				throwError("No index document has been created. Call INDEX_DOCUMENT_NEW.");
			String field = getParameterString(0);
			String format = getParameterString(1);
			format = context.replaceVariables(format);
			SimpleDateFormat df = new SimpleDateFormat(format);
			String value = df.format(new Date());
			Float boost = getParameterFloat(2);
			indexDocument.add(field, value, boost == null ? 1.0F : boost);
		}
	}

	public static class Update extends CommandAbstract {

		public Update() {
			super(CommandEnum.INDEX_DOCUMENT_UPDATE);
		}

		@Override
		public void run(ScriptCommandContext context, String id,
				String... parameters) throws ScriptException {
			List<IndexDocument> indexDocuments = context.getIndexDocuments();
			if (CollectionUtils.isEmpty(indexDocuments))
				return;
			Client client = (Client) context.getConfig();
			try {
				context.clearIndexDocuments(client
						.updateDocuments(indexDocuments));
			} catch (IOException e) {
				throw new ScriptException(e);
			} catch (SearchLibException e) {
				throw new ScriptException(e);
			}
		}
	}

}
