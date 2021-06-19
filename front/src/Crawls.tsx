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

import {useDispatch, useSelector} from "react-redux";
import {CrawlsViews, setCrawlsView, State} from "./store";
import {AppBar, Tab, Tabs} from "@material-ui/core";
import * as React from "react";
import WebCrawls from "./WebCrawls";
import FileCrawls from "./FileCrawls";
import WebCrawl from "./WebCrawlEdit";
import FileCrawl from "./FileCrawl";

const Crawls = () => {
  const dispatch = useDispatch();
  const crawlsView = useSelector<State>(state => state.crawlsView);
  const editWebCrawl= useSelector<State>(state => state.editWebCrawl);

  const handleChange = (event: React.ChangeEvent<{}>, newValue: number) => {
    dispatch(setCrawlsView(newValue))
  };

  console.trace("crawlsview", crawlsView);

  return (
    <div>
      <AppBar position="static">
        <Tabs value={crawlsView} onChange={handleChange} aria-label="Crawls tabs">
          <Tab label="Web Crawls" value={CrawlsViews.WEB_CRAWLS}/>
          <Tab label="File Crawls" value={CrawlsViews.FILE_CRAWLS}/>
        </Tabs>
      </AppBar>
      {(crawlsView === CrawlsViews.WEB_CRAWLS && !editWebCrawl) && <WebCrawls/>}
      {(crawlsView === CrawlsViews.FILE_CRAWLS && true) && <FileCrawls/>}
      {(crawlsView === CrawlsViews.WEB_CRAWLS && editWebCrawl) && <WebCrawl/>}
      {(crawlsView === CrawlsViews.FILE_CRAWLS &&false) && <FileCrawl/>}
    </div>
  )
}

export default Crawls;
