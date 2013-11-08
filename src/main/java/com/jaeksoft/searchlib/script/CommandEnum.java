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

package com.jaeksoft.searchlib.script;

import com.jaeksoft.searchlib.script.commands.ExecutionCommands;
import com.jaeksoft.searchlib.script.commands.IndexDocumentCommands;
import com.jaeksoft.searchlib.script.commands.SearchCommands;
import com.jaeksoft.searchlib.script.commands.Selectors;
import com.jaeksoft.searchlib.script.commands.WebDriverCommands;

public enum CommandEnum {

	CSS_SELECTOR_ADD(Selectors.CSS_Add.class),

	CSS_SELECTOR_RESET(Selectors.CSS_Reset.class),

	CSS_SELECTOR_INDEX_FIELD(Selectors.CSS_IndexField.class),

	CSS_SELECTOR_SUBSCRIPT(Selectors.CSS_SubScript.class),

	CSS_SELECTOR_CLICK_AND_SCRIPT(Selectors.CSS_ClickAndScript.class),

	XPATH_SELECTOR_ADD(Selectors.XPATH_Add.class),

	XPATH_SELECTOR_RESET(Selectors.XPATH_Reset.class),

	XPATH_SELECTOR_INDEX_FIELD(Selectors.XPATH_IndexField.class),

	XPATH_SELECTOR_SUBSCRIPT(Selectors.XPATH_SubScript.class),

	XPATH_SELECTOR_CLICK_AND_SCRIPT(Selectors.XPATH_ClickAndScript.class),

	ID_SELECTOR_ADD(Selectors.ID_Add.class),

	ID_SELECTOR_RESET(Selectors.ID_Reset.class),

	ID_SELECTOR_INDEX_FIELD(Selectors.ID_IndexField.class),

	ID_SELECTOR_SUBSCRIPT(Selectors.ID_SubScript.class),

	ID_SELECTOR_CLICK_AND_SCRIPT(Selectors.ID_ClickAndScript.class),

	ALL_SELECTOR_RESET(Selectors.ALL_Reset.class),

	SLEEP(ExecutionCommands.SleepCommand.class),

	SCRIPT(ExecutionCommands.ScriptCommand.class),

	ON_ERROR(ExecutionCommands.OnErrorCommand.class),

	SEARCH_TEMPLATE(SearchCommands.SearchTemplate.class),

	SEARCH_TEMPLATE_JSON(SearchCommands.SearchTemplateJson.class),

	WEBDRIVER_OPEN(WebDriverCommands.Open.class),

	WEBDRIVER_RESIZE(WebDriverCommands.Resize.class),

	WEBDRIVER_GET(WebDriverCommands.Get.class),

	WEBDRIVER_CAPTURE(WebDriverCommands.Capture.class),

	WEBDRIVER_SCREENSHOT(WebDriverCommands.Screenshot.class),

	WEBDRIVER_SET_TIMEOUTS(WebDriverCommands.SetTimeOuts.class),

	WEBDRIVER_CLOSE(WebDriverCommands.Close.class),

	WEBDRIVER_JAVASCRIPT(WebDriverCommands.Javascript.class),

	WEBDRIVER_NEW_WINDOW(WebDriverCommands.NewWindow.class),

	WEBDRIVER_CLOSE_WINDOW(WebDriverCommands.CloseWindow.class),

	INDEX_DOCUMENT_NEW(IndexDocumentCommands.New.class),

	INDEX_DOCUMENT_ADD_VALUE(IndexDocumentCommands.AddValue.class),

	INDEX_DOCUMENT_UPDATE(IndexDocumentCommands.Update.class);

	private final Class<? extends CommandAbstract> commandClass;

	private CommandEnum(Class<? extends CommandAbstract> commandClass) {
		this.commandClass = commandClass;
	}

	public CommandAbstract getNewInstance() throws ScriptException {
		try {
			return commandClass.newInstance();
		} catch (InstantiationException e) {
			throw new ScriptException("Cannot instance the command: " + name(),
					e);
		} catch (IllegalAccessException e) {
			throw new ScriptException("Cannot instance the command: " + name(),
					e);
		}
	}

	public static final CommandEnum find(String command) throws ScriptException {
		if (command == null)
			throw new ScriptException("No command: ");
		try {
			return valueOf(command);
		} catch (IllegalArgumentException e) {
			throw new ScriptException("Unknown command: " + command, e);
		}
	}

}
