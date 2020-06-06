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

const IndexView = (props) => {

  const [error, setError] = useState(null);
  const [task, setTask] = useState(null);
  const [spinning, setSpinning] = useState(false);

  useEffect(() => {
    if (!props.indexJson) {
      doGenerateIndexSample();
    }
  }, [props.selectedSchema, props.selectedIndex])

  return (
    <div className="p-1 h-100">
      <div className="h-100 d-flex flex-column border bg-light rounded">
        <div className="bg-light text-secondary p -1">INDEXING&nbsp;
          <Status task={task} error={error} spinning={spinning}/>
        </div>
        <div className="flex-grow-1 p-1">
          <div className="h-100 d-flex">
            <div className="w-100 h-100">
              <textarea className="form-control-sm h-100 w-100"
                        style={{resize: 'none'}}
                        value={props.indexJson}
                        onChange={e => props.setIndexJson(e.target.value)}/>
            </div>
          </div>
        </div>
        <form className="form-inline pr-1 pb-1">
          <div className="pl-1">
            <SchemaList id="selectSchema"
                        selectedSchema={props.selectedSchema}
                        setSelectedSchema={props.setSelectedSchema}
            />
          </div>
          <div className="pl-1">
            <IndexList id="selectIndex"
                       selectedSchema={props.selectedSchema}
                       selectedIndex={props.selectedIndex}
                       setSelectedIndex={props.setSelectedIndex}
            />
          </div>
          <div className="pt-1 pl-1">
            <button className="btn btn-outline-primary"
                    onClick={() => doGenerateIndexSample()}>
              Generate example
            </button>
          </div>
          <div className="pt-1 pl-1">
            <button className="btn btn-primary"
                    onClick={() => doIndex()}>
              INDEX
            </button>
          </div>
        </form>
      </div>
    </div>
  )

  function checkSchemaAndIndex() {
    if (props.selectedSchema == null || props.selectedSchema === '') {
      setError('Please select a schema.');
      return false;
    }
    if (props.selectedIndex == null || props.selectedIndex === '') {
      setError('Please select an index.');
      return false;
    }
    return true;
  }

  function doGenerateIndexSample() {
    if (!checkSchemaAndIndex())
      return;
    setError(null);
    setTask('Collecting sample...');
    setSpinning(true);
    fetchJson(
      '/ws/indexes/' + props.selectedSchema + '/' + props.selectedIndex + '/json/samples?count=2',
      {method: 'GET'},
      json => {
        props.setIndexJson(JSON.stringify(json, undefined, 2));
        setTask(null);
        setSpinning(false);
      },
      error => {
        setError(error);
        setTask(null);
        setSpinning(false);
      });
  }

  function parseJson() {
    const notParsed = props.indexJson;
    if (notParsed === null || notParsed === '') {
      throw 'Nothing to index';
    }
    return JSON.parse(notParsed);
  }

  function doIndex() {
    if (!checkSchemaAndIndex())
      return;
    setError(null);
    setTask('Parsing...');
    setSpinning(true);
    var parsedJson = null;
    try {
      parsedJson = parseJson();
      props.setIndexJson(JSON.stringify(parsedJson, undefined, 2))
    } catch (err) {
      setError(err.message);
      setTask(null);
      setSpinning(false);
      return;
    }

    fetchJson(
      '/ws/indexes/' + props.selectedSchema + '/' + props.selectedIndex + '/json',
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(parsedJson)
      },
      json => {
        var msg;
        switch (json) {
          case 0:
            msg = 'Nothing has been indexed.';
            break;
          case 1:
            msg = 'One record has been indexed.';
            break;
          default:
            msg = json + ' records have been indexed.';
            break;
        }
        setTask(msg);
        setSpinning(false);
      },
      error => {
        setError(error);
        setTask(null);
        setSpinning(false);
      });
  }
}

export default hot(IndexView);
