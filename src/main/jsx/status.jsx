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

const Spinning = (props) => {

  if (props.spinning) {
    return (
      <div className="spinner-border spinner-border-sm float-right" role="status">
        <span className="sr-only">Loading...</span>
      </div>
    );
  } else {
    return '';
  }
}

const Status = (props) => {

  if (props.error && props.status) {
    return (
      <div className="row">
        <div className="col-md-6 alert alert-primary" role="alert" title={'Last status'}>
          <Spinning spinning={props.spinning}/>
          {props.status}
        </div>
        <div className="col-md-6 alert alert-danger" role="alert" title={'Last error'}>
          {props.error}
        </div>
      </div>
    );
  } else if (props.error) {
    return (
      <div className="row">
        <div className="alert alert-danger col-md-12" role="alert" title={'Last error'}>
          <Spinning spinning={props.spinning}/>
          {props.error}
        </div>
      </div>
    );
  } else if (props.status) {
    return (
      <div className="row">
        <div className="alert alert-primary col-md-12" role="alert" title={'Last status'}>
          <Spinning spinning={props.spinning}/>
          {props.status}
        </div>
      </div>
    );
  } else return (
    <div className="row">
      <div className="col-md-12 alert alert-primary" role="alert" title={'Last status'}>
        <Spinning spinning={props.spinning}/>
        &nbsp;
      </div>
    </div>);
}

