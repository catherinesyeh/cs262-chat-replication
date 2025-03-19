# June Moretz Engineering Notebook

## March 12, 2025

I haven't started yet, but some loosely structured notes around how we might go about building this:

log-replay replication
split intermachine and client-server protocols! use separate ports and separate RPC servers
must fix ID assignment post-deletion!
storage - just use the log! you can determine ID assignment from this since deletions are also logged. the log system does mean nothing is truly deleted - privacy - but this is fine (could be garbage collected in theory!)

client requirements:

- one request at a time
- failover
- maybe live-update list of available servers?
- a view of connected servers or a way to manipulate this for demo purposes would be nice

server requirements:

- relay-or-fail before returning from a request
- track other servers' availability to manage relaying

make some sort of virtualized relay-agent channel abstraction to maintain ordering invariants (actually we probably just need to synchronize the entire log-replay-relay system? correctness > speed. this guarantees FIFO)

if a server receives a relay request from a machine it thinks is dead it must reject it - you need to resync with me!

spinup synchronization: contact one server, sync with it + obtain network state info, THEN sync with the others - you have new cutoff values so the syncs should be minimal/zero!

some way to use CLI args to run multiple different servers on the same machine

two people trying to register with the same name at almost the same time is undefined behavior. this could break things - but all other conflicts should be fine. this uniqueness constraint is the largest problem here - solving it in a strictly consistent manner without using a primary-backup system would essentially require transactional semantics, but it'll be glossed over in this implementation.

ultimately, server/client specs should be written up in high-level, readable docs that explain the failure and replication model and how everything is handled!

## March 16, 2025

Getting started today! First priority is going to be writing some proper documentation and the proto files. This should set out clear implementation guidelines that I can follow while moving forward! After that, I'm going to work on configuration, the log-replay system, and data storage in a single-replica configuration and try to get everything working reliably before introducing actual replication support. There's a lot that will need to be changed in the existing code to support the replication model I'm considering.

Todos:

- Rewrite ID system (done!)
- Rewrite server configuration system
- Add timestamp generation/storage and redo pagination to use timestamps (done!)
- Rebuild session keys as JWTs
- Write log-replay system for all database-relevant actions
- Build persistence for log-replay system
- Replication!

Username validation will need to be moved elsewhere - I'm updating the database class to make it usable by the log-replay system. Same with marking messages as read. (update: both of these are done, and are tested in OperationHandlerTest)

## March 17, 2025

I've had some weird issues with tests not running in some cases that I've been unable to figure out, but it does seem I can at least trigger the skipped ones if I specify them? Absolutely no idea what's going on here though. Really weird. I've looked into it and found nothing useful - some of them are just randomly skipped sometimes.

In any case, I've done some debugging of the work from yesterday and also written the new configuration system!

JWTs are now also done. Next up is the log-replay system, which will constitute a major rewrite of the five mutating actions.

I started writing the log-replay system, including the proto specification.

Current todos for log-replay base + persistence:

- Make OperationHandler generate LogMessage types and dispatch them
- Make LogReplay apply these to the database properly
- Make LogReplay save LogMessages persistently
- Replay LogMessages from disk on startup
- Minimal testing for LogReplay system itself (mostly this should already be tested by the operation handler tests - I'm just rewriting internals!)

## March 18, 2025

Action items 1/2 for LogReplay are done! Next up is persistence (and a quick test case for persistence). (Update: all persistence features are done!)

Todo when I start on replication: test LogReplay internal message cache in LogReplayTest, including originating replica ID and timestamps (this is now also done!)

An ordering constraint: when receiving a message backlog after introducing to a replica, the order of messages with a particular type does not matter (one message cannot affect another), but the ordering between types does matter. Thus, all account creations must occur before message sends, message sends before mark as read and delete message, and account creations before account deletions. This invariant matters only for receiving backlog messages, as the timestamps do not provide sufficient ordering due to possible clock drift between replicas. Replays from the local database can be done sequentially - they will never be stored in non-causal order.

Todos for replication:

- Creating a gRPC service for replication (done!)
- Receiving + responding to introductions (done!)
- Network state tracking (done!)
- Sending introductions (done!)
- Processing introduction responses (following above ordering!)
- Full startup flow, properly ordered (done!)
- Relay sending
- Relay receiving (done!)
- Testing

Later todo: sending network state to clients upon request, so clients can update the set of live servers they can communicate with!

## March 19, 2025

I finished a lot of the replication work yesterday. The remaining steps still to be finished:

- Processing introduction responses (done!)
- Relay sending (done!)
- Testing (done!)
  - Introduction
  - Successful relay
  - Failed relay
- Sending network state to clients (done!)

Update - everything is completed! I'll need to run through some manual tests (likely with Catherine) and try out manually spinning up a few servers, particularly across a network, but the automated tests seem to indicate everything is working exactly as expected. Very good to see!
