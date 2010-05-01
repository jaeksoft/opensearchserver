package net.gisiinteractive.common.exceptions;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class MultipleBusinessExceptions extends BusinessException {
	private static final long serialVersionUID = 1L;
	protected List<BusinessException> nestedExceptions;

	public MultipleBusinessExceptions(String message,
			List<BusinessException> nestedExceptions) {
		super(message);
		this.nestedExceptions = nestedExceptions;
	}

	public MultipleBusinessExceptions(Throwable cause) {
		super(cause);
	}

	public MultipleBusinessExceptions(String message, Throwable cause) {
		super(message, cause);
	}

	public MultipleBusinessExceptions(String message, Type type,
			List<BusinessException> nestedExceptions) {
		super(message, type);
		this.nestedExceptions = nestedExceptions;
	}

	public MultipleBusinessExceptions(Throwable cause, Type type) {
		super(cause, type);
	}

	public MultipleBusinessExceptions(String message, Throwable cause, Type type) {
		super(message, cause, type);
	}

	public List<BusinessException> getNestedExceptions() {
		return nestedExceptions;
	}

	public void setNestedExceptions(List<BusinessException> nestedExceptions) {
		this.nestedExceptions = nestedExceptions;
	}

	public boolean add(BusinessException e) {
		return nestedExceptions.add(e);
	}

	public void add(int index, BusinessException element) {
		nestedExceptions.add(index, element);
	}

	public boolean addAll(Collection<? extends BusinessException> c) {
		return nestedExceptions.addAll(c);
	}

	public boolean addAll(int index, Collection<? extends BusinessException> c) {
		return nestedExceptions.addAll(index, c);
	}

	public void clear() {
		nestedExceptions.clear();
	}

	public boolean contains(Object o) {
		return nestedExceptions.contains(o);
	}

	public boolean containsAll(Collection<?> c) {
		return nestedExceptions.containsAll(c);
	}

	public boolean isEmpty() {
		return nestedExceptions.isEmpty();
	}

	public Iterator<BusinessException> iterator() {
		return nestedExceptions.iterator();
	}

	public BusinessException remove(int index) {
		return nestedExceptions.remove(index);
	}

	public boolean remove(Object o) {
		return nestedExceptions.remove(o);
	}

	public boolean removeAll(Collection<?> c) {
		return nestedExceptions.removeAll(c);
	}

	public boolean retainAll(Collection<?> c) {
		return nestedExceptions.retainAll(c);
	}

	public int size() {
		return nestedExceptions.size();
	}

}
