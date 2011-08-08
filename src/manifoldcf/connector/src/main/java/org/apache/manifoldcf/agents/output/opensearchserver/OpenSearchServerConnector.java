package org.apache.manifoldcf.agents.output.opensearchserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.manifoldcf.agents.interfaces.IOutputAddActivity;
import org.apache.manifoldcf.agents.interfaces.IOutputRemoveActivity;
import org.apache.manifoldcf.agents.interfaces.RepositoryDocument;
import org.apache.manifoldcf.agents.interfaces.ServiceInterruption;
import org.apache.manifoldcf.agents.output.BaseOutputConnector;
import org.apache.manifoldcf.core.interfaces.ConfigParams;
import org.apache.manifoldcf.core.interfaces.IHTTPOutput;
import org.apache.manifoldcf.core.interfaces.IPostParameters;
import org.apache.manifoldcf.core.interfaces.IThreadContext;
import org.apache.manifoldcf.core.interfaces.ManifoldCFException;

public class OpenSearchServerConnector extends BaseOutputConnector {

	private final static String OPENSEARCHSERVER_INDEXATION_ACTIVITY = "Indexation";
	private final static String OPENSEARCHSERVER_DELETION_ACTIVTY = "Deletion";

	private final static String[] OPENSEARCHSERVER_ACTIVITIES = {
			OPENSEARCHSERVER_INDEXATION_ACTIVITY,
			OPENSEARCHSERVER_DELETION_ACTIVTY };

	private final static String OPENSEARCHSERVER_TAB_PARAMETER = "Parameters";

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
				out.println(params.replace(line));
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

	// @Override
	// public void outputSpecificationHeader(IHTTPOutput out,
	// OutputSpecification os, List<String> tabsArray)
	// throws ManifoldCFException, IOException {
	// super.outputSpecificationHeader(out, os, tabsArray);
	// tabsArray.add(OPENSEARCHSERVER_TAB_FIELDMAPPING);
	// }
	//
	// @Override
	// public void outputSpecificationBody(IHTTPOutput out,
	// OutputSpecification os, String tabName) throws ManifoldCFException,
	// IOException {
	// super.outputSpecificationBody(out, os, tabName);
	// if (OPENSEARCHSERVER_TAB_FIELDMAPPING.equals(tabName)) {
	// Set<OpenSearchServerParam> params = OpenSearchServerParam
	// .getParameters(OpenSearchServerParam.SPECIFICATIONLIST, os);
	// outputResource("fieldmapping.html", out, params);
	// }
	// }

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

	@Override
	public void viewConfiguration(IThreadContext threadContext,
			IHTTPOutput out, ConfigParams parameters)
			throws ManifoldCFException, IOException {
		outputResource("view.html", out, getParameters(parameters));
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

	@Override
	public int addOrReplaceDocument(String documentURI,
			String outputDescription, RepositoryDocument document,
			String authorityNameString, IOutputAddActivity activities)
			throws ManifoldCFException, ServiceInterruption {
		InputStream inputStream = document.getBinaryStream();
		try {
			long startTime = System.currentTimeMillis();
			OpenSearchServerIndex oi = new OpenSearchServerIndex(documentURI,
					inputStream, getParameters(null));
			activities.recordActivity(startTime,
					OPENSEARCHSERVER_INDEXATION_ACTIVITY,
					document.getBinaryLength(), documentURI,
					oi.getResultCode(), oi.getResultDescription());
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
		return DOCUMENTSTATUS_ACCEPTED;
	}

	@Override
	public void removeDocument(String documentURI, String outputDescription,
			IOutputRemoveActivity activities) throws ManifoldCFException,
			ServiceInterruption {
		long startTime = System.currentTimeMillis();
		OpenSearchServerDelete od = new OpenSearchServerDelete(documentURI,
				getParameters(null));
		activities.recordActivity(startTime, OPENSEARCHSERVER_DELETION_ACTIVTY,
				null, documentURI, od.getResultCode(),
				od.getResultDescription());
	}

}
