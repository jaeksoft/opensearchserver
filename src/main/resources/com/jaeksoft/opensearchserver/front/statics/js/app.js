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

function App() {
  const [selectedSchema, setSelectedSchema] = useState(null);
  return /*#__PURE__*/React.createElement(React.Fragment, null, /*#__PURE__*/React.createElement(Navbar, null), /*#__PURE__*/React.createElement("div", {
    className: "container-fluid p-0 m-0"
  }, /*#__PURE__*/React.createElement("div", {
    className: "d-flex"
  }, /*#__PURE__*/React.createElement("div", {
    className: "shadow p-0 mt-2 ml-2 mr-1 bg-white rounded flex-fill"
  }, /*#__PURE__*/React.createElement(Schemas, {
    selectedSchema: selectedSchema,
    setSelectedSchema: setSelectedSchema
  })), /*#__PURE__*/React.createElement("div", {
    className: "shadow p-0 mt-2 ml-2 mr-1 bg-white rounded flex-fill"
  }, /*#__PURE__*/React.createElement(Indices, {
    selectedSchema: selectedSchema
  })))));
}

ReactDOM.render( /*#__PURE__*/React.createElement(App, null), document.getElementById('app'));