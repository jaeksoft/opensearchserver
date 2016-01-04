/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2015 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of OpenSearchServer.
 *
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.webservice.replication;

import java.lang.Thread.State;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.jaeksoft.searchlib.replication.ReplicationItem;
import com.jaeksoft.searchlib.replication.ReplicationMaster;
import com.jaeksoft.searchlib.replication.ReplicationThread;
import com.jaeksoft.searchlib.replication.ReplicationType;
import com.jaeksoft.searchlib.webservice.CommonResult;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "result")
@JsonInclude(Include.NON_NULL)
public class ReplicationResult extends CommonResult {

	public final String name;
	public final ReplicationType replicationType;
	public final String remoteUrl;
	public final String remoteLogin;
	public final String remoteApiKey;
	public final String remoteIndexName;
	public final Integer secTimeOut;
	public final Boolean isActiveThread;
	public final ThreadResult lastThread;

	@XmlAccessorType(XmlAccessType.FIELD)
	@JsonInclude(Include.NON_NULL)
	public static class ThreadResult {

		public final String info;
		public final State state;
		public final Long durationMs;
		public final Date startDate;

		public ThreadResult() {
			info = null;
			state = null;
			durationMs = null;
			startDate = null;
		}

		public ThreadResult(ReplicationThread thread) {
			info = thread.getStatInfo();
			state = thread.getThreadState();
			durationMs = thread.getDuration();
			startDate = new Date(thread.getStartTime());
		}
	}

	public ReplicationResult() {
		name = null;
		replicationType = null;
		remoteUrl = null;
		remoteLogin = null;
		remoteApiKey = null;
		remoteIndexName = null;
		secTimeOut = null;
		isActiveThread = null;
		lastThread = null;
	}

	ReplicationResult(Boolean successful, ReplicationItem replicationItem)
			throws MalformedURLException, URISyntaxException {
		super(successful, null);
		name = replicationItem.getName();
		replicationType = replicationItem.getReplicationType();
		remoteUrl = replicationItem.getInstanceUrl();
		remoteLogin = replicationItem.getLogin();
		remoteApiKey = replicationItem.getApiKey();
		remoteIndexName = replicationItem.getIndexName();
		secTimeOut = replicationItem.getSecTimeOut();
		isActiveThread = replicationItem.isThread();
		ReplicationThread lt = replicationItem.getLastThread();
		lastThread = lt == null ? null : new ThreadResult(lt);
	}

	static List<ReplicationResult> toArray(ReplicationItem[] replicationItemArray)
			throws MalformedURLException, URISyntaxException {
		List<ReplicationResult> replicationResultList = new ArrayList<ReplicationResult>();
		if (replicationItemArray == null)
			return replicationResultList;
		for (ReplicationItem replicationItem : replicationItemArray)
			replicationResultList.add(new ReplicationResult(null, replicationItem));
		return replicationResultList;
	}

	ReplicationItem getReplicationItem(ReplicationMaster replicationMaster)
			throws MalformedURLException, URISyntaxException {
		ReplicationItem replicationItem = new ReplicationItem(replicationMaster);
		if (replicationType != null)
			replicationItem.setReplicationType(replicationType);
		if (remoteUrl != null)
			replicationItem.setInstanceUrl(remoteUrl);
		if (remoteLogin != null)
			replicationItem.setLogin(remoteLogin);
		if (remoteApiKey != null)
			replicationItem.setApiKey(remoteApiKey);
		if (remoteIndexName != null)
			replicationItem.setIndexName(remoteIndexName);
		if (secTimeOut != null)
			replicationItem.setSecTimeOut(secTimeOut);
		return replicationItem;
	}
}
