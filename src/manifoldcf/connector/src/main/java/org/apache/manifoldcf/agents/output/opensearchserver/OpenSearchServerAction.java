package org.apache.manifoldcf.agents.output.opensearchserver;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.manifoldcf.core.interfaces.ManifoldCFException;

public class OpenSearchServerAction extends OpenSearchServerConnection {

	public enum CommandEnum {
		optimize, reload;
	}

	public OpenSearchServerAction(CommandEnum cmd, OpenSearchServerParam params)
			throws ManifoldCFException {
		super(params);
		StringBuffer url = getApiUrl("action");
		url.append("&action=");
		url.append(cmd.name());
		GetMethod method = new GetMethod(url.toString());
		String xPath = "/response/entry[key='Status']/text()";
		call(method, xPath, "OK", "Command " + cmd.name() + "failed");
	}
}
