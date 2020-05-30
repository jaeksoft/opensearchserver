/*
 * Copyright 2017-2018 Emmanuel Keller / Jaeksoft
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

function SchemasTable(props) {
  const [task, setTask] = useState(null);
  const [error, setError] = useState(null);
  const [spinning, setSpinning] = useState(false);
  const [schemas, setSchemas] = useState([]);
  const [schemaName, setSchemaName] = useState('');
  useEffect(() => {
    doFetchSchemas();
  }, []);
  return /*#__PURE__*/React.createElement("div", {
    className: "border p-0 mt-1 ml-1 bg-light rounded"
  }, /*#__PURE__*/React.createElement("div", {
    className: "bg-light text-secondary p-1"
  }, "SCHEMAS\xA0", /*#__PURE__*/React.createElement(Status, {
    task: task,
    error: error,
    spinning: spinning
  })), /*#__PURE__*/React.createElement(CreateEditDelete, {
    name: schemaName,
    setName: sch => setSchemaName(sch),
    selectedName: props.selectedSchema,
    doCreate: sch => doCreateSchema(sch),
    doDelete: sch => doDeleteSchema(sch)
  }), /*#__PURE__*/React.createElement(List, {
    values: schemas,
    selectedValue: props.selectedSchema,
    doSelectValue: value => props.setSelectedSchema(value)
  }));

  function doCreateSchema(sch) {
    startTask('Creating schema ' + sch);
    fetchJson('/ws/indexes/' + sch, {
      method: 'POST'
    }, json => {
      endTask('Schema created');
      setSchemaName('');
      props.setSelectedSchema(sch);
      doFetchSchemas();
    }, error => endTask(null, error.message));
  }

  function doDeleteSchema(sch) {
    startTask('Deleting schema ' + sch);
    fetchJson('/ws/indexes/' + sch, {
      method: 'DELETE'
    }, json => {
      props.setSelectedSchema(null);
      endTask('Schema deleted');
      doFetchSchemas();
    }, error => endTask(null, error.message));
  }

  function doFetchSchemas() {
    startTask();
    fetchJson('/ws/indexes', null, json => {
      endTask();
      setSchemas(json);
    }, error => endTask(null, error.message));
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
    if (newTask) setTask(newTask);
    if (newError) setError(newError);else if (newTask) setError(null);
  }
}

const SchemaList = props => {
  const [spinning, setSpinning] = useState(false);
  const [schemas, setSchemas] = useState([]);
  const [error, setError] = useState(null);
  useEffect(() => {
    doFetchSchemas();
  }, []);
  const items = schemas.map((schema, i) => /*#__PURE__*/React.createElement("option", {
    key: i,
    value: schema
  }, schema));
  return /*#__PURE__*/React.createElement(React.Fragment, null, /*#__PURE__*/React.createElement("label", {
    className: "sr-only",
    htmlFor: props.id
  }, "Schema :"), /*#__PURE__*/React.createElement("select", {
    id: props.id,
    className: "custom-select",
    value: props.selectedSchema,
    onChange: e => props.setSelectedSchema(e.target.value)
  }, /*#__PURE__*/React.createElement("option", {
    value: ""
  }, "Select a schema"), items, "''"));

  function doFetchSchemas() {
    setSpinning(true);
    fetchJson('/ws/indexes', null, json => {
      setSpinning(false);
      setSchemas(json);
    }, error => {
      setSpinning(false);
      setError(error.message);
    });
  }
};