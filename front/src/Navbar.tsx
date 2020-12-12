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
import * as React from 'react';
import {useContext} from 'react';
import {Views, Context} from "./Context"
import logo from './media/oss_logo_32.png';

const Navbar = () => {
  const [state] = useContext(Context);

  return (
    <nav className="navbar navbar-light navbar-expand bg-light">
      <a className="navbar-brand" href="/">
        <img src={logo} height="32" alt="" loading="lazy"/>
      </a>
      <div className="collapse navbar-collapse">
        <div className="navbar-nav mr-auto">
          <MenuItem enabled={true} label="Indices" view={Views.INDICES}/>
          <MenuItem enabled={true} label="Crawls" view={Views.CRAWLS}/>
          <MenuItem enabled={true} label="Queries" view={Views.QUERIES}/>
          <MenuItem enabled={true} label="GraphQL" view={Views.GRAPHQL}/>
        </div>
        <span className="navbar-text">TODO</span>
      </div>
    </nav>
  );
}

interface MenuProps {
  enabled: boolean,
  label: string,
  view: Views
}

const MenuItem = (props: MenuProps) => {
  const [state, dispatch] = useContext(Context);

  if (!props.enabled) {
    return (
      <a className="nav-item nav-link disabled" href="#">{props.view}</a>
    );
  } else if (state.selectedView === props.view) {
    return (
      <a className="nav nav-link active" href="#">{props.view} <span className="sr-only">(current)</span></a>
    );
  } else {
    return (
      <a className="nav-item nav-link" onClick={() => dispatch.selectView(props.view)} href="#">{props.view}</a>
    );
  }
}


export default Navbar;
