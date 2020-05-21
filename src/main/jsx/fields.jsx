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

function Fields(props) {

  const [status, setStatus] = useState(newStatus());
  const [fields, setFields] = useState([]);
  const [editFieldName, setEditFieldName] = useState('');

  useEffect(() => {
    doFetchFields();
  }, [props.selectedSchema, props.selectedIndex])

  if (!props.selectedSchema || !props.selectedIndex)
    return null;

  return (
    <div className="border p-0 mt-1 ml-1 bg-light rounded">
      <div className="bg-light text-secondary p-1">FIELDS
        <Status status={status}/>
      </div>
      <FieldCreateEditDelete editFieldName={editFieldName}
                             setEditFieldName={field => setEditFieldName(field)}
                             selectedName={props.selectedField}
                             doCreateField={idx => doCreateField(idx)}
                             doDeleteField={idx => doDeleteField(idx)}
      />
      <List values={fields}
            selectedValue={props.setSelectedField}
            doSelectValue={value => props.setSelectedField(value)}/>
    </div>
  );

  function doFetchFields() {
    const schema = props.selectedSchema;
    const index = props.selectedIndex;
    if (!index || !schema) {
      return;
    }
    setStatus(startTask(status));
    fetchJson('/ws/indexes/' + schema + '/' + index + '/fields', null,
      json => {
        setStatus(endTask(status));
        setFields(json);
      },
      error => setStatus(endTask(status, null, error)));
  }

  function doCreateField(fieldName) {

  }

  function doDeleteField(fieldName) {

  }

}

const fieldAttributes = ["index", "stored", "facet", "sort"];

function FieldCreateEditDelete(props) {

  const fieldAttributesCheckboxes = fieldAttributes.map(attribute => (
    <div className="form-check form-check-inline">
      <input className="form-check-input" type="checkbox" id="fieldAttr{attribute}" value="{attribute}"/>
      <label className="form-check-label" htmlFor="fieldAttr{attribute}">{attribute}</label>
    </div>
  ));

  return (
    <React.Fragment>
      <div className="input-group p-1">
        <input type="text" className="form-control shadow-none"
               aria-label="edit field name" aria-describedby="edit field name"
               value={props.editFieldName} onChange={e => props.setEditFieldName(e.target.value)}
        />
        <div className="input-group-append">
          <select className="custom-select shadow-none">
            <option value="text">Text</option>
            <option value="integer">Integer</option>
            <option value="long">Long</option>
            <option value="double">Double</option>
            <option value="float">Float</option>
          </select>
          <CreateOrDeleteButton
            name={props.editFieldName}
            selectedName={props.selectedField}
            doDelete={name => props.doDeleteField(name)}
            doCreate={name => props.doCreateField(name)}
          />
        </div>
      </div>
      <div className="p-1">
        {fieldAttributesCheckboxes}
      </div>
    </React.Fragment>
  );
}
