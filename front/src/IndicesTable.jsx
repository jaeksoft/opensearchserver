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
import IndexCreateEditDelete from "./IndexCreateEditDelete";
import IndexList from "./IndexList";
import {fetchJson} from "./fetchUtils.js"

function IndicesTable(props) {

  const [task, setTask] = useState(null);
  const [error, setError] = useState(null);
  const [spinning, setSpinning] = useState(false);
  const [indices, setIndices] = useState([]);
  const [indexName, setIndexName] = useState('');
  const [primaryKey, setPrimaryKey] = useState('id');

  useEffect(() => {
    doFetchIndices();
  }, [])

  return (
    <div className="border bg-light rounded">
      <div className="bg-light text-secondary p-1">INDICES&nbsp;
        <Status task={task} error={error} spinning={spinning}/>
      </div>
      <IndexCreateEditDelete
        indexName={indexName}
        setIndexName={idx => setIndexName(idx)}
        selectedIndex={props.selectedIndex}
        setPrimaryKey={key => setPrimaryKey(key)}
        primaryKey={primaryKey}
        doCreate={idx => doCreateIndex(idx)}
        doDelete={idx => doDeleteIndex(idx)}
      />
      <IndexList indices={indices}
                 selectedIndex={props.selectedIndex}
                 setSelectedIndex={selectIndex}
                 selectedIndexStatus={props.selectedIndexStatus}
                 setSelectedIndexStatus={props.setSelectedIndexStatus}
      />
    </div>
  );

  function doCreateIndex(idx) {
    startTask('Creating index ' + idx);
    fetchJson(
      props.oss + '/ws/indexes/' + indexName,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(
          {
            "primary_key": primaryKey,
            "record_field": "$record$"
          })
      },
      json => {
        endTask('Index created');
        setIndexName('');
        props.setSelectedIndex(idx);
        doFetchIndices();
      },
      error => endTask(null, error)
    );
  }

  function doDeleteIndex(idx) {
    startTask('Deleting index ' + idx);
    fetchJson(props.oss + '/ws/indexes/' + idx, {method: 'DELETE'},
      json => {
        props.setSelectedIndex(null);
        endTask('Index deleted');
        doFetchIndices();
      },
      error => endTask(null, error));
  }

  function doFetchIndices() {
    startTask();
    fetchJson(props.oss + '/ws/indexes', null,
      json => {
        endTask();
        setIndices(json);
      },
      error => endTask(null, error));
  }

  function doFetchIndex(index) {
    if (!index)
      return null;
    startTask();
    fetchJson(props.oss + '/ws/indexes/' + index, null,
      json => {
        props.setSelectedIndexStatus(json);
        endTask();
      },
      error => endTask(null, error));
  }

  function selectIndex(index) {
    props.setSelectedIndexStatus({});
    doFetchIndex(index);
    props.setSelectedIndex(index);
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

export default hot(IndicesTable);
