package edu.harvard.logic;

import static com.google.protobuf.util.Timestamps.fromMillis;
import static com.google.protobuf.util.Timestamps.toMillis;
import static java.lang.System.currentTimeMillis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import at.favre.lib.crypto.bcrypt.BCrypt;
import edu.harvard.Chat;
import edu.harvard.Chat.AccountLookupResponse;
import edu.harvard.Chat.AvailableReplicas;
import edu.harvard.Chat.LoginCreateRequest;
import edu.harvard.Chat.LoginCreateResponse;
import edu.harvard.Chat.RequestMessagesResponse;
import edu.harvard.Chat.ListAccountsRequest;
import edu.harvard.Chat.ListAccountsResponse;
import edu.harvard.Chat.ChatMessage;
import edu.harvard.Chat.SendMessageRequest;
import edu.harvard.Chat.ServerInfo;
import edu.harvard.Logreplay.DeleteAccount;
import edu.harvard.Logreplay.DeleteMessages;
import edu.harvard.Logreplay.MarkAsRead;
import edu.harvard.Logreplay.NewAccount;
import edu.harvard.Logreplay.NewChatMessage;
import edu.harvard.Logreplay.ReplicaInfo;
import edu.harvard.data.Data.Account;
import edu.harvard.data.Data.Message;

/*
 * Higher-level logic for all operations.
 */
public class OperationHandler {
  // Custom exception, caught in AppThread to generate failure responses
  public class HandleException extends Exception {
    public HandleException(String errorMessage) {
      super(errorMessage);
    }
  }

  private Database db;
  private LogReplay logReplay;
  private Configuration configuration;
  private ReplicationService replication;

  public OperationHandler(Database db, LogReplay logReplay, Configuration configuration,
      ReplicationService replication) {
    this.db = db;
    this.logReplay = logReplay;
    this.configuration = configuration;
    this.replication = replication;
  }

  private String createSession(String accountID) {
    Algorithm algorithm = Algorithm.HMAC512(configuration.jwtSecret);
    String token = JWT.create()
        .withSubject(accountID)
        .sign(algorithm);
    return token;
  }

  public String lookupSession(String key) {
    Algorithm algorithm = Algorithm.HMAC512(configuration.jwtSecret);
    JWTVerifier verifier = JWT.require(algorithm)
        .build();
    DecodedJWT decodedJWT = verifier.verify(key);
    return decodedJWT.getSubject();
  }

  public AccountLookupResponse lookupAccount(String username) {
    AccountLookupResponse.Builder response = AccountLookupResponse.newBuilder();
    Account account = db.lookupAccountByUsername(username);
    if (account == null) {
      response.setExists(false);
      return response.build();
    }
    response.setExists(true);
    response.setBcryptPrefix(account.client_bcrypt_prefix);
    return response.build();
  }

  public LoginCreateResponse createAccount(LoginCreateRequest request) throws HandleException {
    NewAccount.Builder account = NewAccount.newBuilder();
    try {
      account.setBcryptPrefix(request.getPasswordHash().substring(0, 29));
    } catch (Exception e) {
      throw new HandleException("Invalid password hash!");
    }
    if (db.accountExists(request.getUsername())) {
      return LoginCreateResponse.newBuilder().setSuccess(false).build();
    }
    account.setUsername(request.getUsername());
    account.setPasswordHash(BCrypt.withDefaults().hashToString(12, request.getPasswordHash().toCharArray()));
    account.setId(logReplay.getAccountId());
    account.setCreatedAt(fromMillis(currentTimeMillis()));
    logReplay.dispatchNewAccount(account.build());
    String key = createSession(account.getId());
    return LoginCreateResponse.newBuilder().setSuccess(true).setUnreadMessages(0).setSessionKey(key).build();
  }

  public LoginCreateResponse login(LoginCreateRequest request) {
    LoginCreateResponse.Builder response = LoginCreateResponse.newBuilder();
    // Get account
    Account account = db.lookupAccountByUsername(request.getUsername());
    if (account == null) {
      response.setSuccess(false);
      return response.build();
    }
    BCrypt.Result result = BCrypt.verifyer().verify(request.getPasswordHash().toCharArray(), account.password_hash);
    if (!result.verified) {
      response.setSuccess(false);
      return response.build();
    }
    String key = createSession(account.id);
    int unreadCount = db.getUnreadMessageCount(account.id);
    response.setSuccess(true);
    response.setUnreadMessages(unreadCount);
    response.setSessionKey(key);
    return response.build();
  }

  public ListAccountsResponse listAccounts(ListAccountsRequest request) {
    ArrayList<Account> list = new ArrayList<>(request.getMaximumNumber());
    Collection<Account> allAccounts = db.getAllAccounts();
    for (Account account : allAccounts) {
      boolean include = true;
      if (account.timestamp <= toMillis(request.getOffsetTimestamp())) {
        include = false;
      }
      if (!account.username.contains(request.getFilterText())) {
        include = false;
      }
      if (include) {
        list.add(account);
      }
      if (list.size() >= request.getMaximumNumber()) {
        break;
      }
    }
    List<Chat.Account> responseList = new ArrayList<>(list.size());
    for (Account a : list) {
      responseList.add(Chat.Account.newBuilder()
        .setId(a.id)
        .setUsername(a.username)
        .setCreatedAt(fromMillis(a.timestamp))
        .build());
    }
    return ListAccountsResponse.newBuilder().addAllAccounts(responseList).build();
  }

  public String sendMessage(String sender_id, SendMessageRequest request) throws HandleException {
    // Look up sender
    Account sender = db.lookupAccount(sender_id);
    if (sender == null) {
      throw new HandleException("Sender does not exist!");
    }
    // Look up recipient
    Account account = db.lookupAccountByUsername(request.getRecipient());
    if (account == null) {
      throw new HandleException("Recipient does not exist!");
    }
    if (account.id.equals(sender_id)) {
      throw new HandleException("You cannot message yourself!");
    }
    // Build NewChatMessage
    NewChatMessage m = NewChatMessage.newBuilder()
        .setMessage(request.getMessage())
        .setSenderId(sender_id)
        .setRecipientId(account.id)
        .setRead(false)
        .setId(logReplay.getMessageId())
        .setCreatedAt(fromMillis(currentTimeMillis()))
        .build();
    logReplay.dispatchNewChatMessage(m);
    return m.getId();
  }

  public RequestMessagesResponse requestMessages(String user_id, int maximum_number) {
    List<Message> unreadMessages = db.getUnreadMessages(user_id, maximum_number);
    ArrayList<ChatMessage> responseMessages = new ArrayList<>(unreadMessages.size());
    ArrayList<String> messageIds = new ArrayList<>(unreadMessages.size());
    for (Message message : unreadMessages) {
      ChatMessage.Builder messageResponse = ChatMessage.newBuilder();
      messageResponse.setId(message.id);
      messageResponse.setMessage(message.message);
      messageResponse.setSender(db.lookupAccount(message.sender_id).username);
      responseMessages.add(messageResponse.build());
      messageIds.add(message.id);
    }
    logReplay.dispatchMarkAsRead(MarkAsRead.newBuilder().addAllId(
        messageIds).build());
    return RequestMessagesResponse.newBuilder().addAllMessages(responseMessages).build();
  }

  // returns success boolean
  public boolean deleteMessages(String user_id, List<String> ids) {
    for (String i : ids) {
      Message m = db.getMessage(i);
      if (!m.recipient_id.equals(user_id) && !m.sender_id.equals(user_id)) {
        return false;
      }
    }
    logReplay.dispatchDeleteMessages(DeleteMessages.newBuilder().addAllId(
        ids).build());
    return true;
  }

  public void deleteAccount(String user_id) {
    logReplay.dispatchDeleteAccount(DeleteAccount.newBuilder().setId(user_id).build());
  }

  public AvailableReplicas getAvailableReplicas() {
    List<ServerInfo> response = new ArrayList<>();
    List<ReplicaInfo> liveReplicas = replication.getOtherReplicas();
    for (ReplicaInfo replica : liveReplicas) {
      response.add(ServerInfo.newBuilder().setHostname(replica.getHostname()).setPort(replica.getPort()).build());
    }
    return AvailableReplicas.newBuilder().addAllReplicas(response).build();
  }
}
