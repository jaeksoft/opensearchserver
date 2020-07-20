/*
 * Copyright 2017-2020 Emmanuel Keller / Jaeksoft
 *  <p>
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  <p>
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  <p>
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import {hot} from 'react-hot-loader/root';
import React, {useState, useEffect} from 'react';
import Status from "./Status";
import JsonEditor from "./JsonEditor";
import {fetchJson, parseJson, simpleFetch} from "./fetchUtils.js"
import WebCrawlSessionList from "./WebCrawlSessionList";
import View from "./View";
import CreateOrDeleteButton from "./CreateOrDeleteButton";

const WebCrawlView = (props) => {

  const collectorFactory = "com.jaeksoft.opensearchserver.CrawlerCollector$Web";

  const [error, setError] = useState(null);
  const [task, setTask] = useState(null);
  const [spinning, setSpinning] = useState(false);
  const [sessions, setSessions] = useState([]);
  const [sessionEdit, setSessionEdit] = useState('');
  const [sessionStatus, setSessionStatus] = useState('');

  useEffect(() => {
    doFetchWebCrawlSessions();
  }, [])

  return (
    <div className="tri-view">
      <div className="bg-light text-secondary p-1">WEB CRAWLS&nbsp;
        <Status task={task} error={error} spinning={spinning}/>
      </div>
      <div className="central border bg-light">
        <div className="left-column border bg-light">
          <div className="query-json">
            <JsonEditor value={props.webCrawlDefinition}
                        setValue={props.setWebCrawlDefinition}
            />
          </div>
          <CreateWebCrawlButton sessionEdit={sessionEdit}
                                setSessionEdit={setSessionEdit}
                                selectedSession={props.selectedWebSession}
                                doCreateSession={name => doStartWebCrawl(name)}
                                doDeleteSession={name => doDeleteWebCrawl(name)}/>
          <div className="query-help">
            <JsonEditor value={sessionStatus}/>
          </div>
        </div>
        <div className="right-column border bg-light">
          <WebCrawlSessionList sessions={sessions}
                               selectedSession={props.selectedWebSession}
                               setSelectedSession={name => doSelectWebCrawlSession(name)}/>
        </div>
      </div>
    </div>
  )

  function doSelectWebCrawlSession(sessionName) {
    if (!sessionName)
      return;
    props.setSelectedWebSession(sessionName);
    doFetchSessionDefinition(sessionName);
    doFetchSessionStatus(sessionName);
    doFetchWebCrawlSessions();
  }

  function doFetchWebCrawlSessions() {
    setError(null);
    fetchJson(props.oss + '/ws/crawler/web/sessions', null,
      json => {
        setSessions(json);
      },
      error => setError(error)
    );
  }

  function doFetchSessionDefinition(sessionName) {
    if (!sessionName)
      return;
    setError(null);
    fetchJson(props.oss + '/ws/crawler/web/sessions/' + sessionName + '/definition', null,
      json => {
        delete json.crawl_collector_factory;
        props.setWebCrawlDefinition(JSON.stringify(json, undefined, 2));
      },
      error => setError(error)
    );
  }

  function doFetchSessionStatus(sessionName) {
    if (!sessionName)
      return;
    setError(null);
    fetchJson(props.oss + '/ws/crawler/web/sessions/' + sessionName, null,
      json => {
        setSessionStatus(JSON.stringify(json, undefined, 2));
      },
      error => setError(error)
    );
  }

  function doStartWebCrawl(sessionName) {
    if (!sessionName)
      return;
    const parsedJson = parseJson(props.webCrawlDefinition);
    parsedJson.crawl_collector_factory = collectorFactory;
    setError(null);
    fetchJson(
      props.oss + '/ws/crawler/web/sessions/' + sessionName,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(parsedJson)
      },
      json => {
        doFetchWebCrawlSessions();
      },
      error => setError(error)
    );
  }

  function doDeleteWebCrawl(sessionName) {
    simpleFetch(props.oss + '/ws/crawler/web/sessions/' + sessionName + '/definition',
      {method: 'DELETE'},
      ok => {
        props.setSelectedWebSession('');
        setSessionStatus('');
        doFetchWebCrawlSessions();
      },
      error => setError(error));
  }
}

const CreateWebCrawlButton = (props) => {
  return (
    <div className="input-group mb-3">
      <input type="text"
             className="form-control"
             placeholder="Session name"
             aria-label="Session name"
             aria-describedby="button-addon2"
             value={props.sessionEdit}
             onChange={e => props.setSessionEdit(e.target.value)}
      />
      <div className="input-group-append">
        <CreateOrDeleteButton
          name={props.sessionEdit}
          selectedName={props.selectedSession}
          doDelete={name => props.doDeleteSession(name)}
          doCreate={name => props.doCreateSession(name)}
        />
      </div>
    </div>
  );
}

export default hot(WebCrawlView);
