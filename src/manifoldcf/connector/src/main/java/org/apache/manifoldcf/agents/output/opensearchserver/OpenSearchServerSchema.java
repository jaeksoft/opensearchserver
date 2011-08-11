package org.apache.manifoldcf.agents.output.opensearchserver;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.manifoldcf.core.interfaces.ManifoldCFException;

public class OpenSearchServerSchema extends OpenSearchServerConnection {

	public OpenSearchServerSchema(OpenSearchServerParam params)
			throws ManifoldCFException {
		super(params);
		String indexName = params
				.get(OpenSearchServerParam.ParameterEnum.INDEXNAME);
		StringBuffer url = getApiUrl("schema");
		url.append("&cmd=indexList");
		GetMethod method = new GetMethod(url.toString());
		String xpath = "count(/response/index[@name='" + indexName + "'])";
		call(method, xpath, "1", "Index not found");
	}
}
