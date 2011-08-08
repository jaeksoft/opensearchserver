package org.apache.manifoldcf.agents.output.opensearchserver;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.manifoldcf.agents.output.opensearchserver.OpenSearchServerParam.ParameterEnum;
import org.apache.manifoldcf.core.interfaces.ManifoldCFException;

public class OpenSearchServerConnection {

	private String serverLocation;

	private String indexName;

	private String userName;

	private String apiKey;

	private String resultCode;

	private String resultDescription;

	private String callUrlSnippet;

	protected OpenSearchServerConnection(OpenSearchServerParam params) {
		resultCode = null;
		resultDescription = null;
		callUrlSnippet = null;
		serverLocation = params.get(ParameterEnum.SERVERLOCATION);
		indexName = params.get(ParameterEnum.INDEXNAME);
		userName = params.get(ParameterEnum.USERNAME);
		apiKey = params.get(ParameterEnum.APIKEY);
	}

	protected final String urlEncode(String t) throws ManifoldCFException {
		try {
			return URLEncoder.encode(t, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new ManifoldCFException(e);
		}
	}

	protected StringBuffer getApiUrl(String command) throws ManifoldCFException {
		StringBuffer url = new StringBuffer(serverLocation);
		if (!serverLocation.endsWith("/"))
			url.append('/');
		url.append(command);
		url.append("?use=");
		url.append(urlEncode(indexName));
		callUrlSnippet = url.toString();
		if (userName != null && apiKey != null && userName.length() > 0
				&& apiKey.length() > 0) {
			url.append("&login=");
			url.append(urlEncode(userName));
			url.append("&key=");
			url.append(apiKey);
		}
		return url;
	}

	protected void call(HttpMethod method) throws ManifoldCFException {
		HttpClient hc = new HttpClient();
		try {
			hc.executeMethod(method);
		} catch (HttpException e) {
			throw new ManifoldCFException(e);
		} catch (IOException e) {
			throw new ManifoldCFException(e);
		} finally {
			if (method != null) {
				setResultCode(method.getStatusCode());
				setResultDescription(method.getStatusText());
				method.releaseConnection();
			}
		}
	}

	private void setResultDescription(String desc) {
		resultDescription = desc;
	}

	public String getResultDescription() {
		return resultDescription;
	}

	private void setResultCode(int code) {
		switch (code) {
		case 0:
			resultCode = null;
			break;
		case 200:
			resultCode = "OK";
			break;
		default:
			resultCode = "ERR (" + code + ")";
			break;
		}
	}

	public String getResultCode() {
		return resultCode;
	}

	public String getCallUrlSnippet() {
		return callUrlSnippet;
	}
}
