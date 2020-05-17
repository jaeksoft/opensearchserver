#!/bin/bash

npm install --save-dev @babel/core @babel/cli @babel/plugin-transform-react-jsx

npx babel \
  --plugins @babel/plugin-transform-react-jsx \
  src/main/jsx \
  --out-dir src/main/resources/com/jaeksoft/opensearchserver/front/statics/js
