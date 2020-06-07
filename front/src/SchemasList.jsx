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
import React, {useEffect,useState} from 'react';

const SchemaList = (props) => {

  const [spinning, setSpinning] = useState(false);
  const [schemas, setSchemas] = useState([]);
  const [error, setError] = useState(null);

  useEffect(() => {
    doFetchSchemas();
  }, [])

  const items = schemas.map((schema, i) => (
    <option key={i} value={schema}>{schema}</option>
  ));

  return (
    <React.Fragment>
      <label className="sr-only" htmlFor={props.id}>Schema :</label>
      <select id={props.id}
              className="custom-select"
              value={props.selectedSchema}
              onChange={e => props.setSelectedSchema(e.target.value)}>
        <option value="">Select a schema</option>
        {items}''
      </select>
    </React.Fragment>
  );

  function doFetchSchemas() {
    setSpinning(true);
    fetchJson('/ws/indexes', null,
      json => {
        setSpinning(false);
        setSchemas(json);
      },
      error => {
        setSpinning(false);
        setError(error.message)
      });
  }
}

export default hot(SchemaList);
