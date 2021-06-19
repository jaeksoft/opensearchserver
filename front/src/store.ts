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
import {WebCrawlSettings} from "./types";

export const STATE_KEY = "opensearchserver:state";

export enum Views {
  INDICES,
  CRAWLS,
  QUERIES,
  GRAPHQL,
  SCHEMA,
}

export enum CrawlsViews {
  WEB_CRAWLS,
  FILE_CRAWLS,
}

export interface State {
  view: Views,
  crawlsView: CrawlsViews,
  selectedIndex: string | undefined,
  editWebCrawl: {
    crawlName: string,
    indexName: string | undefined,
    settings: WebCrawlSettings | undefined
  } | undefined,
  selectedFileCrawl: string | undefined,
}

const defaultState: State = {
  view: Views.INDICES,
  crawlsView: CrawlsViews.WEB_CRAWLS,
  selectedIndex: undefined,
  editWebCrawl: undefined,
  selectedFileCrawl: undefined
}

enum ActionTypes {
  SET_VIEW,
  SET_CRAWLS_VIEW,
  EDIT_SCHEMA,
  EDIT_WEB_CRAWL,
  EDIT_FILE_CRAWL
}

interface SetViewAction {
  type: ActionTypes.SET_VIEW;
  view: Views;
}

interface SetCrawlsViewAction {
  type: ActionTypes.SET_CRAWLS_VIEW;
  crawlsView: CrawlsViews;
}

interface EditSchemaAction {
  type: ActionTypes.EDIT_SCHEMA;
  selectedIndex: string;
}

interface EditWebCrawlAction {
  type: ActionTypes.EDIT_WEB_CRAWL;
  crawlName: string;
  indexName?: string;
  settings?: WebCrawlSettings;
}

interface EditFileCrawlAction {
  type: ActionTypes.EDIT_FILE_CRAWL;
  selectedCrawl: string;
}

type Actions = SetViewAction | SetCrawlsViewAction | EditSchemaAction | EditWebCrawlAction | EditFileCrawlAction;

export const setView = (view: Views): SetViewAction => {
  return {type: ActionTypes.SET_VIEW, view};
};

export const setCrawlsView = (crawlsView: CrawlsViews): SetCrawlsViewAction => {
  return {type: ActionTypes.SET_CRAWLS_VIEW, crawlsView};
};

export const editSchema = (selectedIndex: string): EditSchemaAction => {
  return {type: ActionTypes.EDIT_SCHEMA, selectedIndex};
};

export const editWebCrawl = (crawlName: string, indexName?: string, settings?: WebCrawlSettings): EditWebCrawlAction => {
  return {type: ActionTypes.EDIT_WEB_CRAWL, crawlName, indexName, settings};
};

export const editFileCrawl = (selectedCrawl: string): EditFileCrawlAction => {
  return {type: ActionTypes.EDIT_FILE_CRAWL, selectedCrawl};
};

const reducer = (state: State = defaultState, action: Actions): State => {
  switch (action.type) {
    case ActionTypes.SET_VIEW:
      state = {...state, view: action.view};
      break;
    case ActionTypes.SET_CRAWLS_VIEW:
      state = {...state, view: Views.CRAWLS, crawlsView: action.crawlsView, editWebCrawl: undefined};
      break;
    case ActionTypes.EDIT_SCHEMA:
      state = {
        ...state,
        selectedIndex: action.selectedIndex,
        view: Views.SCHEMA
      };
      break;
    case ActionTypes.EDIT_WEB_CRAWL:
      state = {
        ...state,
        editWebCrawl: {crawlName: action.crawlName, indexName: action.indexName, settings: action.settings},
        view: Views.CRAWLS,
        crawlsView: CrawlsViews.WEB_CRAWLS
      };
      break;
    case ActionTypes.EDIT_FILE_CRAWL:
      state = {
        ...state,
        selectedFileCrawl: action.selectedCrawl,
        view: Views.CRAWLS,
        crawlsView: CrawlsViews.FILE_CRAWLS
      };
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

