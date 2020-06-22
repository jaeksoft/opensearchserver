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
import React, {useState} from 'react';
import Navbar from "./Navbar";
import View from "./View";

const App = () => {

  const [oss] = useState('http://localhost:9090');
  const [selectedView, setSelectedView] = useState('Indices');
  const [selectedIndex, setSelectedIndex] = useState('');
  const [selectedIndexStatus, setSelectedIndexStatus] = useState({});
  const [selectedField, setSelectedField] = useState('');
  const [indexJson, setIndexJson] = useState('');
  const [queryJson, setQueryJson] = useState(
    JSON.stringify(
      JSON.parse(
        '{"query":{"type": "MatchAllDocsQuery"},"returned_fields":["*"]}'), undefined, 2
    )
  );

  return (
    <div className="vh-100 m-0 p-0 d-flex flex-column">
      <Navbar oss={oss}
              selectedIndex={selectedIndex}
              selectedView={selectedView}
              setSelectedView={setSelectedView}/>
      <div className="container-fluid flex-fill d-flex flex-column pl-0 pr-1">
        <View oss={oss}
              selectedView={selectedView}
              selectedIndex={selectedIndex}
              setSelectedIndex={doSetSelectedIndex}
              selectedIndexStatus={selectedIndexStatus}
              setSelectedIndexStatus={setSelectedIndexStatus}
              selectedField={selectedField}
              setSelectedField={setSelectedField}
              indexJson={indexJson}
              setIndexJson={setIndexJson}
              queryJson={queryJson}
              setQueryJson={setQueryJson}
        />
      </div>
    </div>
  );

  function doSetSelectedIndex(index) {
    setSelectedIndex(index);
    setSelectedField('');
  }
}

export default hot(App);
