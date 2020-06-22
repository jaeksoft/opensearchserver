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

import {hot} from 'react-hot-loader/root';
import React, {useEffect} from 'react';

import AceEditor from "react-ace";
import "ace-builds/webpack-resolver";

const JsonEditor = (props) => {

  if (props.setValue == null)
    return (
      <AceEditor
        mode="json"
        theme="github"
        editorProps={{$blockScrolling: false}}
        value={props.value}
        readOnly={true}
        height="100%"
        width="100%"
      />
    );
  else
    return (
      <AceEditor
        mode="json"
        theme="github"
        editorProps={{$blockScrolling: false}}
        value={props.value}
        height="100%"
        width="100%"
        onChange={v => props.setValue(v)}
      />
    );
};

export default hot(JsonEditor);
