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

const Navbar = () => {
  return /*#__PURE__*/React.createElement("nav", {
    className: "navbar navbar-light bg-light"
  }, /*#__PURE__*/React.createElement("a", {
    className: "navbar-brand",
    href: "#"
  }, /*#__PURE__*/React.createElement("img", {
    src: "/s/images/oss_logo_32.png",
    width: "32",
    height: "32",
    className: "d-inline-block align-top",
    alt: "OpenSearchServer",
    loading: "lazy"
  })), /*#__PURE__*/React.createElement("span", {
    className: "navbar-brand text-secondary"
  }, /*#__PURE__*/React.createElement("small", null, "OpenSearchServer 2.0")));
};