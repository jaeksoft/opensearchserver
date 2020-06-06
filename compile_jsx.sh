#!/bin/bash

npx babel --watch src/main/jsx \
    --out-dir src/main/resources/com/jaeksoft/opensearchserver/front/statics/js/ \
    --presets react-app/prod
