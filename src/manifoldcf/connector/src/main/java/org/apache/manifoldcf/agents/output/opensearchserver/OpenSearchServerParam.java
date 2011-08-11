package org.apache.manifoldcf.agents.output.opensearchserver;

import java.util.HashMap;
import java.util.Map;

import org.apache.manifoldcf.agents.output.opensearchserver.OpenSearchServerParam.ParameterEnum;
import org.apache.manifoldcf.core.interfaces.ConfigParams;
import org.apache.manifoldcf.core.interfaces.ConfigurationNode;
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

		FIELDLIST(""),

		MAXFILESIZE("16777216"),

		MIMETYPES(
				"application/msword\n"
						+ "application/vnd.ms-excel\n"
						+ "application/vnd.openxmlformats-officedocument.wordprocessingml.document\n"
						+ "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet\n"
						+ "text/html\n"
						+ "application/pdf\n"
						+ "application/vnd.ms-powerpoint\n"
						+ "application/vnd.openxmlformats-officedocument.presentationml.presentation\n"
						+ "application/vnd.oasis.opendocument.text\n"
						+ "application/vnd.oasis.opendocument.spreadsheet\n"
						+ "application/vnd.oasis.opendocument.formula\n"
						+ "application/rtf\n" + "text/plain\n" + "audio/mpeg\n"
						+ "audio/x-wav\n" + "audio/ogg\n" + "audio/flac\n"
						+ "application/x-bittorrent");

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

	final public static ParameterEnum[] SPECIFICATIONLIST = {
			ParameterEnum.MAXFILESIZE, ParameterEnum.MIMETYPES };

	final public static String OPENSEARCHSERVER_SPECS_NODE = "OPENSEARCHSERVER_SPECS_NODE";

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
	 * Build a set of OpenSearchServerParameters by reading an instance of
	 * SpecificationNode.
	 * 
	 * @param paramList
	 * @param node
	 */
	public OpenSearchServerParam(ParameterEnum[] paramList,
			ConfigurationNode node) {
		for (ParameterEnum param : paramList) {
			String value = null;
			if (node != null)
				value = node.getAttributeValue(param.name());
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

	public static void contextToSpecNode(ParameterEnum[] paramList,
			IPostParameters variableContext, ConfigurationNode specNode) {
		for (ParameterEnum param : paramList) {
			String p = variableContext.getParameter(param.name().toLowerCase());
			if (p != null)
				specNode.setAttribute(param.name(), p);
		}
	}

	public String getUniqueIndexIdentifier() {
		StringBuffer sb = new StringBuffer();
		sb.append(get(ParameterEnum.SERVERLOCATION));
		if (sb.charAt(sb.length() - 1) != '/')
			sb.append('/');
		sb.append(get(ParameterEnum.INDEXNAME));
		return sb.toString();
	}

}
