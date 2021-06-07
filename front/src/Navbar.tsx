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
import AppBar from '@material-ui/core/AppBar';
import * as React from 'react';
import {Tab, Tabs, Toolbar} from "@material-ui/core"
import {useDispatch, useSelector} from "react-redux";
import {setView, State, Views} from "./store";
import oss_logo from "./media/oss_logo.png";

const Navbar = () => {
  const view = useSelector<State>(state => state.view)
  const dispatch = useDispatch();

  const handleChange = (event: React.ChangeEvent<{}>, newValue: Views) => {
    dispatch(setView(newValue))
  };

  return (
    <AppBar position="static" color={"transparent"}>
      <Toolbar>
        <img src={oss_logo} alt="OpenSearchServer Logo" style={{maxHeight: "48px", width: "auto"}}/>
        <Tabs value={view} onChange={handleChange} aria-label="Main tab navigation">
          <Tab label="Indices" value={Views.INDICES}/>
          <Tab label="Crawls" value={Views.CRAWLS}/>
          <Tab label="Queries" value={Views.QUERIES}/>
          <Tab label="GraphQL" value={Views.GRAPHQL}/>
        </Tabs>
      </Toolbar>
    </AppBar>
  );
}


export default Navbar;
