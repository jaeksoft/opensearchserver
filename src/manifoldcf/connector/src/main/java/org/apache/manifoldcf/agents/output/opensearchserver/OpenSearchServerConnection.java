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

	protected String call(HttpMethod method, String xPathQuery)
			throws ManifoldCFException {
		HttpClient hc = new HttpClient();
		try {
			hc.executeMethod(method);
			if (xPathQuery != null)
				return checkXmlResponse(xPathQuery,
						method.getResponseBodyAsStream());
			else
				return IOUtils.toString(method.getResponseBodyAsStream());
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
