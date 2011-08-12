package org.apache.manifoldcf.agents.output.opensearchserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;
import org.apache.manifoldcf.core.interfaces.ConfigurationNode;
import org.apache.manifoldcf.core.interfaces.IPostParameters;
import org.apache.manifoldcf.core.interfaces.ManifoldCFException;
import org.json.JSONException;
import org.json.JSONObject;

public class OpenSearchServerSpecs extends OpenSearchServerParam {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1859653440572662025L;

	final public static ParameterEnum[] SPECIFICATIONLIST = {
			ParameterEnum.MAXFILESIZE, ParameterEnum.MIMETYPES,
			ParameterEnum.EXTENSIONS };

	final public static String OPENSEARCHSERVER_SPECS_NODE = "OPENSEARCHSERVER_SPECS_NODE";

	private Set<String> extensionSet;

	private Set<String> mimeTypeSet;

	/**
	 * Build a set of OpenSearchServer parameters by reading an JSON object
	 * 
	 * @param json
	 * @throws JSONException
	 * @throws ManifoldCFException
	 */
	public OpenSearchServerSpecs(JSONObject json) throws JSONException,
			ManifoldCFException {
		super(SPECIFICATIONLIST);
		extensionSet = null;
		mimeTypeSet = null;
		for (ParameterEnum param : SPECIFICATIONLIST) {
			String value = null;
			value = json.getString(param.name());
			if (value == null)
				value = param.defaultValue;
			put(param, value);
		}
		extensionSet = createStringSet(getExtensions());
		mimeTypeSet = createStringSet(getMimeTypes());
	}

	/**
	 * Build a set of OpenSearchServer parameters by reading an instance of
	 * SpecificationNode.
	 * 
	 * @param node
	 * @throws ManifoldCFException
	 */
	public OpenSearchServerSpecs(ConfigurationNode node)
			throws ManifoldCFException {
		super(SPECIFICATIONLIST);
		for (ParameterEnum param : SPECIFICATIONLIST) {
			String value = null;
			if (node != null)
				value = node.getAttributeValue(param.name());
			if (value == null)
				value = param.defaultValue;
			put(param, value);
		}
		extensionSet = createStringSet(getExtensions());
		mimeTypeSet = createStringSet(getMimeTypes());
	}

	public static void contextToSpecNode(IPostParameters variableContext,
			ConfigurationNode specNode) {
		for (ParameterEnum param : SPECIFICATIONLIST) {
			String p = variableContext.getParameter(param.name().toLowerCase());
			if (p != null)
				specNode.setAttribute(param.name(), p);
		}
	}

	/**
	 * 
	 * @return a JSON representation of the parameter list
	 */
	public JSONObject toJson() {
		return new JSONObject(this);
	}

	public long getMaxFileSize() {
		return Long.parseLong(get(ParameterEnum.MAXFILESIZE));
	}

	public String getMimeTypes() {
		return get(ParameterEnum.MIMETYPES);
	}

	public String getExtensions() {
		return get(ParameterEnum.EXTENSIONS);
	}

	private final static TreeSet<String> createStringSet(String content)
			throws ManifoldCFException {
		TreeSet<String> set = new TreeSet<String>();
		BufferedReader br = null;
		StringReader sr = null;
		try {
			sr = new StringReader(content);
			br = new BufferedReader(sr);
			String line = null;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.length() > 0)
					set.add(line);
			}
			return set;
		} catch (IOException e) {
			throw new ManifoldCFException(e);
		} finally {
			if (br != null)
				IOUtils.closeQuietly(br);
		}
	}

	public boolean checkExtension(String extension) {
		return extensionSet.contains(extension);
	}

	public boolean checkMimeType(String mimeType) {
		return mimeTypeSet.contains(mimeType);
	}
}
