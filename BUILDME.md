# Build the Start images

Adapt  the version of each RTI in the `.env` file as needed.

The following Docker Compose files should be used to build the container images.

## Extension pattern

`docker-compose -f build-pitch.yml build`

`docker-compose -f build-portico.yml build`

`docker-compose -f build-vtmak.yml build`

## Composition patterns

`docker-compose -f build.yml build`

