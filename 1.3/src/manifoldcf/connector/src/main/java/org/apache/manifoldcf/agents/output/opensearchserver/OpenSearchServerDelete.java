package org.apache.manifoldcf.agents.output.opensearchserver;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.manifoldcf.core.interfaces.ManifoldCFException;

public class OpenSearchServerDelete extends OpenSearchServerConnection {

	public OpenSearchServerDelete(String documentURI,
			OpenSearchServerConfig config) throws ManifoldCFException {
		super(config);
		StringBuffer url = getApiUrl("delete");
		url.append("&uniq=");
		url.append(urlEncode(documentURI));
		GetMethod method = new GetMethod(url.toString());
		call(method);
		if ("OK".equals(checkXPath(xPathStatus)))
			return;
		setResult(Result.ERROR, checkXPath(xPathException));
	}
}
