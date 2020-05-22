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

function FieldCreateEditDelete(props) {

  const [index, setIndex] = useState(true);
  const [stored, setStored] = useState(false);
  const [facet, setFacet] = useState(false);
  const [sort, setSort] = useState(false);
  const [type, setType] = useState('text');

  function Attribute(props) {
    const keyid = 'attr' + props.attributeKey;
    return (
      <div key={keyid} className="form-check form-check-inline">
        <input className="form-check-input"
               type="checkbox"
               id={keyid}
               value={props.attributeKey}
               checked={props.checked}
               onChange={e => props.setAttribute(e.target.checked)}
        />
        <label className="form-check-label" htmlFor={keyid}>{props.attributeLabel}</label>
      </div>
    );
  }

  const fieldTypes = ["text", "integer", "long", "double", "float"];

  const fieldTypeOptions = fieldTypes.map(typ => (
      <option key={typ} value={typ}>{typ}</option>
    )
  );

  function Types(props) {
    return (<select className="custom-select shadow-none rounded-0"
                    value={props.value}
                    onChange={e => props.setValue(e.target.value)}>
      {fieldTypeOptions}
    </select>);
  }

  function doCreateField(fieldName) {
    props.doCreateField(fieldName,
      {
        type: type.toUpperCase(),
        stored: stored,
        index: index,
        facet: facet,
        sort: sort
      });
  }

  return (
    <React.Fragment>
      <div className="input-group p-1">
        <input type="text" className="form-control shadow-none rounded-0"
               aria-label="edit field name" aria-describedby="edit field name"
               value={props.editFieldName} onChange={e => props.setEditFieldName(e.target.value)}
        />
        <div className="input-group-append">
          <Types value={type} setValue={typ => setType(typ)}/>
          <CreateOrDeleteButton
            name={props.editFieldName}
            selectedName={props.selectedField}
            doDelete={name => props.doDeleteField(name)}
            doCreate={name => doCreateField(name)}
          />
        </div>
      </div>
      <div className="p-1">
        <Attribute checked={index}
                   attributeKey={'index'}
                   attributeLabel={'indexed'}
                   setAttribute={attr => setIndex(attr)}/>
        <Attribute checked={stored}
                   attributeKey={'stored'}
                   attributeLabel={'stored'}
                   setAttribute={attr => setStored(attr)}/>
        <Attribute checked={facet}
                   attributeKey={'facet'}
                   attributeLabel={'faceted'}
                   setAttribute={attr => setFacet(attr)}/>
        <Attribute checked={sort}
                   attributeKey={'sort'}
                   attributeLabel={'sorted'}
                   setAttribute={attr => setSort(attr)}/>
      </div>
    </React.Fragment>
  );
}
