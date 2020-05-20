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
/**
 *
 * @param props values, selectedValue, doSelectValue
 * @returns {*}
 */
const List = (props) => {
  const values = Array.isArray(props.values) ? props.values : Object.keys(props.values);
  const listItems = values.map(value => (
    <ListItem key={value}
              value={value}
              selectedValue={props.selectedValue}
              doSelectValue={value => props.doSelectValue(value)}
    />
  ));
  return (
    <ul className="list-group-flush p-0 m-0">
      {listItems}
    </ul>
  );
}

/**
 *
 * @param props value, selectedValue, doSelectValue
 * @returns {*}
 */
const ListItem = (props) => {

  if (props.selectedValue === props.value) {
    return (
      <li className="list-group-item active p-1 m-0"
          onClick={() => props.doSelectValue(props.value)}>
        {props.value}
      </li>
    );
  } else {
    return (
      <li className="list-group-item p-1 m-0"
          onClick={() => props.doSelectValue(props.value)}>
        {props.value}
      </li>
    );
  }
}
