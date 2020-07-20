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
import QueryView from "./QueryView";
import IndicesView from "./IndicesView";
import FieldsView from "./FieldsView";
import IngestView from "./IngestView";
import WebCrawlView from "./WebCrawlView";

const View = (props) => {

  switch (props.selectedView) {
    case 'Indices':
      return <IndicesView oss={props.oss}
                          selectedIndex={props.selectedIndex}
                          setSelectedIndex={props.setSelectedIndex}
                          selectedIndexStatus={props.selectedIndexStatus}
                          setSelectedIndexStatus={props.setSelectedIndexStatus}
      />;
    case 'Fields':
      return <FieldsView oss={props.oss}
                         selectedIndex={props.selectedIndex}
                         setSelectedIndex={props.setSelectedIndex}
                         indexJson={props.indexJson}
                         setIndexJson={props.setIndexJson}
                         selectedField={props.selectedField}
                         setSelectedField={props.setSelectedField}
      />
    case 'Ingest':
      return <IngestView oss={props.oss}
                         selectedIndex={props.selectedIndex}
                         setSelectedIndex={props.setSelectedIndex}
                         indexJson={props.indexJson}
                         setIndexJson={props.setIndexJson}
      />
    case 'Query':
      return <QueryView oss={props.oss}
                        selectedIndex={props.selectedIndex}
                        setSelectedIndex={props.setSelectedIndex}
                        queryJson={props.queryJson}
                        setQueryJson={props.setQueryJson}
      />
    case 'Web Crawl':
      return <WebCrawlView oss={props.oss}
                           webCrawlDefinition={props.webCrawlDefinition}
                           setWebCrawlDefinition={props.setWebCrawlDefinition}
      />
    default:
      return null;
  }
}

export default hot(View);
