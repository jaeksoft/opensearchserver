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
 * @param request
 * @param init
 * @param doJson
 * @param doError
 * @returns {Promise<String>}
 */
export function fetchJson(request, init, doJson, doError) {
  if (!doError)
    doError = console.log;
  fetch(request, init)
    .then(response => Promise.all([response.ok, response.json()]))
    .then(
      ([responseOk, responseJson]) => {
        if (responseOk) {
          return doJson(responseJson);
        } else {
          return doError(responseJson.message);
        }
      })
    .catch(error => doError(error.message));
}

export function parseJson(notParsed) {
  if (notParsed === null || notParsed === '') {
    throw 'Nothing to index';
  }
  return JSON.parse(notParsed);
}

export function simpleFetch(request, init, doOk, doError) {
  if (!doError)
    doError = console.log;
  fetch(request, init)
    .then(response => Promise.all([response.ok, response.text()]))
    .then(
      ([responseOk, responseText]) => {
        if (responseOk) {
          return doOk(responseText);
        } else {
          return doError(responseText);
        }
      })
    .catch(error => doError(error.message));
}
