# June Moretz Engineering Notebook

## March 12, 2025

I haven't started yet, but some loosely structured notes around how we might go about building this:

log-replay replication
split intermachine and client-server protocols! use separate ports and separate RPC servers
must fix ID assignment post-deletion!
storage - just use the log! you can determine ID assignment from this since deletions are also logged. the log system does mean nothing is truly deleted - privacy - but whatever

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

two people trying to register with the same name at almost the same time is undefined behavior. this could break things - but all other conflicts should be fine

ultimately, server/client specs should be written up in high-level, readable docs that explain the failure and replication model and how everything is handled!
