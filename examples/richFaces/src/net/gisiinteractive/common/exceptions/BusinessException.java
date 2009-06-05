package net.gisiinteractive.common.exceptions;

public class BusinessException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	protected Type type;

	public enum Type {
		FATAL, WARN, LOG, MUTE
	}

	public BusinessException(String message) {
		this(message, Type.FATAL);
	}

	public BusinessException(Throwable cause) {
		this(cause, Type.FATAL);
	}

	public BusinessException(String message, Throwable cause) {
		this(message, cause, Type.FATAL);
	}

	public BusinessException(String message, Type type) {
		this(message, null, type);
	}

	public BusinessException(Throwable cause, Type type) {
		this(null, cause, type);
	}

	public BusinessException(String message, Throwable cause, Type type) {
		super(message, cause);
		this.type = type;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

}
