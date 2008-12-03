package com.jaeksoft.searchlib.web;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import org.zkoss.zul.Window;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.index.IndexAbstract;
import com.jaeksoft.searchlib.index.IndexGroup;

public class ZkController extends Window {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3581269068713587866L;

	private Client client;

	public ZkController() throws SearchLibException, NamingException {
		super();
		System.out.println("ZkController " + this + " " + this.hashCode());
		client = Client.getWebAppInstance();
	}

	public List<IndexAbstract> getIndices() throws SearchLibException,
			NamingException {
		List<IndexAbstract> list = new ArrayList<IndexAbstract>();
		IndexAbstract index = client.getIndex();
		if (index instanceof IndexGroup) {
			for (IndexAbstract idx : ((IndexGroup) index).getIndices())
				list.add(idx);
		} else
			list.add(index);
		return list;
	}

	public List<?> getAnalyzers() {
		return client.getSchema().getAnalyzerList();
	}
}
