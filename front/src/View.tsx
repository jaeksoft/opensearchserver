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

import * as React from 'react';
import {useContext} from 'react';
import {Context, Views} from "./Context"
import Gql from "./Gql";

const View = () => {
  const [state] = useContext(Context);

  console.log("View: ", state);

  switch (state.selectedView) {
    case Views.INDICES:
      return (
        <div>INDICES</div>
      );
    case Views.CRAWLS:
      return (
        <div>CRAWLS</div>
      );
    case Views.QUERIES:
      return (
        <div>QUERIES</div>
      );
    case Views.GRAPHQL:
      return (
        <Gql/>
      );
    default:
      return null;
  }
}

export default View;
