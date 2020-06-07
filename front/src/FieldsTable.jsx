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
import FieldCreateEditDelete from "./FieldCreateEditDelete";
import {fetchJson} from "./fetchJson";
import Badge from "./Badge";

const FieldsTable = (props) => {

  const [task, setTask] = useState(null);
  const [error, setError] = useState(null);
  const [spinning, setSpinning] = useState(false);
  const [fields, setFields] = useState({});
  const [editFieldName, setEditFieldName] = useState('');

  useEffect(() => {
    doFetchFields();
  }, [props.selectedSchema, props.selectedIndex])

  if (!props.selectedSchema || !props.selectedIndex)
    return null;

  return (
    <div className="border p-0 mt-1 ml-1 bg-light rounded">
      <div className="bg-light text-secondary p-1">FIELDS&nbsp;
        <Status task={task} error={error} spinning={spinning}/>
      </div>
      <FieldCreateEditDelete editFieldName={editFieldName}
                             setEditFieldName={field => setEditFieldName(field)}
                             selectedField={props.selectedField}
                             doCreateField={(field, properties) => doCreateField(field, properties)}
                             doDeleteField={field => doDeleteField(field)}
      />
      <FieldTable oss={props.oss}
                  fields={fields}
                  selectedField={props.selectedField}
                  doSelectField={value => props.setSelectedField(value)}/>
    </div>
  );

  function doFetchFields() {
    const schema = props.selectedSchema;
    const index = props.selectedIndex;
    if (!index || !schema) {
      return;
    }
    startTask();
    fetchJson(props.oss + '/ws/indexes/' + schema + '/' + index + '/fields', null,
      json => {
        endTask();
        setFields(json);
      },
      error => endTask(null, error.message));
  }

  function doCreateField(field, properties) {
    startTask('Creating field ' + field);
    fetchJson(
      props.oss + '/ws/indexes/' + props.selectedSchema + '/' + props.selectedIndex + '/fields/' + field,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(properties)
      },
      json => {
        endTask('Field created');
        setEditFieldName('');
        props.setSelectedField(field);
        doFetchFields();
      },
      error => endTask(null, error));
  }

  function doDeleteField(field) {
    startTask('Deleting field ' + field);
    fetchJson(
      props.oss + '/ws/indexes/' + props.selectedSchema + '/' + props.selectedIndex + '/fields/' + field,
      {
        method: 'DELETE'
      },
      json => {
        endTask('Field deleted');
        props.setSelectedField(null);
        doFetchFields();
      },
      error => endTask(null, error));
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

const FieldTable = (props) => {

  useEffect(() => {
  }, [props.fields])

  if (!props.fields) {
    return null;
  }

  const tableRows = Object.keys(props.fields).map((fieldName, i) => (
    <FieldRow key={i}
              fieldName={fieldName}
              fieldProperties={props.fields[fieldName]}
              selectedField={props.selectedField}
              doSelectField={name => props.doSelectField(name)}
    />
  ));
  return <table className="table table-hover table-sm table-striped table-light">
    <thead className="thead-light">
    <tr>
      <th>Name</th>
      <th>Type</th>
      <th>Analyzer</th>
      <th>Attributes</th>
    </tr>
    </thead>
    <tbody>
    {tableRows}
    </tbody>
  </table>
}

const FieldRow = (props) => {

  if (props.selectedField === props.fieldName) {
    return (
      <tr className="table-active"
          onClick={() => props.doSelectField(props.fieldName)}>
        <FieldCols fieldName={props.fieldName}
                   fieldProperties={props.fieldProperties}/>
      </tr>
    );
  } else {
    return (
      <tr onClick={() => props.doSelectField(props.fieldName)}>
        <FieldCols fieldName={props.fieldName}
                   fieldProperties={props.fieldProperties}/>
      </tr>
    );
  }
}

const FieldCols = (props) => {
  return (
    <React.Fragment>
      <td className="p-1 m-0">
        {props.fieldName}
      </td>
      <td className="p-1 m-0 text-lowercase">
        {props.fieldProperties['type']}
      </td>
      <td className="p-1 m-0 text-lowercase">
        {props.fieldProperties['analyzer']}
      </td>
      <td className="p-1 m-0">
        <Badge true="indexed" false="indexed" value={props.fieldProperties['index']}/>
        <Badge true="stored" false="stored" value={props.fieldProperties['stored']}/>
        <Badge true="sorted" false="sorted" value={props.fieldProperties['sort']}/>
        <Badge true="facet" false="facet" value={props.fieldProperties['facet']}/>
      </td>
    </React.Fragment>
  );
}

export default hot(FieldsTable);
