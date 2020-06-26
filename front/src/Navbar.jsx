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
import React, {useEffect} from 'react';

const Navbar = (props) => {

  useEffect(() => {
  }, [props.selectedIndex])

  return (
    <nav className="navbar navbar-light navbar-expand bg-light">
      <span className="navbar-brand text-secondary"><small>OpenSearchServer 2.0</small></span>
      <div className="collapse navbar-collapse">
        <div className="navbar-nav mr-auto">
          <MenuItem selectedView={props.selectedView}
                    setSelectedView={props.setSelectedView}
                    enabled={true}
                    view="Indices"/>
          <MenuItem selectedView={props.selectedView}
                    setSelectedView={props.setSelectedView}
                    enabled={props.selectedIndex}
                    view="Fields"/>
          <MenuItem selectedView={props.selectedView}
                    setSelectedView={props.setSelectedView}
                    enabled={props.selectedIndex}
                    view="Ingest"/>
          <MenuItem selectedView={props.selectedView}
                    setSelectedView={props.setSelectedView}
                    enabled={props.selectedIndex}
                    view="Query"/>
        </div>
        <SelectedIndex selectedIndex={props.selectedIndex}/>
      </div>
    </nav>
  );
}

const MenuItem = (props) => {

  if (!props.enabled) {
    return (
      <a className="nav-item nav-link disabled" href="#">{props.view}</a>
    );
  } else if (props.selectedView === props.view) {
    return (
      <a className="nav nav-link active" href="#">{props.view} <span className="sr-only">(current)</span></a>
    );
  } else {
    return (
      <a className="nav-item nav-link" onClick={() => props.setSelectedView(props.view)} href="#">{props.view}</a>
    );
  }
}

const SelectedIndex = (props) => {

  useEffect(() => {
  }, [props.selectedIndex])

  if (!props.selectedIndex)
    return null;
  return (<span className="navbar-text">{props.selectedIndex}</span>);
}

export default hot(Navbar);
