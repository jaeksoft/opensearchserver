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
import React from 'react';

/**
 *
 * @param props values, selectedValue, doSelectValue
 * @returns {*}
 */
const IndexList = (props) => {

  if (!props.indices)
    return null;

  const values = Array.isArray(props.indices) ? props.indices : Object.keys(props.indices);
  const listItems = values.map((index, i) => (
    <IndexListItem key={i}
                   index={index}
                   selectedIndex={props.selectedIndex}
                   setSelectedIndex={props.setSelectedIndex}
                   selectedIndexStatus={props.selectedIndexStatus}
                   setSelectedIndexStatus={props.setSelectedIndexStatus}
    />
  ));
  return (
    <table className="table table-hover table-sm table-striped table-light">
      <thead className="thead-light">
      <tr>
        <th>Index name</th>
        <th title="Select an index to see the primary key">Primary key</th>
        <th title="Select an index to see the number of documents">Num docs</th>
      </tr>
      </thead>
      <tbody>
      {listItems}
      </tbody>
    </table>

  );
}

/**
 *
 * @param props value, selectedValue, doSelectValue
 * @returns {*}
 */
const IndexListItem = (props) => {
  if (props.selectedIndex === props.index) {
    return (
      <tr className="table-active" onClick={() => props.setSelectedIndex(props.index)}>
        <td className="p-1 m-1">{props.index}</td>
        <td className="p-1 m-1">{getPrimaryKey(props.selectedIndexStatus)}</td>
        <td className="p-1 m-1">{getNumDocs(props.selectedIndexStatus)}</td>
      </tr>
    );
  } else {
    return (
      <tr onClick={() => props.setSelectedIndex(props.index)}>
        <td className="p-1 m-1">{props.index}</td>
        <td colSpan="2" title="Select the line to see the primary key and the number of docs" className="p-1 m-1"/>
      </tr>
    );
  }

  function getPrimaryKey(indexStatus) {
    if (indexStatus == null)
      return null;
    if (indexStatus.settings == null)
      return null;
    return indexStatus.settings.primary_key;
  }

  function getNumDocs(indexStatus) {
    if (indexStatus == null)
      return null;
    return indexStatus.num_docs;
  }
}


export default hot(IndexList);
