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
import SchemasTable from './SchemasTable';
import IndicesTable from "./IndicesTable";
import FieldsTable from "./FieldsTable";

const SchemaView = (props) => {

  return (
    <div className="d-flex">
      <SchemasTable selectedSchema={props.selectedSchema}
                    setSelectedSchema={props.setSelectedSchema}/>
      <IndicesTable selectedSchema={props.selectedSchema}
                    selectedIndex={props.selectedIndex}
                    setSelectedIndex={props.setSelectedIndex}/>
      <FieldsTable selectedSchema={props.selectedSchema}
                   selectedIndex={props.selectedIndex}
                   selectedField={props.selectedField}
                   setSelectedField={props.setSelectedField}/>
    </div>
  );

}

const View = (props) => {

  switch (props.selectedView) {
    case 'Schema':
      return <SchemaView
        selectedSchema={props.selectedSchema}
        setSelectedSchema={props.setSelectedSchema}
        selectedIndex={props.selectedIndex}
        setSelectedIndex={props.setSelectedIndex}
        selectedField={props.selectedField}
        setSelectedField={props.setSelectedField}
      />;
    case 'Index':
      return <IndexView
        selectedSchema={props.selectedSchema}
        setSelectedSchema={props.setSelectedSchema}
        selectedIndex={props.selectedIndex}
        setSelectedIndex={props.setSelectedIndex}
        indexJson={props.indexJson}
        setIndexJson={props.setIndexJson}
      />
    case 'Query':
      return <QueryView
        selectedSchema={props.selectedSchema}
        setSelectedSchema={props.setSelectedSchema}
        selectedIndex={props.selectedIndex}
        setSelectedIndex={props.setSelectedIndex}
        queryJson={props.queryJson}
        setQueryJson={props.setQueryJson}
      />
    default:
      return null;
  }
}

export default hot(View);
