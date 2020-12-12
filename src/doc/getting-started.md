# Getting started with Docker

The easier way to start with OpenSearchServer 2.0 is to use the Docker container.

## Docker installation

Be sure you have docker installed. Follow the instructions on Docker's documentation:

[docs.docker.com/get-docker](https://docs.docker.com/get-docker/)

## Dockers images

The official home of OpenSearchServer's docker images is here:

[hub.docker.com/r/opensearchserver/opensearchserver](https://hub.docker.com/r/opensearchserver/opensearchserver)

The lastest version is 2.0-dev. It contains the latest build from the development branch.

## Pull and run

To start OpenSearchServer 2.0, just use the following run command:

    docker run -p 9090:9090 opensearchserver/opensearchserver

## Network port

The parameter -p exposes the main HTTP port.

You can then use your browser to navigate on the user interface.
Assuming it is localhost, you can reach the user interface using the following URL:

[http://localhost:9090](http://localhost:9090)

## Volume

Everything you do on OpenSearchServer (indexes files, data crawling) is stored in a working directory.

The docker image defines the volume `/var/lib/opensearchserver/` to context those data.

## DockerFile

The docker file used to create this container is here:

[github.com/jaeksoft/opensearchserver/blob/master/Dockerfile](https://github.com/jaeksoft/opensearchserver/blob/master/Dockerfile)

