package edu.harvard.logic;

/*
 * Log-replay system. Handles all mutating actions.
 */
public class LogReplay {
  int replica_id = 0;
  int next_account = 1;
  int next_message = 1;

  public LogReplay(int replica_id) {
    this.replica_id = replica_id;
  }

  public synchronized String getAccountId() {
    String id = String.valueOf(replica_id).concat("-").concat(String.valueOf(next_account));
    next_account++;
    return id;
  }

  public synchronized String getMessageId() {
    String id = String.valueOf(replica_id).concat("-").concat(String.valueOf(next_message));
    next_message++;
    return id;
  }
}
