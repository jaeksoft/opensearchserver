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

function IndicesTable(props) {
  const [task, setTask] = useState(null);
  const [error, setError] = useState(null);
  const [spinning, setSpinning] = useState(false);
  const [indices, setIndices] = useState([]);
  const [indexName, setIndexName] = useState('');
  useEffect(() => {
    doFetchIndices();
  }, [props.selectedSchema]);
  if (!props.selectedSchema) return null;
  return /*#__PURE__*/React.createElement("div", {
    className: "border p-0 mt-1 ml-1 bg-light rounded"
  }, /*#__PURE__*/React.createElement("div", {
    className: "bg-light text-secondary p-1"
  }, "INDICES\xA0", /*#__PURE__*/React.createElement(Status, {
    task: task,
    error: error,
    spinning: spinning
  })), /*#__PURE__*/React.createElement(CreateEditDelete, {
    name: indexName,
    setName: idx => setIndexName(idx),
    selectedName: props.selectedIndex,
    doCreate: idx => doCreateIndex(idx),
    doDelete: idx => doDeleteIndex(idx)
  }), /*#__PURE__*/React.createElement(List, {
    values: indices,
    selectedValue: props.selectedIndex,
    doSelectValue: value => props.setSelectedIndex(value)
  }));

  function doCreateIndex(idx) {
    if (!props.selectedSchema) {
      endTask(null, 'Please select a schema');
      return;
    }

    startTask('Creating index ' + idx);
    fetchJson('/ws/indexes/' + props.selectedSchema + '/' + indexName, {
      method: 'POST'
    }, json => {
      endTask('Index created');
      setIndexName('');
      props.setSelectedIndex(idx);
      doFetchIndices();
    }, error => endTask(null, error.message));
  }

  function doDeleteIndex(idx) {
    if (!props.selectedSchema) {
      return endTask(null, 'No schema is selected');
    }

    startTask('Deleting index ' + idx);
    fetchJson('/ws/indexes/' + props.selectedSchema + '/' + idx, {
      method: 'DELETE'
    }, json => {
      props.setSelectedIndex(null);
      endTask('Index deleted');
      doFetchIndices();
    }, error => endTask(null, error));
  }

  function doFetchIndices() {
    const schema = props.selectedSchema;

    if (!schema) {
      return;
    }

    startTask();
    fetchJson('/ws/indexes/' + schema, null, json => {
      endTask();
      setIndices(json);
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

const IndexList = props => {
  const [spinning, setSpinning] = useState(false);
  const [indices, setIndices] = useState([]);
  const [error, setError] = useState(null);
  useEffect(() => {
    doFetchIndices();
  }, [props.selectedSchema]);
  const items = Object.keys(indices).map((index, i) => /*#__PURE__*/React.createElement("option", {
    key: i,
    value: index
  }, index));
  return /*#__PURE__*/React.createElement(React.Fragment, null, /*#__PURE__*/React.createElement("label", {
    className: "sr-only",
    htmlFor: props.id
  }, "Index :"), /*#__PURE__*/React.createElement("select", {
    id: props.id,
    className: "custom-select",
    value: props.selectedIndex,
    onChange: e => props.setSelectedIndex(e.target.value)
  }, /*#__PURE__*/React.createElement("option", {
    value: ""
  }, "Select an index"), items));

  function doFetchIndices() {
    const schema = props.selectedSchema;

    if (!schema) {
      return;
    }

    setSpinning(true);
    fetchJson('/ws/indexes/' + schema, null, json => {
      setSpinning(false);
      setIndices(json);
    }, error => {
      setSpinning(false);
      setError(error.message);
    });
  }
};