#!/bin/bash

cd front
yarn build
cd ..
rm src/main/resources/com/jaeksoft/opensearchserver/front/assets/*
cp front/build/assets/* src/main/resources/com/jaeksoft/opensearchserver/front/assets/.
cp -f front/build/index.html src/main/resources/com/jaeksoft/opensearchserver/front/.
