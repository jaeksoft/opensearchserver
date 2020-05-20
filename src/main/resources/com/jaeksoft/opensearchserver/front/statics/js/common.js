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

const {
  useState,
  useEffect
} = React;
/**
 *
 * @param request
 * @param init
 * @param doJson
 * @param doError
 * @returns {Promise<String>}
 */

function fetchJson(request, init, doJson, doError) {
  return fetch(request, init).then(response => {
    const jsonPromise = response.json();

    if (response.ok) {
      return jsonPromise;
    } else {
      return jsonPromise.then(errorJson => {
        const errorMessage = errorJson && errorJson.message || response.statusText;
        doError(errorMessage);
      });
    }
  }).then(json => {
    doJson(json);
  }, error => {
    doError(error);
  });
}
/**
 *
 * @param props name, selectedName, doDelete, doCreate
 * @returns {*}
 */


function CreateOrDeleteButton(props) {
  if (props.name === props.selectedName) {
    return /*#__PURE__*/React.createElement("button", {
      className: "btn btn-danger btn-sm shadow-none",
      type: "button",
      onClick: () => props.doDelete(props.name)
    }, "Delete");
  } else {
    return /*#__PURE__*/React.createElement("button", {
      className: "btn btn-primary btn-sm shadow-none",
      type: "button",
      onClick: () => props.doCreate(props.name)
    }, "Create");
  }
}
/**
 *
 * @param props doCreate, doDelete, name, setName, selectedName
 * @returns {*}
 * @constructor
 */


function CreateDeleteButtons(props) {
  return /*#__PURE__*/React.createElement("div", {
    className: "input-group input-group-sm p-1"
  }, /*#__PURE__*/React.createElement("input", {
    type: "text",
    className: "form-control shadow-none",
    "aria-label": "name",
    "aria-describedby": "create",
    value: props.name,
    onChange: e => props.setName(e.target.value)
  }), /*#__PURE__*/React.createElement(CreateOrDeleteButton, {
    name: props.name,
    selectedName: props.selectedName,
    doDelete: name => props.doDelete(name),
    doCreate: name => props.doCreate(name)
  }));
}