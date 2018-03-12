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

import java.util.Collection;

public enum Language {
	en, fr, de, it;

	public static Language findByName(final String name, final Language defaultValue) {
		try {
			return Language.valueOf(name);
		} catch (IllegalArgumentException | NullPointerException e) {
			return defaultValue;
		}
	}

	public static Language find(final Object value, final Language defaultValue) {
		if (value == null)
			return defaultValue;
		if (value instanceof Collection) {
			final Collection<?> collection = (Collection) value;
			if (collection.isEmpty())
				return defaultValue;
			return find(collection.iterator().next(), defaultValue);
		}
		return findByName(value.toString(), defaultValue);
	}
}