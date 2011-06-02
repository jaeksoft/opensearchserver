package org.apache.manifoldcf.agents.output.opensearchserver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.manifoldcf.agents.interfaces.IOutputAddActivity;
import org.apache.manifoldcf.agents.interfaces.IOutputConnector;
import org.apache.manifoldcf.agents.interfaces.IOutputNotifyActivity;
import org.apache.manifoldcf.agents.interfaces.IOutputRemoveActivity;
import org.apache.manifoldcf.agents.interfaces.OutputSpecification;
import org.apache.manifoldcf.agents.interfaces.RepositoryDocument;
import org.apache.manifoldcf.agents.interfaces.ServiceInterruption;
import org.apache.manifoldcf.core.interfaces.ConfigParams;
import org.apache.manifoldcf.core.interfaces.Configuration;
import org.apache.manifoldcf.core.interfaces.IHTTPOutput;
import org.apache.manifoldcf.core.interfaces.IPostParameters;
import org.apache.manifoldcf.core.interfaces.IThreadContext;
import org.apache.manifoldcf.core.interfaces.ManifoldCFException;

public class OpenSearchServerConnector implements IOutputConnector {

	@Override
	public String check() throws ManifoldCFException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clearThreadContext() {
		// TODO Auto-generated method stub

	}

	@Override
	public void connect(ConfigParams arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deinstall(IThreadContext arg0) throws ManifoldCFException {
		// TODO Auto-generated method stub

	}

	@Override
	public void disconnect() throws ManifoldCFException {
		// TODO Auto-generated method stub

	}

	@Override
	public ConfigParams getConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void install(IThreadContext arg0) throws ManifoldCFException {
		// TODO Auto-generated method stub

	}

	@Override
	public void outputConfigurationBody(IThreadContext arg0, IHTTPOutput arg1,
			ConfigParams arg2, String arg3) throws ManifoldCFException,
			IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void outputConfigurationHeader(IThreadContext arg0,
			IHTTPOutput arg1, ConfigParams arg2, ArrayList arg3)
			throws ManifoldCFException, IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void poll() throws ManifoldCFException {
		// TODO Auto-generated method stub

	}

	@Override
	public String processConfigurationPost(IThreadContext arg0,
			IPostParameters arg1, ConfigParams arg2) throws ManifoldCFException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setThreadContext(IThreadContext arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void viewConfiguration(IThreadContext arg0, IHTTPOutput arg1,
			ConfigParams arg2) throws ManifoldCFException, IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public int addOrReplaceDocument(String arg0, String arg1,
			RepositoryDocument arg2, String arg3, IOutputAddActivity arg4)
			throws ManifoldCFException, ServiceInterruption {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean checkDocumentIndexable(File arg0)
			throws ManifoldCFException, ServiceInterruption {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean checkMimeTypeIndexable(String arg0)
			throws ManifoldCFException, ServiceInterruption {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String[] getActivitiesList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getOutputDescription(OutputSpecification arg0)
			throws ManifoldCFException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void noteJobComplete(IOutputNotifyActivity arg0)
			throws ManifoldCFException, ServiceInterruption {
		// TODO Auto-generated method stub

	}

	@Override
	public void outputSpecificationBody(IHTTPOutput arg0,
			OutputSpecification arg1, String arg2) throws ManifoldCFException,
			IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void outputSpecificationHeader(IHTTPOutput arg0,
			OutputSpecification arg1, ArrayList arg2)
			throws ManifoldCFException, IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public String processSpecificationPost(IPostParameters arg0,
			OutputSpecification arg1) throws ManifoldCFException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeDocument(String arg0, String arg1,
			IOutputRemoveActivity arg2) throws ManifoldCFException,
			ServiceInterruption {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean requestInfo(Configuration arg0, String arg1)
			throws ManifoldCFException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void viewSpecification(IHTTPOutput arg0, OutputSpecification arg1)
			throws ManifoldCFException, IOException {
		// TODO Auto-generated method stub

	}

}
