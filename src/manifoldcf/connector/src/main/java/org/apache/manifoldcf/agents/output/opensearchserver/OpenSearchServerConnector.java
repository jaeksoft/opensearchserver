package org.apache.manifoldcf.agents.output.opensearchserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Set;

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
			Set<OpenSearchServerParam> params) throws ManifoldCFException {
		InputStream is = getClass().getResourceAsStream(resName);
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String line;
			while ((line = br.readLine()) != null)
				out.println(OpenSearchServerParam.replace(line, params));
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
		tabsArray.add("Parameters");
	}

	@Override
	public void outputConfigurationBody(IThreadContext threadContext,
			IHTTPOutput out, ConfigParams parameters, String tabName)
			throws ManifoldCFException, IOException {
		super.outputConfigurationBody(threadContext, out, parameters, tabName);
		Set<OpenSearchServerParam> params = OpenSearchServerParam
				.getParameters(OpenSearchServerParam.CONFIGURATIONLIST,
						parameters);
		if ("Parameters".equals(tabName))
			outputResource("configuration.html", out, params);
	}

	@Override
	public void viewConfiguration(IThreadContext threadContext,
			IHTTPOutput out, ConfigParams parameters)
			throws ManifoldCFException, IOException {
		Set<OpenSearchServerParam> params = OpenSearchServerParam
				.getParameters(OpenSearchServerParam.CONFIGURATIONLIST,
						parameters);
		outputResource("view.html", out, params);
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
					inputStream);
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
		System.out.println("removeDocument " + documentURI);
		System.out.flush();
	}

}
