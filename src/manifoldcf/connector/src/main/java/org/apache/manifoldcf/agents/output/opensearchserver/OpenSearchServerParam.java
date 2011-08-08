package org.apache.manifoldcf.agents.output.opensearchserver;

import java.util.HashMap;
import java.util.Map;

import org.apache.manifoldcf.agents.output.opensearchserver.OpenSearchServerParam.ParameterEnum;
import org.apache.manifoldcf.core.interfaces.ConfigParams;
import org.apache.manifoldcf.core.interfaces.IPostParameters;

public class OpenSearchServerParam extends HashMap<ParameterEnum, String> {

	/**
	 * Parameters constants
	 */
	public enum ParameterEnum {
		SERVERLOCATION("http://localhost:8080/"),

		INDEXNAME("index"),

		USERNAME(""),

		APIKEY(""),

		FIELDLIST("");

		final protected String defaultValue;

		private ParameterEnum(String defaultValue) {
			this.defaultValue = defaultValue;
		}
	}

	/**
	 * Parameters used for the configuration
	 */
	final public static ParameterEnum[] CONFIGURATIONLIST = {
			ParameterEnum.SERVERLOCATION, ParameterEnum.INDEXNAME,
			ParameterEnum.USERNAME, ParameterEnum.APIKEY };

	final public static ParameterEnum[] SPECIFICATIONLIST = { ParameterEnum.FIELDLIST };

	private static final long serialVersionUID = -1593234685772720029L;

	/**
	 * Build a set of OpenSearchServerParameters by reading ConfigParams. If the
	 * value returned by ConfigParams.getParameter is null, the default value is
	 * set.
	 * 
	 * @param paramList
	 * @param params
	 */
	public OpenSearchServerParam(ParameterEnum[] paramList, ConfigParams params) {
		for (ParameterEnum param : paramList) {
			String value = params.getParameter(param.name());
			if (value == null)
				value = param.defaultValue;
			put(param, value);
		}
	}

	/**
	 * Replace the variables ${PARAMNAME} with the value contained in the set.
	 * 
	 * @param text
	 * @return
	 */
	final public String replace(String text) {
		for (Map.Entry<ParameterEnum, String> entry : this.entrySet())
			text = text.replace("${" + entry.getKey().name() + "}",
					entry.getValue());
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
