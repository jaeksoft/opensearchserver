package org.apache.manifoldcf.agents.output.opensearchserver;

import java.util.HashSet;
import java.util.Set;

import org.apache.manifoldcf.core.interfaces.ConfigParams;
import org.apache.manifoldcf.core.interfaces.IPostParameters;

public class OpenSearchServerParam {

	/**
	 * Parameters constants
	 */
	private enum ParameterEnum {
		SERVERLOCATION("http://localhost:8080/"),

		INDEXNAME("index"),

		USERNAME(""),

		APIKEY("");

		final protected String defaultValue;

		private ParameterEnum(String defaultValue) {
			this.defaultValue = defaultValue;
		}
	}

	final public ParameterEnum key;
	final public String value;

	protected OpenSearchServerParam(ParameterEnum key, String value) {
		this.key = key;
		this.value = value;
	}

	/**
	 * Parameters used for the configuration
	 */
	final public static ParameterEnum[] CONFIGURATIONLIST = {
			ParameterEnum.SERVERLOCATION, ParameterEnum.INDEXNAME,
			ParameterEnum.USERNAME, ParameterEnum.APIKEY };

	/**
	 * Build a set of OpenSearchServerParameters by reading ConfigParams. If the
	 * value returned by ConfigParams.getParameter is null, the default value is
	 * set.
	 * 
	 * @param params
	 * @return
	 */
	final public static Set<OpenSearchServerParam> getParameters(
			ParameterEnum[] paramList, ConfigParams params) {
		Set<OpenSearchServerParam> ossParams = new HashSet<OpenSearchServerParam>();
		for (ParameterEnum param : paramList) {
			String value = params.getParameter(param.name());
			if (value == null)
				value = param.defaultValue;
			ossParams.add(new OpenSearchServerParam(param, value));
		}
		return ossParams;
	}

	/**
	 * Replace the variables ${PARAMNAME} with the value contained in the set.
	 * 
	 * @param text
	 * @param params
	 */
	final public static String replace(String text,
			Set<OpenSearchServerParam> params) {
		for (OpenSearchServerParam param : params)
			text = text.replace("${" + param.key.name() + "}", param.value);
		return text;
	}

	public static void contextToConfig(ParameterEnum[] paramList,
			IPostParameters variableContext, ConfigParams parameters) {
		for (ParameterEnum param : paramList) {
			String p = variableContext.getParameter(param.name().toLowerCase());
			if (p != null)
				parameters.setParameter(param.name(), p);
		}
	}
}
