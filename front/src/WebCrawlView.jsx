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
import {fetchJson, parseJson} from "./fetchJson.js"


const WebCrawlView = (props) => {

  const collectorFactory = "com.jaeksoft.opensearchserver.CrawlerCollector$Web";

  const [error, setError] = useState(null);
  const [task, setTask] = useState(null);
  const [spinning, setSpinning] = useState(false);
  const [sessions, setSessions] = useState([]);
  const [sessionEdit, setSessionEdit] = useState('');

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
                                onClick={() => doStartWebCrawl()}/>
          <div className="query-help">
          </div>
        </div>
        <div className="right-column border bg-light">

        </div>
      </div>
    </div>
  )

  function doFetchWebCrawlSessions() {
    setError(null);
    fetchJson(props.oss + '/ws/crawler/web/sessions', null,
      json => {
        setSessions(json);
      },
      error => setError(error)
    );
  }

  function doStartWebCrawl() {
    const parsedJson = parseJson(props.webCrawlDefinition);
    parsedJson.crawl_collector_factory = collectorFactory;
    setError(null);
    fetchJson(
      props.oss + '/ws/crawler/web/sessions/' + sessionEdit,
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
        <button className="btn btn-primary" type="button" id="button-addon2"
                onClick={props.onClick}>Start crawl
        </button>
      </div>
    </div>
  );
}

export default hot(WebCrawlView);
