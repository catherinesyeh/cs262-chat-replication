package edu.harvard.logic;

import org.junit.jupiter.api.Test;

import edu.harvard.Chat.LoginCreateRequest;
import edu.harvard.Chat.SendMessageRequest;
import edu.harvard.Logreplay.ReplicaSyncState;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

class LogReplayTest {
  @Test
  void saveToDiskWorks() throws Exception {
    Configuration config = new Configuration("../config.example.json");
    Files.deleteIfExists(Path.of("../".concat(config.databaseFile)));
    Database db1 = new Database();
    LogReplay logReplay1 = new LogReplay(config.replicaID, config.databaseFile, db1);
    OperationHandler handler = new OperationHandler(db1, logReplay1, config);
    // Create two accounts and a message with this operation handler
    LoginCreateRequest u1 = LoginCreateRequest.newBuilder().setUsername("june")
        .setPasswordHash("passwordpasswordpasswordpasswordpasswordpasswordpassword").build();
    LoginCreateRequest u2 = LoginCreateRequest.newBuilder().setUsername("catherine")
        .setPasswordHash("password2passwordpasswordpasswordpasswordpasswordpassword").build();
    assertEquals("1-1", handler.lookupSession(handler.createAccount(u1).getSessionKey()));
    Thread.sleep(2);
    assertEquals("1-2", handler.lookupSession(handler.createAccount(u2).getSessionKey()));
    SendMessageRequest msg = SendMessageRequest.newBuilder().setRecipient("catherine").setMessage("Hi!").build();
    assertEquals("1-1", handler.sendMessage("1-1", msg));
    // Replay them into a new database
    Database db2 = new Database();
    LogReplay logReplay2 = new LogReplay(config.replicaID, config.databaseFile, db2);
    logReplay2.replayMessagesFromDisk();
    assertEquals(2, db2.getAllAccounts().size());
    assertEquals("Hi!", db2.getMessage("1-1").message);
    // Test replication service helpers
    List<ReplicaSyncState> syncStates = logReplay2.getLatestTimestamps();
    assertEquals(1, syncStates.size());
    assertEquals("1", syncStates.get(0).getId());
    assertEquals(0, logReplay2.getNewerMessages(syncStates).size());
    // Send another message to replica 1 to be sure these work
    SendMessageRequest msg2 = SendMessageRequest.newBuilder().setRecipient("catherine").setMessage("Hi!").build();
    assertEquals("1-2", handler.sendMessage("1-1", msg2));
    assertEquals(1, logReplay1.getNewerMessages(syncStates).size());
    assertEquals(0, logReplay1.getNewerMessages(logReplay1.getLatestTimestamps()).size());
  }
}
