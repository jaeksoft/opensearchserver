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

function Spinning(props) {

  if (props.spinning) {
    return (
      <div className="spinner-border spinner-border-sm" role="status">
        <span className="sr-only">Loading...</span>
      </div>
    );
  } else {
    return '';
  }
}

function Status(props) {

  useEffect(() => {
  }, [props.error, props.task, props.spinning])

  console.log('Update status: ' + props.error + ' ' + props.task);

  if (props.error && props.task) {
    return (
      <React.Fragment>
        <Spinning spinning={props.spinning}/>
        <div className="text-danger float-right">
          <small>{props.task}: {props.error}</small>
        </div>
      </React.Fragment>
    );
  } else if (props.error) {
    return (
      <React.Fragment>
        <Spinning spinning={props.spinning}/>
        <div className="text-danger float-right">
          <small>{props.error}</small>
        </div>
      </React.Fragment>
    );
  } else if (props.task) {
    return (
      <React.Fragment>
        <Spinning spinning={props.spinning}/>
        <div className="text-success float-right">
          <small>{props.task}</small>
        </div>
      </React.Fragment>
    );
  } else return (
    <React.Fragment>
      <Spinning spinning={props.spinning}/>
      &nbsp;
    </React.Fragment>
  );
}
