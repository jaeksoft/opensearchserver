#!/bin/bash

cd front
npm run build
cd ..
rm src/main/resources/com/jaeksoft/opensearchserver/front/*
rm src/main/resources/com/jaeksoft/opensearchserver/front/static/media/*
rm src/main/resources/com/jaeksoft/opensearchserver/front/static/css/*
rm src/main/resources/com/jaeksoft/opensearchserver/front/static/js/*
cp front/build/* src/main/resources/com/jaeksoft/opensearchserver/front/.
cp front/build/static/media/* src/main/resources/com/jaeksoft/opensearchserver/front/static/media/.
cp front/build/static/css/* src/main/resources/com/jaeksoft/opensearchserver/front/static/css/.
cp front/build/static/js/* src/main/resources/com/jaeksoft/opensearchserver/front/static/js/.
