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
import CreateEditDelete from "./CreateEditDelete";
import List from "./List";
import {fetchJson} from "./fetchJson.js"

const IndexList = (props) => {

  const [spinning, setSpinning] = useState(false);
  const [indices, setIndices] = useState([]);
  const [error, setError] = useState(null);

  useEffect(() => {
    doFetchIndices();
  }, [props.selectedSchema])

  const items = Object.keys(indices).map((index, i) => (
    <option key={i} value={index}>{index}</option>
  ));

  return (
    <React.Fragment>
      <label className="sr-only" htmlFor={props.id}>Index :</label>
      <select id={props.id}
              className="custom-select"
              value={props.selectedIndex}
              onChange={e => props.setSelectedIndex(e.target.value)}>
        <option value="">Select an index</option>
        {items}
      </select>
    </React.Fragment>
  );

  function doFetchIndices() {
    const schema = props.selectedSchema;
    if (!schema) {
      return;
    }
    setSpinning(true);
    fetchJson(props.oss + '/ws/indexes/' + schema, null,
      json => {
        setSpinning(false);
        setIndices(json);
      },
      error => {
        setSpinning(false);
        setError(error.message)
      });
  }
}

export default hot(IndexList);
