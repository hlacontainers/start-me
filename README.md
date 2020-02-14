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

## Run the examples

Several example docker compose files can be found under the examples directory.

Compose files for the extension pattern:

- `<vendor>-extension*.yml` : starts a federate application for the `<vendor>` RTI using the extension pattern. Note that the Pitch and VTMak Free RTIs have a limit of two applications, so not all applications will be able to join.


Compose files for the two composition patterns:

- `<vendor>-composition*.yml` : starts a federate application for the `<vendor>` RTI using a composition pattern.


To run an example, use `docker-compose -f <filename> up`.

Note that the Pitch Free RTI displays a message in the X Display that must be confirmed before any federate application can be started.

Also the VTMaK RTI displays a message that must be confirmed before a federate application can start.

