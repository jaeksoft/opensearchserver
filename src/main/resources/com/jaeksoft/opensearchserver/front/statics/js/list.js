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

/**
 *
 * @param props values, selectedValue, doSelectValue
 * @returns {*}
 */
const List = props => {
  if (!props.values) return null;
  const values = Array.isArray(props.values) ? props.values : Object.keys(props.values);
  const listItems = values.map((value, i) => /*#__PURE__*/React.createElement(ListItem, {
    key: i,
    s: true,
    value: value,
    selectedValue: props.selectedValue,
    doSelectValue: value => props.doSelectValue(value)
  }));
  return /*#__PURE__*/React.createElement("table", {
    className: "table table-hover table-sm table-striped"
  }, /*#__PURE__*/React.createElement("tbody", null, listItems));
};
/**
 *
 * @param props value, selectedValue, doSelectValue
 * @returns {*}
 */


const ListItem = props => {
  if (props.selectedValue === props.value) {
    return /*#__PURE__*/React.createElement("tr", {
      className: "table-active",
      onClick: () => props.doSelectValue(props.value)
    }, /*#__PURE__*/React.createElement("td", {
      className: "p-1 m-1"
    }, props.value));
  } else {
    return /*#__PURE__*/React.createElement("tr", {
      onClick: () => props.doSelectValue(props.value)
    }, /*#__PURE__*/React.createElement("td", {
      className: "p-1 m-1"
    }, props.value));
  }
};