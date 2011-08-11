package org.apache.manifoldcf.agents.output.opensearchserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.io.IOUtils;
import org.apache.manifoldcf.agents.output.opensearchserver.OpenSearchServerParam.ParameterEnum;
import org.apache.manifoldcf.core.interfaces.ManifoldCFException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class OpenSearchServerConnection {

	private String serverLocation;

	private String indexName;

	private String userName;

	private String apiKey;

	private String resultDescription;

	private String callUrlSnippet;

	public enum Result {
		OK, ERROR, UNKNOWN;
	}

	private Result result;

	protected OpenSearchServerConnection(OpenSearchServerParam params) {
		result = Result.UNKNOWN;
		resultDescription = "";
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

	protected void call(HttpMethod method, String xPathQuery,
			String resultCheck, String errorMessage) throws ManifoldCFException {
		HttpClient hc = new HttpClient();
		try {
			hc.executeMethod(method);
			if (!checkResultCode(method.getStatusCode()))
				return;
			String result = null;
			if (xPathQuery != null)
				result = checkXmlResponse(xPathQuery,
						method.getResponseBodyAsStream());
			else
				result = IOUtils.toString(method.getResponseBodyAsStream());

			if (resultCheck != null)
				if (resultCheck.equals(result))
					setResult(Result.OK);
				else {
					setResult(Result.ERROR);
					if (errorMessage != null)
						setResultDescription(errorMessage);
				}
		} catch (HttpException e) {
			setResult(Result.ERROR);
			setResultDescription(e.getMessage());
			throw new ManifoldCFException(e);
		} catch (IOException e) {
			setResult(Result.ERROR);
			setResultDescription(e.getMessage());
			throw new ManifoldCFException(e);
		} finally {
			if (method != null)
				method.releaseConnection();
		}
	}

	private String checkXmlResponse(String xPathQuery, InputStream inputStream)
			throws ManifoldCFException {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true); // never forget this!
			DocumentBuilder builder;
			builder = dbf.newDocumentBuilder();
			Document doc = builder.parse(inputStream);
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			XPathExpression xPathExpr = xpath.compile(xPathQuery);
			return xPathExpr.evaluate(doc);
		} catch (ParserConfigurationException e) {
			throw new ManifoldCFException(e);
		} catch (SAXException e) {
			throw new ManifoldCFException(e);
		} catch (IOException e) {
			throw new ManifoldCFException(e);
		} catch (XPathExpressionException e) {
			throw new ManifoldCFException(e);
		}
	}

	private void setResultDescription(String desc) {
		resultDescription = desc == null ? "" : desc;
	}

	public String getResultDescription() {
		return resultDescription;
	}

	private void setResult(Result r) {
		this.result = r;
	}

	private boolean checkResultCode(int code) {
		switch (code) {
		case 0:
			setResult(Result.UNKNOWN);
			return false;
		case 200:
			setResult(Result.OK);
			return true;
		case 404:
			setResult(Result.ERROR);
			setResultDescription("Server/page not found");
			return false;
		default:
			setResult(Result.ERROR);
			return false;
		}
	}

	public Result getResult() {
		return result;
	}

	public String getCallUrlSnippet() {
		return callUrlSnippet;
	}
}
