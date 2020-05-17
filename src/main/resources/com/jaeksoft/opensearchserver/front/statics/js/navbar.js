'use strict';

function Navbar() {
  return /*#__PURE__*/React.createElement("nav", {
    className: "navbar navbar-expand-lg navbar-light bg-light"
  }, /*#__PURE__*/React.createElement("a", {
    className: "navbar-brand",
    href: "#"
  }, /*#__PURE__*/React.createElement("img", {
    src: "/s/images/oss_logo_32.png",
    width: "32",
    height: "32",
    className: "d-inline-block align-top",
    alt: "OpenSearchServer 2.0"
  }), "OpenSearchServer 2.0"), /*#__PURE__*/React.createElement("button", {
    className: "navbar-toggler",
    type: "button",
    "data-toggle": "collapse",
    "data-target": "#navbarNav",
    "aria-controls": "navbarNav",
    "aria-expanded": "false",
    "aria-label": "Toggle navigation"
  }, /*#__PURE__*/React.createElement("span", {
    className: "navbar-toggler-icon"
  })), /*#__PURE__*/React.createElement("div", {
    className: "collapse navbar-collapse",
    id: "navbarNav"
  }, /*#__PURE__*/React.createElement("ul", {
    className: "navbar-nav"
  }, /*#__PURE__*/React.createElement("li", {
    className: "nav-item active"
  }, /*#__PURE__*/React.createElement("a", {
    className: "nav-link"
  }, "Accounts", /*#__PURE__*/React.createElement("span", {
    className: "sr-only"
  }, "(current)"))), /*#__PURE__*/React.createElement("li", {
    className: "nav-item"
  }, /*#__PURE__*/React.createElement("a", {
    className: "nav-link",
    href: "#"
  }, "Admin")), /*#__PURE__*/React.createElement("li", {
    className: "nav-item"
  }, /*#__PURE__*/React.createElement("a", {
    className: "nav-link",
    href: "#"
  }, "Sign In")), /*#__PURE__*/React.createElement("li", {
    className: "nav-item"
  }, /*#__PURE__*/React.createElement("a", {
    className: "nav-link",
    href: "#"
  }, "Log out")))));
}