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

function Indices(props) {
  const [status, setStatus] = useState(newStatus());
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
  }, "INDICES", /*#__PURE__*/React.createElement(Status, {
    status: status
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
      setStatus(endTask(status, null, 'Please select a schema'));
      return;
    }

    setStatus(startTask(status, 'Creating index ' + idx));
    fetchJson('/ws/indexes/' + props.selectedSchema + '/' + indexName, {
      method: 'POST'
    }, json => {
      setStatus(endTask(status, 'Index created'));
      doFetchIndices();
    }, error => setStatus(endTask(status, null, error)));
  }

  function doDeleteIndex(idx) {
    if (!props.selectedSchema) {
      return setStatus(endTask(status, null, 'No schema is selecteds'));
    }

    setStatus(startTask(status, 'Deleting index ' + idx));
    fetchJson('/ws/indexes/' + props.selectedSchema + '/' + idx, {
      method: 'DELETE'
    }, json => {
      setStatus(endTask(status, 'index deleted'));
      doFetchIndices();
    }, error => setStatus(endTask(status, null, error)));
  }

  function doFetchIndices() {
    const schema = props.selectedSchema;

    if (!schema) {
      return;
    }

    setStatus(startTask(status));
    fetchJson('/ws/indexes/' + schema, null, json => {
      setStatus(endTask(status));
      setIndices(json);
    }, error => setStatus(endTask(status, null, error)));
  }
}