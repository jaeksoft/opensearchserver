package com.jaeksoft.searchlib.basket;

import com.jaeksoft.searchlib.cache.CacheKeyInterface;

public class BasketKey implements CacheKeyInterface<BasketKey> {

	private long key;

	public BasketKey(long key) {
		this.key = key;
	}

	public BasketKey(BasketDocument basketDocument) {
		key = (long) Integer.MAX_VALUE * 2 - (long) basketDocument.hashCode();
	}

	@Override
	public int compareTo(BasketKey basketKey) {
		if (key < basketKey.key)
			return -1;
		if (key > basketKey.key)
			return 1;
		return 0;
	}

	@Override
	public String toString() {
		return Long.toString(key);
	}
}
