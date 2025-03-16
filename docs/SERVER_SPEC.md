# General Chat Server Specifications

## Password hashing

Passwords are double-hashed using bcrypt. Double-hashing means that the password will be hashed both by the client and by the server. The server will use the bcrypt hash sent by the client as an input for its own hashing function. Both the client and server hash will have unique salts. Double-hashing prevents values obtained by exfiltrating the database from being used for future login, while also protecting passwords in transit.

The client can obtain the bcrypt settings needed for login by making an account lookup request; this request will return the password salt and number of bcrypt rounds to use. All password hashes will be transmitted in the standard bcrypt string format, which includes the number of rounds, salt, and hash.

## Session keys

Logging in or creating an account returns a string session key. This must be sent in all future requests to identify a user's session. Session keys are JWTs, allowing them to be used across replicas.

## Request/Response System

No streaming responses exist. The recipient should receive exactly one response per gRPC call.

## IDs

All entities (accounts and messages) are assigned a unique string ID. This ID is in the format `[originating replica ID]-[ascending ID]`. The ascending ID component will always be assigned in ascending order by the originating replica. IDs will never be reused. This ensures that IDs are unique no matter which replica receives a send message or create account request from a user.

## Pagination

Entities are returned in order of creation timestamp (oldest first), as assigned by its originating replica. A creation timestamp can then be used as an offset timestamp in an account listing request - the server will then return only entities with a more recent timestamp. Pagination is currently only used for listing accounts, as messages are implicitly paginated by their delivered status, as noted below.

## Maximum Lengths

**Usernames:** 255 characters (2^8-1)

**Messages:** 65535 characters (2^16-1)

## Message delivery

You cannot send a message to yourself.

Only the delivery of new/unread messages is supported by the protocol, but all messages are stored. Once a message has been delivered, it is marked as read and will not be redelivered.

## Account Deletion

Deletion of an account marks the account as deleted. The username will remain claimed in the database. The user's hashed password will be deleted, as will all messages received by that user, including unread messages. Messages sent by the user will remain sent.

## Persistence and Log-Replay

The server will use a system we term Log-Replay for handling persistence and replication. The Log-Replay system works as follows:

Every request that modifies the database (creating an account, sending a message, reading messages, deleting messages, and deleting an account) is associated with a _log message_. When a request is sent to a server, it can be sent to any online server - the server receiving the message is termed the _originating replica_. When handling a request, the originating replica generates a log message, which is associated with the originating replica's ID, a timestamp generated from the local clock, and the details needed to apply the operation to the database.

Once a log message has been generated, it is dispatched to the log-replay system. The log-replay system stores the log message on disk and applies its contents to the database.

When a server first spins up, if it has a database file specified in its configuration, it will read log messages from the database file and replay them through the log-replay system in order to bring its in-memory database up to date.

## Replication

The replication protocol also uses the log-replay system to establish a consistent set of log messages on all machines, and thus a consistent database.

### Originating replicas

When an originating replica receives a request, it will generate a log message. Once that log message has been saved and replied locally, it will be dispatched to all other servers in the network. (Each replica is aware of all other active replicas, as described below.) The request is complete once the log message has either been delivered to all servers or failed.

### Non-originating replicas

All replicas maintain a separate gRPC server, at a separate port, for communicating inter-replica log messages. Non-originating replicas can be sent a log message on their inter-replica gRPC server by originating replicas, and will then save that log message locally and apply it to their database, as if it was generated internally. This allows a consistent datastore to be established between replicas. This datastore is not necessarily strongly consistent, but since originating replicas do not return from a request until a log message has been synchronized to all available servers, any available server should be in a consistent state once the request returns. (Servers for which synchronization failed may be temporarily inconsistent; see below.) The one opportunity for failure is usernames in registration, where it is possible for a conflict to arise between usernames since they must be unique. The protocol does not currently enforce transactional semantics for creating accounts, and thus in rare circumstances an account creation that returns successfully could lead to a state inconsistent from what the client may have expected.

### Spin-up

New replicas can be added to the network dynamically. When a replica spins up, it may or may not already have an associated datastore. If it has an associated datastore, it will begin by replaying it, as discussed above. At this point, it will attempt to connect to the entry point set in its configuration file in order to connect to the network. When connecting to the entry point, it will send its own inter-replica gRPC IP address and port, as well as the latest timestamp for log messages it has received for every originating replica ID. The entry point will then send back the network state (all other replicas that it believes are currently online) and all log messages newer than the cutoffs sent by the connecting replica. Since timestamps are tracked individually for each originating replica ID, time synchronization is not necessary so long as timestamps monotonically increase for each originating replica. The new replica will then synchronize with every other entry point on the network in a similar manner.

### Tracking Network State

Each replica tracks the identity of every other replica so it knows where to relay messages. The spin-up process described above is used to synchronize entering replicas with each other, which enters them into the network state map of every replica they introduce themselves to. A failure to relay a log message leads to a replica being removed from the network map by the originating replica. If that replica then attempts to relay a message to the replica that removed it from its network map, it will be instructed to re-synchronize before being allowed to relay a message. This ensures that replicas can drop in and out of the network and will still end up in a consistent state.

## Internals

The server code is in the `server/app/src` directory. `main` contains all the functional classes, while `test` contains unit tests.

The `Logic` package contains the actual database and operation logic. These classes only handle internal data classes, and do not interact with the data sent over the network directly, though the `OperationHandler` does reuse some Protobuf generated classes. The database is an in-memory datastore, with no persistence, and is created in `App`. All methods are `synchronized` to allow for cross thread use.

The `App` class sets up the gRPC server and handles incoming RPC requests.
