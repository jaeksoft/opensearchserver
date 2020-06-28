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
import {fetchJson} from "./fetchJson.js"
import ReactAce from "react-ace";
import {QuestionCircleFill} from 'react-bootstrap-icons';

const QueryHelper = (props) => {

  const [queryTypes, setQueryTypes] = useState([]);
  const [lastQueryJsonLookup, setLastQueryJsonLookup] = useState('');
  const [queryLookup, setQueryLookup] = useState('');
  const [firstQueryTypeDetail, setFirstQueryTypeDetail] = useState('');

  useEffect(() => {
  }, [props.selectedIndex, props.queryJson])

  if (!props.selectedIndex)
    return null;

  try {
    const newLookup = Object.keys(JSON.parse(props.queryJson).query)[0] || '';
    if (newLookup !== lastQueryJsonLookup) {
      setLastQueryJsonLookup(newLookup);
      doFetchQueryTypes(newLookup);
    }
  } catch (err) {
  }

  const listItems = Object.keys(queryTypes).map((queryType, i) => (
    <li className="list-group-item pl-1 pr-1 pt-1 pb-0"
        key={i}
        onClick={e => doFetchQueryTypes(queryType)}>
      <a href={queryTypes[queryType]}
         target={"_blank"}
         className="float-right">
        <QuestionCircleFill/>
      </a>
      <HelpItem pos={i}
                queryType={queryType}
                queryTypeDetail={firstQueryTypeDetail}/>
    </li>
  ));

  return (
    <React.Fragment>
      <input className="form-control" value={queryLookup}
             onChange={e => doFetchQueryTypes(e.target.value)}/>
      <div className="list-group">
        {listItems}
      </div>
    </React.Fragment>
  );

  function doFetchQueryTypes(lookup) {
    if (!props.selectedIndex) {
      return;
    }
    setQueryLookup(lookup);
    fetchJson(props.oss + '/ws/indexes/' + props.selectedIndex
      + '/search/queries/types?lookup=' + lookup, null,
      json => {
        doSetQueryTypes(json);
      });
  }

  function doSetQueryTypes(json) {
    setQueryTypes(json);
    const firstType = Object.keys(json)[0] || '';
    if (firstType) {
      fetchJson(props.oss + '/ws/indexes/' + props.selectedIndex
        + '/search/queries/types/' + firstType, null,
        json => {
          setFirstQueryTypeDetail(JSON.stringify(json, undefined, 2));
        });
    }
  }
}

const HelpItem = (props) => {
  if (props.pos == 0) {
    return (
      <pre className="small">
        <code>
        {props.queryTypeDetail || props.queryType}
      </code>
      </pre>
    )
  } else {
    return (
      <small>{props.queryType}</small>
    )
  }
}


export default hot(QueryHelper, HelpItem);

