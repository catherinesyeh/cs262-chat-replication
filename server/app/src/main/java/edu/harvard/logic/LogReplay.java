package edu.harvard.logic;

import static com.google.protobuf.util.Timestamps.fromMillis;
import static java.lang.System.currentTimeMillis;

import edu.harvard.Logreplay.DeleteAccount;
import edu.harvard.Logreplay.DeleteMessages;
import edu.harvard.Logreplay.LogMessage;
import edu.harvard.Logreplay.MarkAsRead;
import edu.harvard.Logreplay.NewAccount;
import edu.harvard.Logreplay.NewChatMessage;

/*
 * Log-replay system. Handles all mutating actions.
 */
public class LogReplay {
  String replica_id = "0";
  int next_account = 1;
  int next_message = 1;

  public LogReplay(String replica_id) {
    this.replica_id = replica_id;
  }

  // Helpers for operation handler to generate IDs
  public synchronized String getAccountId() {
    String id = replica_id.concat("-").concat(String.valueOf(next_account));
    next_account++;
    return id;
  }

  public synchronized String getMessageId() {
    String id = replica_id.concat("-").concat(String.valueOf(next_message));
    next_message++;
    return id;
  }

  // Replaying messages
  public synchronized void replayMessage(LogMessage message) {
    // Switch on type, send to database
    // Save to storage
  }

  // Dispatching messages.
  // Transforms a base log-replay message type into a complete LogMessage,
  // replays it, and then relays it.
  public synchronized void dispatchMessage(LogMessage message) {
    replayMessage(message);
    // todo relay
  }

  public synchronized void dispatchBuilder(LogMessage.Builder builder) {
    LogMessage message = builder.setOriginatingReplicaId(replica_id).setTimestamp(fromMillis(currentTimeMillis()))
        .build();
    dispatchMessage(message);
  }

  public synchronized void dispatchNewAccount(NewAccount newAccount) {
    dispatchBuilder(LogMessage.newBuilder().setNewAccount(newAccount));
  }

  public synchronized void dispatchNewChatMessage(NewChatMessage newChatMessage) {
    dispatchBuilder(LogMessage.newBuilder().setNewChatMessage(newChatMessage));
  }

  public synchronized void dispatchMarkAsRead(MarkAsRead markAsRead) {
    dispatchBuilder(LogMessage.newBuilder().setMarkAsRead(markAsRead));
  }

  public synchronized void dispatchDeleteMessages(DeleteMessages deleteMessages) {
    dispatchBuilder(LogMessage.newBuilder().setDeleteMessages(deleteMessages));
  }

  public synchronized void dispatchDeleteAccount(DeleteAccount deleteAccount) {
    dispatchBuilder(LogMessage.newBuilder().setDeleteAccount(deleteAccount));
  }
}
