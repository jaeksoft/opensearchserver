package org.apache.manifoldcf.agents.output.opensearchserver;

import org.apache.manifoldcf.core.interfaces.ConfigParams;
import org.apache.manifoldcf.core.interfaces.IPostParameters;

public class OpenSearchServerConfig extends OpenSearchServerParam {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2071296573398352538L;

	/**
	 * Parameters used for the configuration
	 */
	final private static ParameterEnum[] CONFIGURATIONLIST = {
			ParameterEnum.SERVERLOCATION, ParameterEnum.INDEXNAME,
			ParameterEnum.USERNAME, ParameterEnum.APIKEY };

	/**
	 * Build a set of OpenSearchServerParameters by reading ConfigParams. If the
	 * value returned by ConfigParams.getParameter is null, the default value is
	 * set.
	 * 
	 * @param paramList
	 * @param params
	 */
	public OpenSearchServerConfig(ConfigParams params) {
		super(CONFIGURATIONLIST);
		for (ParameterEnum param : CONFIGURATIONLIST) {
			String value = params.getParameter(param.name());
			if (value == null)
				value = param.defaultValue;
			put(param, value);
		}
	}

	/**
	 * 
	 * @return a unique identifier for one index on one OpenSearchServer
	 *         instance.
	 */
	public String getUniqueIndexIdentifier() {
		StringBuffer sb = new StringBuffer();
		sb.append(getServerLocation());
		if (sb.charAt(sb.length() - 1) != '/')
			sb.append('/');
		sb.append(getIndexName());
		return sb.toString();
	}

	public final static void contextToConfig(IPostParameters variableContext,
			ConfigParams parameters) {
		for (ParameterEnum param : CONFIGURATIONLIST) {
			String p = variableContext.getParameter(param.name().toLowerCase());
			if (p != null)
				parameters.setParameter(param.name(), p);
		}
	}

	final public String getServerLocation() {
		return get(ParameterEnum.SERVERLOCATION);
	}

	final public String getIndexName() {
		return get(ParameterEnum.INDEXNAME);
	}

	final public String getUserName() {
		return get(ParameterEnum.USERNAME);
	}

	final public String getApiKey() {
		return get(ParameterEnum.APIKEY);
	}
}
