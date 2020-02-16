# Start image

The Start image is a simple HLA federate application that provides a GUI (if specified) to display the names of the joined federates in the federation. The Start image exposes a listen port to indicate service readiness.

The examples directory contains several compositions to demonstrate the patterns to containerize HLA federate applications.

The source code of the start application is included in this repository as well.

## Container synopsis

````
start:<vendor>-<platform>

start:lrc

start:app-<platform>
````

The images with the tag `<vendor>-<platform>` are built using the extension pattern.

The other two images contain only the application code and should be used in the composition patterns:

- the image tagged with `start:app-<platform>` contains the application code plus a Java Open JDK to run the application. The LRC volume declared in the LRC container should be mounted into this container at run time to create a functioning HLA federate application.
- the image tagged with `start:lrc` contains only the application code, declared as a volume. The application volume declared in this container should be mounted into the LRC container at run time to create a functioning HLA federate application.

## Environment variables

| Environment variable               | Default                    | Description                                                  | Required |
| ---------------------------------- | -------------------------- | ------------------------------------------------------------ | -------- |
| ``LISTENPORT``                   | None | Port to indicate service readiness. | No       |
| `DISPLAY` | None | The X Display to use. Format: `<address>:0`. | No |

## Examples

Several example docker compose files can be found under the examples directory.

### Extension pattern

Compose files for the extension pattern:

`<vendor>-extension.yml`

### Application composition pattern

Compose files for the composition pattern where the application container is mounted into the LRC container:

`<vendor>-composition-mount-app.yml`

### LRC composition pattern

Compose files for the composition pattern where the LRC container is mounted into the application container:

`<vendor>-composition-mount-lrc.yml`

### Run examples

The examples for the Pitch and VTMaK RTI use skeleton container images. For these vendors the RTI must be installed on the host filesystem first, with the environment variable in the `.env` file set appropriately.

To run an example, use `docker-compose -f <filename> up`.

Notes with examples:

- The Pitch and VTMak Free RTIs have a limit of two applications.

- The Pitch Free RTI displays a message in the X Display that must be confirmed before a federate application can be started.

- Also the VTMaK RTI displays a message that must be closed before a federate application can start.

