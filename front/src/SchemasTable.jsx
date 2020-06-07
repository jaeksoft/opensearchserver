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
import React, {useEffect, useState} from 'react';
import CreateEditDelete from "./CreateEditDelete";
import Status from "./Status";
import List from "./List";
import {fetchJson} from "./fetchJson.js"

const SchemasTable = (props) => {

  const [task, setTask] = useState(null);
  const [error, setError] = useState(null);
  const [spinning, setSpinning] = useState(false);
  const [schemas, setSchemas] = useState([]);
  const [schemaName, setSchemaName] = useState('');

  useEffect(() => {
    doFetchSchemas();
  }, [])

  return (
    <div className="border p-0 mt-1 ml-1 bg-light rounded">
      <div className="bg-light text-secondary p-1">SCHEMAS&nbsp;
        <Status task={task} error={error} spinning={spinning}/>
      </div>
      <CreateEditDelete
        name={schemaName}
        setName={sch => setSchemaName(sch)}
        selectedName={props.selectedSchema}
        doCreate={sch => doCreateSchema(sch)}
        doDelete={sch => doDeleteSchema(sch)}
      />
      <List values={schemas}
            selectedValue={props.selectedSchema}
            doSelectValue={value => props.setSelectedSchema(value)}
            doGetKey={value => value}/>
    </div>
  );

  function doCreateSchema(sch) {
    startTask('Creating schema ' + sch);
    fetchJson(props.oss + '/ws/indexes/' + sch, {method: 'POST'},
      json => {
        endTask('Schema created');
        setSchemaName('');
        props.setSelectedSchema(sch);
        doFetchSchemas();
      },
      error => endTask(null, error.message));
  }

  function doDeleteSchema(sch) {
    startTask('Deleting schema ' + sch);
    fetchJson(props.oss + '/ws/indexes/' + sch, {method: 'DELETE'},
      json => {
        props.setSelectedSchema(null);
        endTask('Schema deleted');
        doFetchSchemas();
      },
      error => endTask(null, error.message));
  }

  function doFetchSchemas() {
    startTask();
    fetchJson(props.oss + '/ws/indexes', null,
      json => {
        endTask();
        setSchemas(json);
      },
      error => endTask(null, error.message));
  }

  function startTask(newTask) {
    setSpinning(true);
    if (newTask) {
      setTask(newTask);
      setError(null);
    }
  }

  function endTask(newTask, newError) {
    setSpinning(false);
    if (newTask)
      setTask(newTask);
    if (newError)
      setError(newError);
    else if (newTask)
      setError(null);
  }

}

export default hot(SchemasTable);
