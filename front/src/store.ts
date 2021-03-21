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

import {createStore} from "redux";

export const STATE_KEY = "opensearchserver:state";

export enum Views {
  INDICES,
  CRAWLS,
  QUERIES,
  GRAPHQL,
  SCHEMA,
  CRAWL
}

export interface State {
  view: Views
  selectedIndex: string | undefined,
  selectedCrawl: string | undefined,
}

const defaultState: State = {
  view: Views.INDICES,
  selectedIndex: undefined,
  selectedCrawl: undefined
}

enum ActionTypes {
  SET_VIEW,
  EDIT_SCHEMA,
  EDIT_CRAWL
}

interface SetViewAction {
  type: ActionTypes.SET_VIEW;
  view: Views;
}

interface EditSchemaAction {
  type: ActionTypes.EDIT_SCHEMA;
  selectedIndex: string;
}

interface EditCrawlAction {
  type: ActionTypes.EDIT_CRAWL;
  selectedCrawl: string;
}

type Actions = SetViewAction | EditSchemaAction | EditCrawlAction;

export const setView = (view: Views): SetViewAction => {
  return {type: ActionTypes.SET_VIEW, view};
};

export const editSchema = (selectedIndex: string): EditSchemaAction => {
  return {type: ActionTypes.EDIT_SCHEMA, selectedIndex};
};

export const editCrawl = (selectedCrawl: string): EditCrawlAction => {
  return {type: ActionTypes.EDIT_CRAWL, selectedCrawl};
};

const reducer = (state: State = defaultState, action: Actions): State => {
  switch (action.type) {
    case ActionTypes.SET_VIEW:
      state = {...state, view: action.view};
      break;
    case ActionTypes.EDIT_SCHEMA:
      state = {...state, selectedIndex: action.selectedIndex, view: Views.SCHEMA};
      break;
    case ActionTypes.EDIT_CRAWL:
      state = {...state, selectedCrawl: action.selectedCrawl, view: Views.CRAWL};
      break;
    default:
      return state;
  }
  window.localStorage.setItem(STATE_KEY, JSON.stringify(state));
  return state;
}

// Store creation
const storageState = window.localStorage.getItem(STATE_KEY);
const initialState: State = storageState ? JSON.parse(storageState) : defaultState;

export default createStore(reducer, initialState)

