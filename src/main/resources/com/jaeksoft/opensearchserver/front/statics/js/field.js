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
  const [analyzer, setAnalyzer] = useState('');

  function Attribute(props) {
    const keyid = 'attr' + props.attributeKey;
    return /*#__PURE__*/React.createElement("div", {
      key: keyid,
      className: "form-check form-check-inline"
    }, /*#__PURE__*/React.createElement("input", {
      className: "form-check-input",
      type: "checkbox",
      id: keyid,
      value: props.attributeKey,
      checked: props.checked,
      onChange: e => props.setAttribute(e.target.checked)
    }), /*#__PURE__*/React.createElement("label", {
      className: "form-check-label",
      htmlFor: keyid
    }, props.attributeLabel));
  }

  const fieldTypes = ["text", "integer", "long", "double", "float"];
  const analyzers = ['', 'lowercase', 'ascii', 'arabic', 'bulgarian', 'cjk', 'czech', 'danish', 'dutch', 'english', 'french', 'finnish', 'german', 'greek', 'hindi', 'hungarian', 'irish', 'italian', 'lithuanian', 'latvian', 'norwegian', 'polish', 'portuguese', 'romanian', 'russian', 'spanish', 'swedish', 'turkish'];
  const fieldTypeOptions = fieldTypes.map(typ => /*#__PURE__*/React.createElement("option", {
    key: typ,
    value: typ
  }, typ));

  function Types(props) {
    return /*#__PURE__*/React.createElement("select", {
      className: "custom-select shadow-none rounded-0",
      value: props.value,
      onChange: e => props.setValue(e.target.value)
    }, fieldTypeOptions);
  }

  const analyzerOptions = analyzers.map(alzr => /*#__PURE__*/React.createElement("option", {
    key: alzr,
    value: alzr
  }, alzr));

  function Analyzers(props) {
    return /*#__PURE__*/React.createElement("select", {
      className: "custom-select shadow-none rounded-0",
      value: props.value,
      onChange: e => props.setValue(e.target.value)
    }, analyzerOptions);
  }

  function doCreateField(fieldName) {
    props.doCreateField(fieldName, {
      type: type.toUpperCase(),
      stored: stored,
      index: index,
      facet: facet,
      sort: sort,
      analyzer: analyzer === "" ? null : analyzer
    });
  }

  return /*#__PURE__*/React.createElement(React.Fragment, null, /*#__PURE__*/React.createElement("div", {
    className: "input-group p-1"
  }, /*#__PURE__*/React.createElement("input", {
    type: "text",
    className: "form-control shadow-none rounded-0",
    "aria-label": "edit field name",
    "aria-describedby": "edit field name",
    value: props.editFieldName,
    onChange: e => props.setEditFieldName(e.target.value)
  }), /*#__PURE__*/React.createElement("div", {
    className: "input-group-append"
  }, /*#__PURE__*/React.createElement(Types, {
    value: type,
    setValue: typ => setType(typ)
  }), /*#__PURE__*/React.createElement(Analyzers, {
    value: analyzer,
    setValue: alzr => setAnalyzer(alzr)
  }), /*#__PURE__*/React.createElement(CreateOrDeleteButton, {
    name: props.editFieldName,
    selectedName: props.selectedField,
    doDelete: name => props.doDeleteField(name),
    doCreate: name => doCreateField(name)
  }))), /*#__PURE__*/React.createElement("div", {
    className: "p-1"
  }, /*#__PURE__*/React.createElement(Attribute, {
    checked: index,
    attributeKey: 'index',
    attributeLabel: 'indexed',
    setAttribute: attr => setIndex(attr)
  }), /*#__PURE__*/React.createElement(Attribute, {
    checked: stored,
    attributeKey: 'stored',
    attributeLabel: 'stored',
    setAttribute: attr => setStored(attr)
  }), /*#__PURE__*/React.createElement(Attribute, {
    checked: facet,
    attributeKey: 'facet',
    attributeLabel: 'faceted',
    setAttribute: attr => setFacet(attr)
  }), /*#__PURE__*/React.createElement(Attribute, {
    checked: sort,
    attributeKey: 'sort',
    attributeLabel: 'sorted',
    setAttribute: attr => setSort(attr)
  })));
}