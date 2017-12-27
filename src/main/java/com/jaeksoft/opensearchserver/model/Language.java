package com.jaeksoft.opensearchserver.model;

import java.util.Collection;

public enum Language {
	en, fr, de, it;

	public static Language find(Object value) {
		if (value == null)
			return null;
		if (value instanceof Collection) {
			final Collection<?> collection = (Collection) value;
			if (collection.isEmpty())
				return null;
			return find(collection.iterator().next());
		}
		try {
			return Language.valueOf(value.toString());
		} catch (IllegalArgumentException | NullPointerException e) {
			return null;
		}
	}
}