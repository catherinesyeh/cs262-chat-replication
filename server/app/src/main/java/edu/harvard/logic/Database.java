package edu.harvard.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import edu.harvard.data.Data.Account;
import edu.harvard.data.Data.Message;

/*
 * Properly-synchronized in-memory datastore.
 * Methods are designed specifically to meet application needs.
 * Higher-level application logic will take place outside the database.
 */
public class Database {
  private Map<String, Account> accountMap;
  private Map<String, String> accountUsernameMap;
  private Map<String, Message> messageMap;

  // Optimization for getting unread messages
  private Map<String, ArrayList<String>> unreadMessagesPerAccount;

  public Database() {
    accountMap = new HashMap<>();
    accountUsernameMap = new HashMap<>();
    messageMap = new HashMap<>();
    unreadMessagesPerAccount = new HashMap<>();
  }

  public synchronized Account lookupAccount(String id) {
    return accountMap.get(id);
  }

  public synchronized Account lookupAccountByUsername(String username) {
    return accountMap.get(accountUsernameMap.get(username));
  }

  public synchronized boolean accountExists(String username) {
    return accountUsernameMap.containsKey(username);
  }

  public synchronized void createAccount(Account account) {
    accountMap.put(account.id, account);
    accountUsernameMap.put(account.username, account.id);
  }

  public synchronized Collection<Account> getAllAccounts() {
    return accountMap.values().stream().sorted(Comparator.comparing(account -> account.timestamp))
        .collect(Collectors.toList());
  }

  /*
   * Adds a message to the database.
   * If message.read is false, also adds it to a user's unread list.
   */
  public synchronized void createMessage(Message message) {
    messageMap.put(message.id, message);
    if (!message.read) {
      List<String> unreads = unreadMessagesPerAccount.get(message.recipient_id);
      if (unreads != null) {
        unreads.add(message.id);
      } else {
        unreadMessagesPerAccount.put(message.recipient_id, new ArrayList<>(Arrays.asList(message.id)));
      }
    }
  }

  public synchronized int getUnreadMessageCount(String user_id) {
    ArrayList<String> unreads = unreadMessagesPerAccount.get(user_id);
    if (unreads == null) {
      return 0;
    }
    return unreads.size();
  }

  public synchronized Message getMessage(String id) {
    return messageMap.get(id);
  }

  /*
   * Gets the first [number] unread messages for a user
   */
  public synchronized List<Message> getUnreadMessages(String user_id, int number) {
    ArrayList<Message> list = new ArrayList<>(number);
    ArrayList<String> unreads = unreadMessagesPerAccount.get(user_id);
    if (unreads == null) {
      return list;
    }
    for (int i = 0; i < number; i++) {
      if (unreads.size() > i) {
        list.add(messageMap.get(unreads.get(i)));
      } else {
        break;
      }
    }
    return list;
  }

  public synchronized void markMessagesRead(List<String> message_ids) {
    for (String id : message_ids) {
      Message m = messageMap.get(id);
      m.read = true;
      unreadMessagesPerAccount.get(m.recipient_id).remove(id);
    }
  }

  /*
   * Verification that the user can delete this message must take place in
   * higher-level logic.
   */
  public synchronized void deleteMessage(String id) {
    Message m = messageMap.get(id);
    if (m != null) {
      if (!m.read) {
        unreadMessagesPerAccount.get(m.recipient_id).remove(id);
      }
      messageMap.remove(id);
    }
  }

  /*
   * The username remains claimed.
   */
  public synchronized void deleteAccount(String id) {
    unreadMessagesPerAccount.remove(id);
    accountMap.remove(id);
  }
}
