package org.apache.manifoldcf.agents.output.opensearchserver;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.manifoldcf.core.interfaces.ManifoldCFException;

public class OpenSearchServerDelete extends OpenSearchServerConnection {

	public OpenSearchServerDelete(String documentURI,
			OpenSearchServerParam params) throws ManifoldCFException {
		super(params);
		StringBuffer url = getApiUrl("delete");
		url.append("&uniq=");
		url.append(urlEncode(documentURI));
		GetMethod method = new GetMethod(url.toString());
		call(method);
	}
}
