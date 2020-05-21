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

function Fields(props) {
  const [status, setStatus] = useState(newStatus());
  const [fields, setFields] = useState([]);
  const [editFieldName, setEditFieldName] = useState('');
  useEffect(() => {
    doFetchFields();
  }, [props.selectedSchema, props.selectedIndex]);
  if (!props.selectedSchema || !props.selectedIndex) return null;
  return /*#__PURE__*/React.createElement("div", {
    className: "border p-0 mt-1 ml-1 bg-light rounded"
  }, /*#__PURE__*/React.createElement("div", {
    className: "bg-light text-secondary p-1"
  }, "FIELDS", /*#__PURE__*/React.createElement(Status, {
    status: status
  })), /*#__PURE__*/React.createElement(FieldCreateEditDelete, {
    editFieldName: editFieldName,
    setEditFieldName: field => setEditFieldName(field),
    selectedName: props.selectedField,
    doCreateField: idx => doCreateField(idx),
    doDeleteField: idx => doDeleteField(idx)
  }), /*#__PURE__*/React.createElement(List, {
    values: fields,
    selectedValue: props.setSelectedField,
    doSelectValue: value => props.setSelectedField(value)
  }));

  function doFetchFields() {
    const schema = props.selectedSchema;
    const index = props.selectedIndex;

    if (!index || !schema) {
      return;
    }

    setStatus(startTask(status));
    fetchJson('/ws/indexes/' + schema + '/' + index + '/fields', null, json => {
      setStatus(endTask(status));
      setFields(json);
    }, error => setStatus(endTask(status, null, error)));
  }

  function doCreateField(fieldName) {}

  function doDeleteField(fieldName) {}
}

const fieldAttributes = ["index", "stored", "facet", "sort"];

function FieldCreateEditDelete(props) {
  const fieldAttributesCheckboxes = fieldAttributes.map(attribute => /*#__PURE__*/React.createElement("div", {
    className: "form-check form-check-inline"
  }, /*#__PURE__*/React.createElement("input", {
    className: "form-check-input",
    type: "checkbox",
    id: "fieldAttr{attribute}",
    value: "{attribute}"
  }), /*#__PURE__*/React.createElement("label", {
    className: "form-check-label",
    htmlFor: "fieldAttr{attribute}"
  }, attribute)));
  return /*#__PURE__*/React.createElement(React.Fragment, null, /*#__PURE__*/React.createElement("div", {
    className: "input-group p-1"
  }, /*#__PURE__*/React.createElement("input", {
    type: "text",
    className: "form-control shadow-none",
    "aria-label": "edit field name",
    "aria-describedby": "edit field name",
    value: props.editFieldName,
    onChange: e => props.setEditFieldName(e.target.value)
  }), /*#__PURE__*/React.createElement("div", {
    className: "input-group-append"
  }, /*#__PURE__*/React.createElement("select", {
    className: "custom-select shadow-none"
  }, /*#__PURE__*/React.createElement("option", {
    value: "text"
  }, "Text"), /*#__PURE__*/React.createElement("option", {
    value: "integer"
  }, "Integer"), /*#__PURE__*/React.createElement("option", {
    value: "long"
  }, "Long"), /*#__PURE__*/React.createElement("option", {
    value: "double"
  }, "Double"), /*#__PURE__*/React.createElement("option", {
    value: "float"
  }, "Float")), /*#__PURE__*/React.createElement(CreateOrDeleteButton, {
    name: props.editFieldName,
    selectedName: props.selectedField,
    doDelete: name => props.doDeleteField(name),
    doCreate: name => props.doCreateField(name)
  }))), /*#__PURE__*/React.createElement("div", {
    className: "p-1"
  }, fieldAttributesCheckboxes));
}