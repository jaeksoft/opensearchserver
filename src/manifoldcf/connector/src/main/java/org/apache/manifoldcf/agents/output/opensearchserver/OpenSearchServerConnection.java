package org.apache.manifoldcf.agents.output.opensearchserver;

import java.io.IOException;
import java.io.StringReader;
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
import org.apache.manifoldcf.core.interfaces.ManifoldCFException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class OpenSearchServerConnection {

	private String serverLocation;

	private String indexName;

	private String userName;

	private String apiKey;

	private String resultDescription;

	private String callUrlSnippet;

	private String response;

	private Document xmlResponse;

	protected String xPathStatus = "/response/entry[@key='Status']/text()";
	protected String xPathException = "/response/entry[@key='Exception']/text()";

	public enum Result {
		OK, ERROR, UNKNOWN;
	}

	private Result result;

	protected OpenSearchServerConnection(OpenSearchServerConfig config) {
		result = Result.UNKNOWN;
		response = null;
		xmlResponse = null;
		resultDescription = "";
		callUrlSnippet = null;
		serverLocation = config.getServerLocation();
		indexName = config.getIndexName();
		userName = config.getUserName();
		apiKey = config.getApiKey();
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
			if (!checkResultCode(method.getStatusCode()))
				throw new ManifoldCFException(getResultDescription());
			response = IOUtils.toString(method.getResponseBodyAsStream());
		} catch (HttpException e) {
			setResult(Result.ERROR, e.getMessage());
			throw new ManifoldCFException(e);
		} catch (IOException e) {
			setResult(Result.ERROR, e.getMessage());
			throw new ManifoldCFException(e);
		} finally {
			if (method != null)
				method.releaseConnection();
		}
	}

	private void readXmlResponse() throws ManifoldCFException {
		if (xmlResponse != null)
			return;
		StringReader sw = null;
		try {
			sw = new StringReader(response);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true); // never forget this!
			DocumentBuilder builder;
			builder = dbf.newDocumentBuilder();
			xmlResponse = builder.parse(new InputSource(sw));
		} catch (ParserConfigurationException e) {
			throw new ManifoldCFException(e);
		} catch (SAXException e) {
			throw new ManifoldCFException(e);
		} catch (IOException e) {
			throw new ManifoldCFException(e);
		} finally {
			if (sw != null)
				IOUtils.closeQuietly(sw);
		}
	}

	protected String checkXPath(String xPathQuery) throws ManifoldCFException {
		try {
			readXmlResponse();
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			XPathExpression xPathExpr = xpath.compile(xPathQuery);
			return xPathExpr.evaluate(xmlResponse);
		} catch (XPathExpressionException e) {
			throw new ManifoldCFException(e);
		}
	}

	protected void setResult(Result res, String desc) {
		if (res != null)
			result = res;
		if (desc != null)
			if (desc.length() > 0)
				resultDescription = desc;
	}

	public String getResultDescription() {
		return resultDescription;
	}

	protected String getResponse() {
		return response;
	}

	private boolean checkResultCode(int code) {
		switch (code) {
		case 0:
			setResult(Result.UNKNOWN, null);
			return false;
		case 200:
			setResult(Result.OK, null);
			return true;
		case 404:
			setResult(Result.ERROR, "Server/page not found");
			return false;
		default:
			setResult(Result.ERROR, null);
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
