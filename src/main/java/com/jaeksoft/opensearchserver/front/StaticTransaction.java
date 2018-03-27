/*
 * Copyright 2017-2018 Emmanuel Keller / Jaeksoft
 *  <p>
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  <p>
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  <p>
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.jaeksoft.opensearchserver.front;

import com.jaeksoft.opensearchserver.Components;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class StaticTransaction extends ServletTransaction {

	private final String template;

	public StaticTransaction(final Components components, final String template, final HttpServletRequest request,
			final HttpServletResponse response, final boolean requireLoggedUser) {
		super(components.getFreemarkerTool(), request, response, requireLoggedUser);
		this.template = template;
	}

	@Override
	protected String getTemplate() {
		return template;
	}
}
