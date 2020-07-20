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
import React from 'react';

/**
 *
 * @param props values, selectedValue, doSelectValue
 * @returns {*}
 */
const WebCrawlSessionList = (props) => {

  if (!props.sessions)
    return null;

  const values = Object.keys(props.sessions);
  const listItems = values.map((sessionName, i) => (
    <WebCrawlSessionItem key={i}
                         sessionName={sessionName}
                         selectedSession={props.selectedSession}
                         setSelectedSession={props.setSelectedSession}
                         sessionStatus={props.sessions[sessionName]}
    />
  ));
  return (
    <table className="table table-hover table-sm table-striped table-light">
      <thead className="thead-light">
      <tr>
        <th>Session name</th>
        <th>Crawled</th>
        <th>Start</th>
        <th>End</th>
      </tr>
      </thead>
      <tbody>
      {listItems}
      </tbody>
    </table>

  );
}

/**
 *
 * @param props sessionName, selectedSession, setSelectedSession
 * @returns {*}
 */
const WebCrawlSessionItem = (props) => {
  const startTime = props.sessionStatus.start_time ? new Date(props.sessionStatus.start_time).toLocaleString() : null;
  const endTime = props.sessionStatus.end_time ? new Date(props.sessionStatus.end_time).toLocaleString() : null;
  const trClassName = props.selectedSession === props.sessionName ? "table-active" : null;
  return (
    <tr className={trClassName} onClick={() => props.setSelectedSession(props.sessionName)}>
      <td className="p-1 m-1">{props.sessionName}</td>
      <td className="p-1 m-1">{props.sessionStatus.crawled}</td>
      <td className="p-1 m-1">{startTime}</td>
      <td className="p-1 m-1">{endTime}</td>
    </tr>
  );
}

export default hot(WebCrawlSessionList);
