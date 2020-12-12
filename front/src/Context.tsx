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
import {createContext, Dispatch, useReducer} from "react";

const defaultEndPoint = 'http://localhost:9091/indexes';

export enum Views {
  CRAWLS = "CRAWLS",
  INDICES = "INDICES",
  QUERIES = "QUERIES",
  GRAPHQL = "GRAPHQL"
}

enum Actions {
  SELECT_VIEW = "SELECT_VIEW",
  SELECT_INDEX = "SELECT_INDEX",
  SELECT_CRAWL = "SELECT_CRAWL",
  SELECT_QUERY = "SELECT_QUERY",
  SET_ERROR = "SET_ERROR",
}

interface SelectViewAction {
  type: typeof Actions.SELECT_VIEW,
  view: Views,
}

interface SelectIndexAction {
  type: typeof Actions.SELECT_INDEX,
  index: string | undefined,
}

interface SelectCrawlAction {
  type: typeof Actions.SELECT_CRAWL,
  crawl: string | undefined
}

interface SelectQueryAction {
  type: typeof Actions.SELECT_QUERY,
  query: string | undefined
}

interface SetErrorAction {
  type: typeof Actions.SET_ERROR,
  error: string | undefined
}

type StateActions = SelectViewAction | SelectIndexAction | SelectCrawlAction | SelectQueryAction | SetErrorAction;

// Definition of the state
type StateType = {
  selectedView: Views;
  selectedIndex: string | undefined;
  selectedCrawl: string | undefined,
  selectedQuery: string | undefined,
  endPoint: string,
  error: string | undefined,
}

// Initial value of the App Context state
const defaultState: StateType = {
  selectedView: Views.INDICES,
  selectedIndex: undefined,
  selectedCrawl: undefined,
  selectedQuery: undefined,
  endPoint: defaultEndPoint,
  error: undefined,
}

const initialState = () => {
  let state = defaultState;
  const viewString = localStorage.getItem(Actions.SELECT_VIEW);
  if (viewString != null) {
    const typeViewString = viewString as keyof typeof Views;
    state = {...state, selectedView: Views[typeViewString]}
  }
  const index = localStorage.getItem(Actions.SELECT_INDEX);
  if (index != null)
    state = {...state, selectedIndex: index};
  const crawl = localStorage.getItem(Actions.SELECT_CRAWL);
  if (crawl != null)
    state = {...state, selectedCrawl: crawl};
  const query = localStorage.getItem(Actions.SELECT_QUERY);
  if (query != null)
    state = {...state, selectedQuery: query};
  return state;
}

const reducer = (state: StateType, action: StateActions): StateType => {
  let newState: StateType;
  switch (action.type) {
    case Actions.SELECT_VIEW:
      newState = {
        ...state,
        selectedView: action.view
      };
      break;
    case Actions.SELECT_INDEX:
      newState = {
        ...state,
        selectedIndex: action.index
      };
      break;
    case Actions.SELECT_CRAWL:
      newState = {
        ...state,
        selectedCrawl: action.crawl
      };
      break;
    case Actions.SELECT_QUERY:
      newState = {
        ...state,
        selectedQuery: action.query
      };
      break;
    case Actions.SET_ERROR:
      newState = {
        ...state,
        error: action.error
      }
      break;
    default:
      return state;
  }
  return newState;
}

class Dispatcher {

  constructor(private readonly dispatch: Dispatch<StateActions>) {
  }

  selectView(view: Views) {
    localStorage.setItem(Actions.SELECT_VIEW, view);
    return this.dispatch({type: Actions.SELECT_VIEW, view});
  }

  selectIndex(index: string | undefined) {
    if (index !== undefined)
      localStorage.setItem(Actions.SELECT_INDEX, index);
    else
      localStorage.removeItem(Actions.SELECT_INDEX);
    return this.dispatch({type: Actions.SELECT_INDEX, index});
  }

  selectCrawl(crawl: string | undefined) {
    if (crawl !== undefined)
      localStorage.setItem(Actions.SELECT_CRAWL, crawl);
    else
      localStorage.removeItem(Actions.SELECT_CRAWL);
    return this.dispatch({type: Actions.SELECT_CRAWL, crawl});
  }

  selectQuery(query: string | undefined) {
    if (query !== undefined)
      localStorage.setItem(Actions.SELECT_QUERY, query);
    else
      localStorage.removeItem(Actions.SELECT_QUERY);
    return this.dispatch({type: Actions.SELECT_QUERY, query});
  }

  setError(error: string) {
    if (error !== undefined)
      localStorage.setItem(Actions.SET_ERROR, error);
    else
      localStorage.removeItem(Actions.SET_ERROR);
    return this.dispatch({type: Actions.SET_ERROR, error});
  }
}

export const Context = createContext<[StateType, Dispatcher]>([defaultState,
  new Dispatcher(() => {
  })]);

const ContextProvider = (props: any) => {
  const [state, dispatch] = useReducer(reducer, initialState());
  const dispatcher = new Dispatcher(dispatch);

  return <Context.Provider value={[state, dispatcher]}>
    {props.children}
  </Context.Provider>
}

export default ContextProvider;
