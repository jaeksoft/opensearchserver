package net.gisiinteractive.gipublish.model.common;

import java.io.Serializable;

/**
 * 
 * @author zhamdi
 * 
 * @param <T>
 *            the id type
 */
public interface DBObject<T> extends Serializable {
	T getId();

	void setId(T id);
}
