package edu.harvard.logic;

import static com.google.protobuf.util.Timestamps.fromMillis;
import static com.google.protobuf.util.Timestamps.toMillis;
import static java.lang.System.currentTimeMillis;

import java.util.ArrayList;

import edu.harvard.Logreplay.DeleteAccount;
import edu.harvard.Logreplay.DeleteMessages;
import edu.harvard.Logreplay.LogMessage;
import edu.harvard.Logreplay.MarkAsRead;
import edu.harvard.Logreplay.NewAccount;
import edu.harvard.Logreplay.NewChatMessage;
import edu.harvard.data.Data.Account;
import edu.harvard.data.Data.Message;

/*
 * Log-replay system. Handles all mutating actions.
 */
public class LogReplay {
  Database db;
  String replica_id = "0";
  int next_account = 1;
  int next_message = 1;

  ArrayList<LogMessage> messages;

  public LogReplay(String replica_id, Database db) {
    this.replica_id = replica_id;
    this.db = db;
    this.messages = new ArrayList<>();
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
    switch (message.getLogMessageCase()) {
      case LogMessage.LogMessageCase.NEW_ACCOUNT:
        Account account = new Account();
        account.id = message.getNewAccount().getId();
        account.username = message.getNewAccount().getUsername();
        account.client_bcrypt_prefix = message.getNewAccount().getBcryptPrefix();
        account.password_hash = message.getNewAccount().getPasswordHash();
        account.timestamp = toMillis(message.getNewAccount().getCreatedAt());
        db.createAccount(account);
        break;
      case LogMessage.LogMessageCase.NEW_CHAT_MESSAGE:
        Message m = new Message();
        m.message = message.getNewChatMessage().getMessage();
        m.recipient_id = message.getNewChatMessage().getRecipientId();
        m.sender_id = message.getNewChatMessage().getSenderId();
        m.read = message.getNewChatMessage().getRead();
        m.id = message.getNewChatMessage().getId();
        m.timestamp = toMillis(message.getNewChatMessage().getCreatedAt());
        db.createMessage(m);
        break;
      case LogMessage.LogMessageCase.MARK_AS_READ:
        db.markMessagesRead(message.getMarkAsRead().getIdList());
        break;
      case LogMessage.LogMessageCase.DELETE_MESSAGES:
        for (String i : message.getDeleteMessages().getIdList()) {
          db.deleteMessage(i);
        }
        break;
      case LogMessage.LogMessageCase.DELETE_ACCOUNT:
        db.deleteAccount(message.getDeleteAccount().getId());
        break;
      default:
    }
    // Add to in-memory log message cache
    messages.add(message);
  }

  public synchronized void saveMessage(LogMessage message) {
    // todo
  }

  // replay and save
  public synchronized void receiveMessage(LogMessage message) {
    replayMessage(message);
    saveMessage(message);
  }

  // Dispatching internally-generated messages.
  // Transforms a base log-replay message type into a complete LogMessage,
  // replays it, saves it, and then relays it.
  public synchronized void dispatchMessage(LogMessage message) {
    receiveMessage(message);
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
