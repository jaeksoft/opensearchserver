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

const QueryView = props => {
  const [error, setError] = useState(null);
  const [task, setTask] = useState(null);
  const [spinning, setSpinning] = useState(false);
  const [resultJson, setResultJson] = useState('');
  return /*#__PURE__*/React.createElement("div", {
    className: "p-1 h-100"
  }, /*#__PURE__*/React.createElement("div", {
    className: "h-100 d-flex flex-column border bg-light rounded"
  }, /*#__PURE__*/React.createElement("div", {
    className: "bg-light text-secondary p-1"
  }, "QUERYING\xA0", /*#__PURE__*/React.createElement(Status, {
    task: task,
    error: error,
    spinning: spinning
  })), /*#__PURE__*/React.createElement("div", {
    className: "flex-grow-1 p-1"
  }, /*#__PURE__*/React.createElement("div", {
    className: "h-100 d-flex"
  }, /*#__PURE__*/React.createElement("textarea", {
    className: "form-control-sm w-50 h-100",
    style: {
      resize: 'none'
    },
    value: props.queryJson,
    onChange: e => props.setQueryJson(e.target.value)
  }), /*#__PURE__*/React.createElement("textarea", {
    className: "form-control-sm w-50 h-100",
    readOnly: true,
    style: {
      resize: 'none'
    },
    value: resultJson,
    onChange: e => props.setIndexJson(e.target.value)
  }))), /*#__PURE__*/React.createElement("form", {
    className: "form-inline pr-1 pb-1"
  }, /*#__PURE__*/React.createElement("div", {
    className: "pl-1"
  }, /*#__PURE__*/React.createElement(SchemaList, {
    id: "selectSchema",
    selectedSchema: props.selectedSchema,
    setSelectedSchema: props.setSelectedSchema
  })), /*#__PURE__*/React.createElement("div", {
    className: "pl-1"
  }, /*#__PURE__*/React.createElement(IndexList, {
    id: "selectIndex",
    selectedSchema: props.selectedSchema,
    selectedIndex: props.selectedIndex,
    setSelectedIndex: props.setSelectedIndex
  })), /*#__PURE__*/React.createElement("div", {
    className: "pt-1 pl-1"
  }, /*#__PURE__*/React.createElement("button", {
    className: "btn btn-primary",
    onClick: () => doQuery()
  }, "Post JSON")))));

  function parseJson() {
    const notParsed = props.queryJson;

    if (notParsed === null || notParsed === '') {
      throw 'Nothing to index';
    }

    return JSON.parse(notParsed);
  }

  function doQuery() {
    if (props.selectedSchema == null || props.selectedSchema === '') {
      setError('Please select a schema.');
      return;
    }

    if (props.selectedIndex == null || props.selectedIndex === '') {
      setError('Please select an index.');
      return;
    }

    setError(null);
    setTask('Parsing...');
    setSpinning(true);
    var parsedJson = null;

    try {
      parsedJson = parseJson();
      props.setQueryJson(JSON.stringify(parsedJson, undefined, 2));
    } catch (err) {
      setError(err.message);
      setTask(null);
      setSpinning(false);
      return;
    }

    setTask('Querying...');
    fetchJson('/ws/indexes/' + props.selectedSchema + '/' + props.selectedIndex + '/search', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(parsedJson)
    }, json => {
      setResultJson(JSON.stringify(json, undefined, 2));
      setTask("Query successful.");
      setSpinning(false);
    }, error => {
      setResultJson('');
      setError(error);
      setTask(null);
      setSpinning(false);
    });
  }
};