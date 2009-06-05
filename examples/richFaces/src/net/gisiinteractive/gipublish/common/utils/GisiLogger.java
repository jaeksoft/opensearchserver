package net.gisiinteractive.gipublish.common.utils;

import org.apache.commons.logging.Log;

/**
 * Class <code>PropertiesManager</code>
 * 
 * This class implements the design patern singleton. <br>
 * It's used to load a set of configuration properties needed to performe<br>
 * some backoffice task.
 * 
 */
public class GisiLogger {

	private String typeApplication;
	private String platProperty;

	private Log logger = null;

	public GisiLogger(String name) {
		
		logger = org.apache.commons.logging.LogFactory.getLog(typeApplication
				+ platProperty + name);
	}

	public void info(Object message, Throwable t) {
		logger.info(message, t);
	}

	public void info(Object message) {
		logger.info(message);
	}

	public void warn(Object message, Throwable t) {
		logger.warn(message, t);
	}

	public void warn(Object message) {
		logger.warn(message);
	}

	public void debug(Object message, Throwable t) {
		logger.debug(message, t);
	}

	public void debug(Object message) {
		logger.debug(message);
	}

	public void error(Object message) {
		logger.error(message);
	}

	public void error(Object message, Throwable t) {

		logger.error(message, t);
	}

	@SuppressWarnings("unchecked")
	public static GisiLogger getLogger(Class clazz) {
		return new GisiLogger(clazz.getCanonicalName());
	}

	public boolean isDebugEnabled() {
		return logger.isDebugEnabled();
	}

	public void trace(String msg) {
		logger.trace(msg);
	}

	public boolean isTraceEnabled() {
		return logger.isTraceEnabled();
	}
}
