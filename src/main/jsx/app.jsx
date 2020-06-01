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

'use strict';

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

const App = () => {

  const [selectedView, setSelectedView] = useState('Schema');
  const [selectedSchema, setSelectedSchema] = useState('');
  const [selectedIndex, setSelectedIndex] = useState('');
  const [selectedField, setSelectedField] = useState('');
  const [indexJson, setIndexJson] = useState('{}');
  const [queryJson, setQueryJson] = useState(
    JSON.stringify(
      JSON.parse(
        '{"query":{"type": "MatchAllDocsQuery"},"returned_fields":["*"]}'), undefined, 2
    )
  );

  return (
    <React.Fragment>
      <div className="container-fluid">
        <Navbar selectedView={selectedView}
                setSelectedView={setSelectedView}/>
      </div>
      <div className="container-fluid h-100 overflow-auto p-0 m-0">
        <View selectedView={selectedView}
              selectedSchema={selectedSchema}
              setSelectedSchema={doSetSelectedSchema}
              selectedIndex={selectedIndex}
              setSelectedIndex={doSetSelectedIndex}
              selectedField={selectedField}
              setSelectedField={setSelectedField}
              indexJson={indexJson}
              setIndexJson={setIndexJson}
              queryJson={queryJson}
              setQueryJson={setQueryJson}
        />
      </div>
    </React.Fragment>
  );

  function doSetSelectedSchema(schema) {
    setSelectedSchema(schema);
    doSetSelectedIndex('');
  }

  function doSetSelectedIndex(index) {
    setSelectedIndex(index);
    setSelectedField('');
  }
}

ReactDOM.render(
  <App/>,
  document.getElementById('app')
);
