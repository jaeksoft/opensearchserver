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

public class Message {

	public enum Css {
		success, warning, info, danger;
	}

	private final Css css;
	private final String title;
	private final String message;

	Message(final Css css, final String title, final String message) {
		this.css = css;
		this.title = title;
		this.message = message;
	}

	public String getCss() {
		return css.name();
	}

	public String getTitle() {
		return title;
	}

	public String getMessage() {
		return message;
	}

}
