package edu.harvard.logic;

import static com.google.protobuf.util.Timestamps.fromMillis;
import static java.lang.System.currentTimeMillis;

import org.junit.jupiter.api.Test;

import edu.harvard.Chat.Account;
import edu.harvard.Chat.AccountLookupResponse;
import edu.harvard.Chat.ListAccountsRequest;
import edu.harvard.Chat.LoginCreateRequest;
import edu.harvard.Chat.LoginCreateResponse;
import edu.harvard.Chat.ChatMessage;
import edu.harvard.Chat.SendMessageRequest;
import edu.harvard.logic.OperationHandler.HandleException;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

class OperationTest {
  @Test
  void operationTest() throws Exception {
    try {
      Database db = new Database();
      Configuration config = new Configuration("../config.example.json");
      OperationHandler handler = new OperationHandler(db, config);
      // Create two accounts
      LoginCreateRequest u1 = LoginCreateRequest.newBuilder().setUsername("june")
          .setPasswordHash("passwordpasswordpasswordpasswordpasswordpasswordpassword").build();
      LoginCreateRequest u2 = LoginCreateRequest.newBuilder().setUsername("catherine")
          .setPasswordHash("password2passwordpasswordpasswordpasswordpasswordpassword").build();
      assertEquals("1-1", handler.lookupSession(handler.createAccount(u1).getSessionKey()));
      long timestamp = currentTimeMillis();
      Thread.sleep(2);
      assertEquals("1-2", handler.lookupSession(handler.createAccount(u2).getSessionKey()));
      // Reusing a username should fail
      assertEquals(false, handler.createAccount(u2).getSuccess());
      // Log into one
      AccountLookupResponse lookup = handler.lookupAccount("june");
      assertEquals(true, lookup.getExists());
      assertEquals(29, lookup.getBcryptPrefix().length());
      LoginCreateResponse login = handler.login(u1);
      assertEquals(true, login.getSuccess());
      assertEquals(0, login.getUnreadMessages());
      assertTrue(login.getSessionKey().length() > 0);
      assertEquals("1-1", handler.lookupSession(login.getSessionKey()));
      // List accounts
      ListAccountsRequest listRequest1 = ListAccountsRequest.newBuilder().setMaximumNumber(1)
          .setOffsetTimestamp(fromMillis(0))
          .setFilterText("").build();
      List<Account> accountList = handler.listAccounts(listRequest1).getAccountsList();
      assertEquals(accountList.get(0).getUsername(), u1.getUsername());
      assertEquals(1, accountList.size());
      ListAccountsRequest listRequest2 = ListAccountsRequest.newBuilder().setMaximumNumber(1)
          .setOffsetTimestamp(fromMillis(timestamp))
          .setFilterText("").build();
      List<Account> accountList2 = handler.listAccounts(listRequest2).getAccountsList();
      assertEquals(accountList2.get(0).getUsername(), u2.getUsername());
      ListAccountsRequest listRequest3 = ListAccountsRequest.newBuilder().setMaximumNumber(1)
          .setOffsetTimestamp(fromMillis(0))
          .setFilterText("c").build();
      List<Account> accountList3 = handler.listAccounts(listRequest3).getAccountsList();
      assertEquals(accountList3.get(0).getUsername(), u2.getUsername());
      ListAccountsRequest listRequest4 = ListAccountsRequest.newBuilder().setMaximumNumber(1)
          .setOffsetTimestamp(fromMillis(currentTimeMillis())).build();
      List<Account> accountList4 = handler.listAccounts(listRequest4).getAccountsList();
      assertEquals(0, accountList4.size());
      // Send a message
      SendMessageRequest msg = SendMessageRequest.newBuilder().setRecipient("catherine").setMessage("Hi!").build();
      assertEquals("1-1", handler.sendMessage("1-1", msg));
      SendMessageRequest msg2 = SendMessageRequest.newBuilder().setRecipient("june").setMessage("Hi!").build();
      assertThrows(HandleException.class, () -> handler.sendMessage("1-1", msg2));
      SendMessageRequest msg3 = SendMessageRequest.newBuilder().setRecipient("unknown").setMessage("Hi!").build();
      assertThrows(HandleException.class, () -> handler.sendMessage("1-1", msg3));
      // Receive a message
      LoginCreateResponse login2 = handler.login(u2);
      assertEquals(1, login2.getUnreadMessages());
      ChatMessage m = handler.requestMessages("1-2", 5).getMessagesList().get(0);
      assertEquals("1-1", m.getId());
      assertEquals("june", m.getSender());
      assertEquals(msg.getMessage(), m.getMessage());
      // Delete it
      assertEquals(true, handler.deleteMessages("1-2", Arrays.asList("1-1")));
      // Send another message
      assertEquals("1-2", handler.sendMessage("1-1", msg));
      // Delete it
      assertEquals(false, handler.deleteMessages("1-3", Arrays.asList("1-2")));
      assertEquals(true, handler.deleteMessages("1-1", Arrays.asList("1-2")));
      // Verify that this worked
      assertEquals(0, handler.requestMessages("1-2", 5).getMessagesList().size());
      // Delete an account
      handler.deleteAccount("1-1");
    } catch (HandleException e) {
      throw new RuntimeException(e.getMessage());
    }
  }
}
