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
import React, {useState, useEffect} from 'react';
import {fetchJson} from "./fetchJson.js"

const QueryHelper = (props) => {

  const [queryTypes, setQueryTypes] = useState([]);

  useEffect(() => {
    doFetchQueryTypes();
  }, [props.selectedIndex, props.queryJson])

  if (!props.selectedIndex || !props.queryJson)
    return null;

  const listItems = Object.keys(queryTypes).map((queryType, i) => (
      <tr>
        <td className="list-group-item" key={i}>{queryType}</td>
      </tr>
    ))
  ;

  return (
    <table className="table table-sm">
      {listItems}
    </table>
  );

  function doFetchQueryTypes() {
    if (!props.selectedIndex) {
      return;
    }
    var lookup = '';
    if (props.queryJson) {
      try {
        lookup = Object.keys(JSON.parse(props.queryJson).query)[0] || '';
      } catch (err) {
        lookup = '';
      }
    }

    fetchJson(props.oss + '/ws/indexes/' + props.selectedIndex
      + '/search/queries/types?lookup=' + lookup,
      null,
      json => {
        setQueryTypes(json);
      });
  }

}

export default hot(QueryHelper);

