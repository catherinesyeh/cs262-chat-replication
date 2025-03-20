# cs262-chat-replication

CS262 Design Exercise 4: Chat Server with Replication

This project should provide a fully functional chat system built with gRPC that is 2-fault tolerant + has persistent storage.

## Setup

1. Duplicate [server/config.example.json](server/config.example.json) and rename to `server/config.json`, or `server/config-1.json` or similar if multiple servers are being run.
   - Fill in your configuration details. Be sure these match!
   - If you're not using replication, you can omit the `introductionPoint`.
   - If you're using replication:
     - Create a configuration with no `introductionPoint` and start that server first.
     - Use that server as the `introductionPoint` for other servers. The `port` should be that server's `replicaPort`!
     - All `replicaID`s must be unique. All `jwtSecret`s must be identical. If you're running multiple servers from the same folder/machine, be sure to use different `databaseFile`s for them!
2. Duplicate [config_example.json](config_example.json) and rename to `config.json`.
   - Fill in your configuration details.
   - Make sure the ports match the `clientPort` entries in your server config files!
3. Install the python dependencies for the client (this requires `poetry` to be installed):

```
poetry install
```

## Proto Files

The chat proto file is in the `proto/` directory. It is symlinked into the expected subdirectory in the server directory. The log-replay proto file is used by the log-replay system on the server side only, and is only located in the `server/src/main/proto/edu/harvard` directory.

## Server

The server is a Java application built using Gradle. On Linux, run `./gradlew run --args='[config file path]'` from the `server` directory to run the server. (On Windows, this can be replaced with `./gradlew.bat`.) The argument can be omitted if `config.json` is the name of the configuration file.

### Server Testing

Run `./gradlew test` from the `server` directory.

## Client

The client is a Python application with a Tkinter interface.

1. Navigate to [client/](client/) folder:

```
cd client
```

2. Start client:

```
poetry run python client.py
```

### Client Testing

1. Navigate into [client/tests/](client/tests/) folder:

```
cd client/tests
```

2. Start tests:

```
poetry run pytest
```

- Integration tests: [client/tests/test_integration.py](client/tests/test_integration.py)
  - Note: These tests do require the server and expect a clean database, so we suggest restarting the server and clearing old db files before running them.
  - The integration tests will also log metrics to the [client/tests/logs/](client/tests/logs/) directory.

## Documentation

More comprehensive internal documentation (including engineering notebooks with our efficiency analysis) is in the [docs/](docs/) folder.
