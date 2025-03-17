package edu.harvard.Logic;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import edu.harvard.Data.Data;

public class DatabaseTest {
  Data.Message buildMessage(String sender_id, String recipient_id, boolean read, String message, int id) {
    Data.Message m = new Data.Message();
    m.sender_id = sender_id;
    m.recipient_id = recipient_id;
    m.read = read;
    m.message = message;
    m.id = "1-".concat(String.valueOf(id));
    return m;
  }

  @Test
  void accountOperationsWork() {
    Data.Account a1 = new Data.Account();
    a1.username = "june";
    a1.password_hash = "test1";
    a1.id = "1-1";
    Data.Account a2 = new Data.Account();
    a2.username = "catherine";
    a2.password_hash = "test2";
    a2.id = "1-2";

    Database db = new Database();
    db.createAccount(a1);
    assertEquals(db.getAllAccounts().size(), 1);
    assertNull(db.lookupAccountByUsername(a2.username));
    assertEquals(db.lookupAccount("1-1").username, a1.username);
    assertEquals(db.lookupAccount("1-1").password_hash, a1.password_hash);
    assertEquals(db.lookupAccount("1-1").id, "1-1");
    db.createAccount(a2);
    assertEquals(db.getAllAccounts().size(), 2);
    assertEquals(db.lookupAccountByUsername(a2.username).username, a2.username);
    assertEquals(db.lookupAccount("1-2").username, a2.username);
    assertEquals(db.getUnreadMessages("1-2", 10).size(), 0);
    db.deleteAccount("1-1");
    assertEquals(db.getAllAccounts().size(), 1);
    assertEquals(db.getAllAccounts().iterator().next().username, a2.username);
    assertNull(db.lookupAccountByUsername("june"));
  }

  @Test
  void messageOperationsWork() {
    Data.Account a1 = new Data.Account();
    a1.username = "june";
    a1.password_hash = "test1";
    Data.Account a2 = new Data.Account();
    a2.username = "catherine";
    a2.password_hash = "test2";
    // Create the same message a lot so we can send it repeatedly
    Data.Message m1 = buildMessage("1-1", "1-2", false, "message!", 1);
    Data.Message m2 = buildMessage("1-1", "1-2", false, "message!", 2);
    Data.Message m3 = buildMessage("1-1", "1-2", false, "message!", 3);
    Data.Message m4 = buildMessage("1-1", "1-2", false, "message!", 4);
    Data.Message m5 = buildMessage("1-1", "1-2", false, "message!", 5);
    Data.Message m6 = buildMessage("1-1", "1-2", false, "message!", 6);
    Data.Message m7 = buildMessage("1-1", "1-2", false, "message!", 7);
    Data.Message m8 = buildMessage("1-1", "1-2", false, "message!", 8);
    Data.Message m9 = buildMessage("1-1", "1-2", true, "message!", 9);

    Database db = new Database();
    db.createAccount(a1);
    db.createAccount(a2);
    // Send message once
    db.createMessage(m1);
    assertEquals(db.getUnreadMessages("1-2", 1).getFirst().message, m1.message);
    assertEquals(db.getUnreadMessages("1-1", 1).size(), 0);
    // Send message repeatedly
    db.createMessage(m2);
    db.createMessage(m3);
    db.createMessage(m4);
    db.createMessage(m5);
    db.createMessage(m6);
    db.createMessage(m7);
    assertEquals(db.getUnreadMessageCount("1-2"), 7);
    assertEquals(db.getUnreadMessages("1-2", 1).size(), 1);
    assertEquals(db.getUnreadMessages("1-2", 3).size(), 3);
    assertEquals(db.getUnreadMessages("1-2", 10).size(), 7);
    // Test delete
    db.createMessage(m8);
    db.deleteMessage("1-8");
    assertEquals(db.getUnreadMessages("1-2", 10).size(), 7);
    // Test already-read message
    db.createMessage(m9);
    assertEquals(db.getUnreadMessages("1-2", 10).size(), 7);
    assertNotNull(db.getMessage("1-9"));
  }

}
