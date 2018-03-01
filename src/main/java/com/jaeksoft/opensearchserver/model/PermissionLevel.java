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

package com.jaeksoft.opensearchserver.model;

import com.qwazr.utils.StringUtils;

public enum PermissionLevel {

	OWNER(1), ADMIN(2), WRITE(3), READ(4);

	public final int value;
	public final String name;

	PermissionLevel(int value) {
		this.value = value;
		this.name = StringUtils.capitalize(name());
	}

	static PermissionLevel resolve(final Integer value) {
		if (value == null)
			return null;
		switch (value) {
		case 1:
			return OWNER;
		case 2:
			return ADMIN;
		case 3:
			return WRITE;
		case 4:
			return READ;
		default:
			return null;
		}
	}

	@Override
	public String toString() {
		return name;
	}

	public static PermissionLevel resolve(final String name) {
		return StringUtils.isBlank(name) ? null : valueOf(name.toUpperCase());
	}
}
