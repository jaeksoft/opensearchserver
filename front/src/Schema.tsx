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


import {useDispatch, useSelector} from "react-redux";
import {setView, State, Views} from "./store";
import {Button} from "@material-ui/core";

const Schema = () => {
  const selectedIndex = useSelector<State>(state => state.selectedIndex)
  const dispatch = useDispatch();

  return (<div>
    Edit Schema ? {selectedIndex}
    <Button onClick={() => dispatch(setView(Views.INDICES))}>Back</Button>
  </div>)
}

export default Schema;
