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

function FieldsTable(props) {
  const [task, setTask] = useState(null);
  const [error, setError] = useState(null);
  const [spinning, setSpinning] = useState(false);
  const [fields, setFields] = useState({});
  const [editFieldName, setEditFieldName] = useState('');
  useEffect(() => {
    doFetchFields();
  }, [props.selectedSchema, props.selectedIndex]);
  if (!props.selectedSchema || !props.selectedIndex) return null;
  return /*#__PURE__*/React.createElement("div", {
    className: "border p-0 mt-1 ml-1 bg-light rounded"
  }, /*#__PURE__*/React.createElement("div", {
    className: "bg-light text-secondary p-1"
  }, "FIELDS\xA0", /*#__PURE__*/React.createElement(Status, {
    task: task,
    error: error,
    spinning: spinning
  })), /*#__PURE__*/React.createElement(FieldCreateEditDelete, {
    editFieldName: editFieldName,
    setEditFieldName: field => setEditFieldName(field),
    selectedField: props.selectedField,
    doCreateField: (field, properties) => doCreateField(field, properties),
    doDeleteField: field => doDeleteField(field)
  }), /*#__PURE__*/React.createElement(FieldTable, {
    fields: fields,
    selectedField: props.selectedField,
    doSelectField: value => props.setSelectedField(value)
  }));

  function doFetchFields() {
    const schema = props.selectedSchema;
    const index = props.selectedIndex;

    if (!index || !schema) {
      return;
    }

    startTask();
    fetchJson('/ws/indexes/' + schema + '/' + index + '/fields', null, json => {
      endTask();
      setFields(json);
    }, error => endTask(null, error.message));
  }

  function doCreateField(field, properties) {
    startTask('Creating field ' + field);
    fetchJson('/ws/indexes/' + props.selectedSchema + '/' + props.selectedIndex + '/fields/' + field, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(properties)
    }, json => {
      endTask('Field created');
      setEditFieldName('');
      props.setSelectedField(field);
      doFetchFields();
    }, error => endTask(null, error));
  }

  function doDeleteField(field) {
    startTask('Deleting field ' + field);
    fetchJson('/ws/indexes/' + props.selectedSchema + '/' + props.selectedIndex + '/fields/' + field, {
      method: 'DELETE'
    }, json => {
      endTask('Field deleted');
      props.setSelectedField(null);
      doFetchFields();
    }, error => endTask(null, error));
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

const FieldTable = props => {
  const tableRows = Object.keys(props.fields).map((fieldName, i) => /*#__PURE__*/React.createElement(FieldRow, {
    key: i,
    fieldName: fieldName,
    fieldProperties: props.fields[fieldName],
    selectedField: props.selectedField,
    doSelectField: name => props.doSelectField(name)
  }));
  return /*#__PURE__*/React.createElement("table", {
    className: "table table-hover table-sm table-striped table-light"
  }, /*#__PURE__*/React.createElement("thead", {
    className: "thead-light"
  }, /*#__PURE__*/React.createElement("tr", null, /*#__PURE__*/React.createElement("th", null, "Name"), /*#__PURE__*/React.createElement("th", null, "Type"), /*#__PURE__*/React.createElement("th", null, "Analyzer"), /*#__PURE__*/React.createElement("th", null, "Attributes"))), /*#__PURE__*/React.createElement("tbody", null, tableRows));
};

const FieldRow = props => {
  if (props.selectedField === props.fieldName) {
    return /*#__PURE__*/React.createElement("tr", {
      className: "table-active",
      onClick: () => props.doSelectField(props.fieldName)
    }, /*#__PURE__*/React.createElement(FieldCols, {
      fieldName: props.fieldName,
      fieldProperties: props.fieldProperties
    }));
  } else {
    return /*#__PURE__*/React.createElement("tr", {
      onClick: () => props.doSelectField(props.fieldName)
    }, /*#__PURE__*/React.createElement(FieldCols, {
      fieldName: props.fieldName,
      fieldProperties: props.fieldProperties
    }));
  }
};

const FieldCols = props => {
  return /*#__PURE__*/React.createElement(React.Fragment, null, /*#__PURE__*/React.createElement("td", {
    className: "p-1 m-0"
  }, props.fieldName), /*#__PURE__*/React.createElement("td", {
    className: "p-1 m-0 text-lowercase"
  }, props.fieldProperties['type']), /*#__PURE__*/React.createElement("td", {
    className: "p-1 m-0 text-lowercase"
  }, props.fieldProperties['analyzer']), /*#__PURE__*/React.createElement("td", {
    className: "p-1 m-0"
  }, /*#__PURE__*/React.createElement(Badge, {
    true: "indexed",
    false: "indexed",
    value: props.fieldProperties['index']
  }), /*#__PURE__*/React.createElement(Badge, {
    true: "stored",
    false: "stored",
    value: props.fieldProperties['stored']
  }), /*#__PURE__*/React.createElement(Badge, {
    true: "sorted",
    false: "sorted",
    value: props.fieldProperties['sort']
  }), /*#__PURE__*/React.createElement(Badge, {
    true: "facet",
    false: "facet",
    value: props.fieldProperties['facet']
  })));
};