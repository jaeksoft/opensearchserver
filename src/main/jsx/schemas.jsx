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

'use strict';

const {useState, useEffect} = React;

const Schemas = () => {

  const [lastError, setLastError] = useState(null);
  const [status, setStatus] = useState(null);
  const [spinning, setSpinning] = useState(false);
  const [schemas, setSchemas] = useState([]);
  const [schemaName, setSchemaName] = useState('');

  useEffect(() => {
    doFetchSchemas();
  }, [])

  return (
    <div>
      <Status error={lastError} status={status} spinning={spinning}/>
      <div className="card-group">
        <div className="card">
          <div className="card-body">
            <div className="row">
              <div className="col-md-5">
                <div className="card-group">
                  <div className="card">
                    <div className="card-body">
                      <h5 className="card-title">Schemas</h5>
                    </div>
                    <ul className="list-group">
                      {schemas.map(schema => (
                        <li key={schema} className="list-group-item">
                          {schema}
                        </li>
                      ))}
                    </ul>
                    <div className="card-body">
                      <div className="input-group">
                        <div className=" input-group-prepend">
                          <button className=" btn btn-primary" type=" button" id=" create-schema"
                                  onClick={doCreateSchema}>
                            Create
                          </button>
                        </div>
                        <input type=" text" className=" form-control" placeholder="schema name"
                               aria-label=" New schema name" aria-describedby=" create-schema"
                               value={schemaName} onChange={e => setSchemaName(e.target.value)}
                        />
                        <div className=" input-group-append">
                          <button className=" btn btn-danger" type=" button" id="delete-schema"
                                  onClick={doDeleteSchema}>
                            Delete
                          </button>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              <div className="col-md-5">&nbsp;</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );

  function doCreateSchema() {
    setStatus('Creating schema ' + schemaName);
    fetch('/ws/indexes/' + schemaName, {method: 'POST'})
      .then(response => checkErrorJson(response, 'Schema created'))
      .then(json => doFetchSchemas(), error => endTask(null, error));
  }

  function doDeleteSchema() {
    setStatus('Deleting schema ' + schemaName);
    fetch('/ws/indexes/' + schemaName, {method: 'DELETE'})
      .then(response => checkErrorJson(response, 'Schema deleted'))
      .then(json => doFetchSchemas(), error => endTask(null, error));
  }

  function doFetchSchemas() {
    startTask(null);
    fetch('/ws/indexes')
      .then(response => checkErrorJson(response, null))
      .then(
        (json) => {
          endTask(null, null);
          setSchemas(json);
        },
        (error) => endTask(null, error)
      )
  }

  function checkErrorJson(response, newSuccessfulStatus) {
    const jsonPromise = response.json();
    if (response.ok) {
      if (newSuccessfulStatus) {
        setStatus(newSuccessfulStatus);
        setLastError(null);
      }
      return jsonPromise;
    } else {
      return jsonPromise.then(errorJson => {
        const errorMessage = (errorJson && errorJson.message) || response.statusText;
        setLastError(errorMessage);
        return null;
      });
    }
  }

  function endTask(newStatus, newError) {
    setSpinning(false);
    if (newStatus)
      setStatus(newStatus);
    if (newError)
      setLastError(newError);
    else if (newStatus)
      setLastError(null);
  }

  function startTask(newStatus) {
    setSpinning(true);
    if (newStatus)
      setStatus(newStatus);
  }
}
