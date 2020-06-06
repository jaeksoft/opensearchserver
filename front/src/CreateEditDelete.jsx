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
 * @param props name, selectedName, doDelete, doCreate
 * @returns {*}
 */
const CreateOrDeleteButton = (props) => {

  if (props.name === props.selectedName) {
    return (
      <button className="btn btn-danger shadow-none rounded-0"
              type="button"
              onClick={() => props.doDelete(props.name)}>
        Delete
      </button>
    );
  } else {
    return (
      <button className="btn btn-primary shadow-none rounded-0"
              type="button"
              onClick={() => props.doCreate(props.name)}>
        Create
      </button>
    );
  }
}

/**
 *
 * @param props doCreate, doDelete, name, setName, selectedName
 * @returns {*}
 * @constructor
 */
const CreateEditDelete = (props) => {

  return (
    <div className="input-group p-1">
      <input type="text" className="form-control shadow-none"
             aria-label="name" aria-describedby="create"
             value={props.name} onChange={e => props.setName(e.target.value)}
      />
      <CreateOrDeleteButton
        name={props.name}
        selectedName={props.selectedName}
        doDelete={name => props.doDelete(name)}
        doCreate={name => props.doCreate(name)}
      />
    </div>
  );
}

export default hot(CreateOrDeleteButton, CreateEditDelete);

