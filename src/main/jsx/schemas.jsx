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

function Schemas(props) {

  const [status, setStatus] = useState(newStatus());
  const [schemas, setSchemas] = useState([]);
  const [schemaName, setSchemaName] = useState('');

  useEffect(() => {
    doFetchSchemas();
  }, [])

  return (
    <div className="border p-0 mt-1 ml-1 bg-light rounded">
      <div className="bg-light text-secondary p-1">SCHEMAS&nbsp;
        <Status status={status}/>
      </div>
      <CreateEditDelete
        name={schemaName}
        setName={sch => setSchemaName(sch)}
        selectedName={props.selectedSchema}
        doCreate={sch => doCreateSchema(sch)}
        doDelete={sch => doDeleteSchema(sch)}
      />
      <List values={schemas}
            selectedValue={props.selectedSchema}
            doSelectValue={value => props.setSelectedSchema(value)}/>
    </div>
  );

  function doCreateSchema(sch) {
    setStatus(startTask(status, 'Creating schema ' + sch));
    fetchJson('/ws/indexes/' + sch, {method: 'POST'},
      json => {
        setStatus(endTask(status, 'Schema created'));
        doFetchSchemas();
      }, error => setStatus(endTask(status, null, error)));
  }

  function doDeleteSchema(sch) {
    setStatus(startTask(status, 'Deleting schema ' + sch));
    fetchJson('/ws/indexes/' + sch, {method: 'DELETE'},
      json => {
        props.setSelectedSchema(null);
        setStatus(endTask(status, 'Schema deleted'));
        doFetchSchemas();
      }, error => setStatus(endTask(status, null, error)));
  }

  function doFetchSchemas() {
    setStatus(startTask(status, null));
    fetchJson('/ws/indexes', null,
      json => {
        setStatus(endTask(status));
        setSchemas(json);
      },
      error => setStatus(endTask(status, null, error)));
  }

}
