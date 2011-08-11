package org.apache.manifoldcf.agents.output.opensearchserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.IOUtils;
import org.apache.manifoldcf.agents.interfaces.IOutputAddActivity;
import org.apache.manifoldcf.agents.interfaces.IOutputNotifyActivity;
import org.apache.manifoldcf.agents.interfaces.IOutputRemoveActivity;
import org.apache.manifoldcf.agents.interfaces.OutputSpecification;
import org.apache.manifoldcf.agents.interfaces.RepositoryDocument;
import org.apache.manifoldcf.agents.interfaces.ServiceInterruption;
import org.apache.manifoldcf.agents.output.BaseOutputConnector;
import org.apache.manifoldcf.agents.output.opensearchserver.OpenSearchServerAction.CommandEnum;
import org.apache.manifoldcf.core.interfaces.ConfigParams;
import org.apache.manifoldcf.core.interfaces.ConfigurationNode;
import org.apache.manifoldcf.core.interfaces.IHTTPOutput;
import org.apache.manifoldcf.core.interfaces.IPostParameters;
import org.apache.manifoldcf.core.interfaces.IThreadContext;
import org.apache.manifoldcf.core.interfaces.ManifoldCFException;
import org.apache.manifoldcf.core.interfaces.SpecificationNode;

public class OpenSearchServerConnector extends BaseOutputConnector {

	private final static String OPENSEARCHSERVER_INDEXATION_ACTIVITY = "Indexation";
	private final static String OPENSEARCHSERVER_DELETION_ACTIVITY = "Deletion";
	private final static String OPENSEARCHSERVER_OPTIMIZE_ACTIVITY = "Optimize";

	private final static String[] OPENSEARCHSERVER_ACTIVITIES = {
			OPENSEARCHSERVER_INDEXATION_ACTIVITY,
			OPENSEARCHSERVER_DELETION_ACTIVITY,
			OPENSEARCHSERVER_OPTIMIZE_ACTIVITY };

	private final static String OPENSEARCHSERVER_TAB_PARAMETER = "Parameters";
	private final static String OPENSEARCHSERVER_TAB_OPENSEARCHSERVER = "OpenSearchServer";

	// private final static String OPENSEARCHSERVER_TAB_FIELDMAPPING =
	// "Field mapping";

	@Override
	public String[] getActivitiesList() {
		return OPENSEARCHSERVER_ACTIVITIES;
	}

	/**
	 * Read the content of a resource, replace the variable ${PARAMNAME} with
	 * the value and copy it to the out.
	 * 
	 * @param resName
	 * @param out
	 * @throws ManifoldCFException
	 */
	private void outputResource(String resName, IHTTPOutput out,
			OpenSearchServerParam params) throws ManifoldCFException {
		InputStream is = getClass().getResourceAsStream(resName);
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String line;
			while ((line = br.readLine()) != null)
				if (params != null)
					out.println(params.replace(line));
				else
					out.println(line);
		} catch (UnsupportedEncodingException e) {
			throw new ManifoldCFException(e);
		} catch (IOException e) {
			throw new ManifoldCFException(e);
		} finally {
			if (br != null)
				IOUtils.closeQuietly(br);
			if (is != null)
				IOUtils.closeQuietly(is);
		}
	}

	@Override
	public void outputConfigurationHeader(IThreadContext threadContext,
			IHTTPOutput out, ConfigParams parameters, List<String> tabsArray)
			throws ManifoldCFException, IOException {
		super.outputConfigurationHeader(threadContext, out, parameters,
				tabsArray);
		tabsArray.add(OPENSEARCHSERVER_TAB_PARAMETER);
		outputResource("configuration.js", out, null);
	}

	@Override
	public void outputConfigurationBody(IThreadContext threadContext,
			IHTTPOutput out, ConfigParams parameters, String tabName)
			throws ManifoldCFException, IOException {
		super.outputConfigurationBody(threadContext, out, parameters, tabName);
		if (OPENSEARCHSERVER_TAB_PARAMETER.equals(tabName)) {
			outputResource("configuration.html", out, getParameters(parameters));
		}
	}

	@Override
	public void outputSpecificationHeader(IHTTPOutput out,
			OutputSpecification os, List<String> tabsArray)
			throws ManifoldCFException, IOException {
		super.outputSpecificationHeader(out, os, tabsArray);
		tabsArray.add(OPENSEARCHSERVER_TAB_OPENSEARCHSERVER);
	}

	final private SpecificationNode getSpecNode(OutputSpecification os) {
		int l = os.getChildCount();
		for (int i = 0; i < l; i++) {
			SpecificationNode node = os.getChild(i);
			if (OpenSearchServerParam.OPENSEARCHSERVER_SPECS_NODE.equals(node
					.getType())) {
				return node;
			}
		}
		return null;
	}

	@Override
	public void outputSpecificationBody(IHTTPOutput out,
			OutputSpecification os, String tabName) throws ManifoldCFException,
			IOException {
		super.outputSpecificationBody(out, os, tabName);
		if (OPENSEARCHSERVER_TAB_OPENSEARCHSERVER.equals(tabName)) {
			outputResource("specifications.html", out, getSpecParameters(os));
		}
	}

	@Override
	public String processSpecificationPost(IPostParameters variableContext,
			OutputSpecification os) throws ManifoldCFException {
		ConfigurationNode specNode = getSpecNode(os);
		boolean bAdd = (specNode == null);
		if (bAdd) {
			specNode = new SpecificationNode(
					OpenSearchServerParam.OPENSEARCHSERVER_SPECS_NODE);
		}
		OpenSearchServerParam.contextToSpecNode(
				OpenSearchServerParam.SPECIFICATIONLIST, variableContext,
				specNode);
		if (bAdd)
			os.addChild(os.getChildCount(), specNode);
		return null;
	}

	/**
	 * Build a Set of OpenSearchServerParam. If configParams is null,
	 * getConfiguration() is used.
	 * 
	 * @param configParams
	 * @return
	 */
	final private OpenSearchServerParam getParameters(ConfigParams configParams) {
		if (configParams == null)
			configParams = getConfiguration();
		OpenSearchServerParam parameters = new OpenSearchServerParam(
				OpenSearchServerParam.CONFIGURATIONLIST, configParams);
		return parameters;
	}

	final private OpenSearchServerParam getSpecParameters(OutputSpecification os) {
		return new OpenSearchServerParam(
				OpenSearchServerParam.SPECIFICATIONLIST, getSpecNode(os));
	}

	@Override
	public String getOutputDescription(OutputSpecification os) {
		OpenSearchServerParam ossParam = new OpenSearchServerParam(
				OpenSearchServerParam.SPECIFICATIONLIST, getSpecNode(os));
		return ossParam.get(OpenSearchServerParam.ParameterEnum.MAXFILESIZE);
	}

	@Override
	public boolean checkLengthIndexable(String outputDescription, long length)
			throws ManifoldCFException, ServiceInterruption {
		long maxFileSize = Long.parseLong(outputDescription);
		if (length > maxFileSize)
			return false;
		return super.checkLengthIndexable(outputDescription, length);
	}

	@Override
	public void viewConfiguration(IThreadContext threadContext,
			IHTTPOutput out, ConfigParams parameters)
			throws ManifoldCFException, IOException {
		outputResource("view.html", out, getParameters(parameters));
	}

	@Override
	public void viewSpecification(IHTTPOutput out, OutputSpecification os)
			throws ManifoldCFException, IOException {
		outputResource("viewSpec.html", out, getSpecParameters(os));
	}

	@Override
	public String processConfigurationPost(IThreadContext threadContext,
			IPostParameters variableContext, ConfigParams parameters)
			throws ManifoldCFException {
		OpenSearchServerParam.contextToConfig(
				OpenSearchServerParam.CONFIGURATIONLIST, variableContext,
				parameters);
		return null;
	}

	private static Map<String, Integer> ossInstances = null;

	private synchronized final Integer addInstance(OpenSearchServerParam param) {
		if (ossInstances == null)
			ossInstances = new TreeMap<String, Integer>();
		synchronized (ossInstances) {
			String uii = param.getUniqueIndexIdentifier();
			Integer count = ossInstances.get(uii);
			if (count == null) {
				count = new Integer(1);
				ossInstances.put(uii, count);
			} else
				count++;
			return count;
		}
	}

	private synchronized final void removeInstance(OpenSearchServerParam param) {
		if (ossInstances == null)
			return;
		synchronized (ossInstances) {
			String uii = param.getUniqueIndexIdentifier();
			Integer count = ossInstances.get(uii);
			if (count == null)
				return;
			if (--count == 0)
				ossInstances.remove(uii);
		}
	}

	@Override
	public int addOrReplaceDocument(String documentURI,
			String outputDescription, RepositoryDocument document,
			String authorityNameString, IOutputAddActivity activities)
			throws ManifoldCFException, ServiceInterruption {
		OpenSearchServerParam param = getParameters(null);
		Integer count = addInstance(param);
		synchronized (count) {
			InputStream inputStream = document.getBinaryStream();
			try {
				long startTime = System.currentTimeMillis();
				OpenSearchServerIndex oi = new OpenSearchServerIndex(
						documentURI, inputStream, param);
				activities.recordActivity(startTime,
						OPENSEARCHSERVER_INDEXATION_ACTIVITY, document
								.getBinaryLength(), documentURI, oi.getResult()
								.name(), oi.getResultDescription());
			} finally {
				IOUtils.closeQuietly(inputStream);
				removeInstance(param);
			}
			return DOCUMENTSTATUS_ACCEPTED;
		}
	}

	@Override
	public void removeDocument(String documentURI, String outputDescription,
			IOutputRemoveActivity activities) throws ManifoldCFException,
			ServiceInterruption {
		long startTime = System.currentTimeMillis();
		OpenSearchServerDelete od = new OpenSearchServerDelete(documentURI,
				getParameters(null));
		activities.recordActivity(startTime,
				OPENSEARCHSERVER_DELETION_ACTIVITY, null, documentURI, od
						.getResult().name(), od.getResultDescription());
	}

	@Override
	public String check() throws ManifoldCFException {
		OpenSearchServerSchema oss = new OpenSearchServerSchema(
				getParameters(null));
		return oss.getResult().name() + " " + oss.getResultDescription();
	}

	@Override
	public void noteJobComplete(IOutputNotifyActivity activities)
			throws ManifoldCFException, ServiceInterruption {
		long startTime = System.currentTimeMillis();
		OpenSearchServerAction oo = new OpenSearchServerAction(
				CommandEnum.optimize, getParameters(null));
		activities.recordActivity(startTime,
				OPENSEARCHSERVER_OPTIMIZE_ACTIVITY, null,
				oo.getCallUrlSnippet(), oo.getResult().name(),
				oo.getResultDescription());
	}

}
