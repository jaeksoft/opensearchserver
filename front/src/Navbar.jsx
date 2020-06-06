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

const Navbar = (props) => {

  return (
    <nav className="navbar sticky-top navbar-light navbar-expand bg-light p-0">
      <span className="navbar-brand text-secondary"><small>OpenSearchServer 2.0</small></span>
      <div className="collapse navbar-collapse">
        <ul className="navbar-nav mr-auto">
          <MenuItem selectedView={props.selectedView}
                    setSelectedView={props.setSelectedView}
                    view="Schema"/>
          <MenuItem selectedView={props.selectedView}
                    setSelectedView={props.setSelectedView}
                    view="Index"/>
          <MenuItem selectedView={props.selectedView}
                    setSelectedView={props.setSelectedView}
                    view="Query"/>
        </ul>
      </div>
    </nav>
  );
}

function MenuItem(props) {

  if (props.selectedView === props.view) {
    return (
      <li className="nav-item active">
        <a className="nav-link" href="#">{props.view} <span className="sr-only">(current)</span></a>
      </li>);
  } else {
    return (
      <li className="nav-item">
        <a className="nav-link"
           onClick={() => props.setSelectedView(props.view)}
           href="#">
          {props.view}
        </a>
      </li>
    );
  }
}

export default hot(Navbar);
