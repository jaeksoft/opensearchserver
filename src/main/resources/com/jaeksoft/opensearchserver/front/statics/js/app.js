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

const {
  useState,
  useEffect
} = React;

const useAsyncError = () => {
  const [_, setError] = React.useState();
  return React.useCallback(e => {
    setError(() => {
      throw e;
    });
  }, [setError]);
};

const useFetch = url => {
  const [data, setData] = useState(null); // empty array as second argument equivalent to componentDidMount

  useEffect(() => {
    async function fetchData() {
      const response = await fetch(url);
      const json = await response.json();
      setData(json);
    }

    fetchData();
  }, [url]);
  return data;
};

const App = () => {
  return /*#__PURE__*/React.createElement("div", null, /*#__PURE__*/React.createElement(Navbar, null), /*#__PURE__*/React.createElement(Schemas, null));
};

ReactDOM.render( /*#__PURE__*/React.createElement(App, null), document.getElementById('app'));